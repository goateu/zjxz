package com.atguigu.java.ai.langchain4j.service.impl;

import com.atguigu.java.ai.langchain4j.dto.QueueDTO;
import com.atguigu.java.ai.langchain4j.entity.QueueEntry;
import com.atguigu.java.ai.langchain4j.mapper.QueueEntryMapper;
import com.atguigu.java.ai.langchain4j.service.QueueService;
import com.atguigu.java.ai.langchain4j.vo.QueuePositionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class QueueServiceImpl implements QueueService {

    private static final String QUEUE_DOCTOR_KEY = "queue:doctor:";
    private static final int QUEUE_TIMEOUT_MINUTES = 15;

    @Autowired
    private QueueEntryMapper queueEntryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void joinQueue(QueueDTO queueDTO) {
        // 检查医生当前排队人数和正在问诊的人数
        Long doctorId = queueDTO.getDoctorId();
        Long patientId = queueDTO.getPatientId();
        Long sessionId = queueDTO.getSessionId();

        System.out.println("=== joinQueue called: doctorId=" + doctorId + ", patientId=" + patientId + ", sessionId=" + sessionId);

        // 计算排队位置
        String queueKey = QUEUE_DOCTOR_KEY + doctorId;
        Long position = redisTemplate.opsForList().size(queueKey);

        System.out.println("=== Queue size before push: " + position);

        // 创建排队记录
        QueueEntry queueEntry = new QueueEntry();
        queueEntry.setDoctorId(doctorId);
        queueEntry.setPatientId(patientId);
        queueEntry.setSessionId(queueDTO.getSessionId());
        queueEntry.setPosition(position != null ? position.intValue() + 1 : 1);
        queueEntry.setEstimatedWaitMinutes(calculateEstimatedWait(doctorId, position));
        queueEntry.setStatus("WAITING");
        queueEntry.setCreateTime(LocalDateTime.now());
        queueEntry.setUpdateTime(LocalDateTime.now());
        queueEntryMapper.insert(queueEntry);

        // 加入Redis排队队列
        redisTemplate.opsForList().rightPush(queueKey, patientId.toString());
        redisTemplate.expire(queueKey, QUEUE_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        // 更新排队位置
        QueuePositionVO queuePosition = getQueuePosition(patientId);
        if (queuePosition != null) {
            System.out.println("=== Sending queue position to /topic/queue." + patientId + ": " + queuePosition);
            messagingTemplate.convertAndSend("/topic/queue." + patientId, queuePosition);
        }

        // 广播排队患者列表给医生
        broadcastQueueList(doctorId);
    }

    @Override
    @Transactional
    public void cancelQueue(QueueDTO queueDTO) {
        Long patientId = queueDTO.getPatientId();
        Long doctorId = queueDTO.getDoctorId();

        // 从Redis队列移除
        String queueKey = QUEUE_DOCTOR_KEY + doctorId;
        redisTemplate.opsForList().remove(queueKey, 1, patientId.toString());

        // 更新数据库状态
        LambdaQueryWrapper<QueueEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueueEntry::getPatientId, patientId)
               .eq(QueueEntry::getDoctorId, doctorId)
               .eq(QueueEntry::getStatus, "WAITING");
        QueueEntry queueEntry = queueEntryMapper.selectOne(wrapper);
        if (queueEntry != null) {
            queueEntry.setStatus("CANCEL");
            queueEntry.setUpdateTime(LocalDateTime.now());
            queueEntryMapper.updateById(queueEntry);
        }

        // 广播更新排队位置
        broadcastQueuePosition(doctorId);
    }

    @Override
    @Transactional
    public void callPatient(QueueDTO queueDTO) {
        Long doctorId = queueDTO.getDoctorId();
        Long patientId = queueDTO.getPatientId();
        Long sessionId = queueDTO.getSessionId();

        // 从Redis队列取出患者
        String queueKey = QUEUE_DOCTOR_KEY + doctorId;
        Object first = redisTemplate.opsForList().index(queueKey, 0);
        if (first != null && first.toString().equals(patientId.toString())) {
            redisTemplate.opsForList().leftPop(queueKey);
        }

        // 更新数据库状态为已叫号
        LambdaQueryWrapper<QueueEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueueEntry::getPatientId, patientId)
               .eq(QueueEntry::getDoctorId, doctorId)
               .eq(QueueEntry::getStatus, "WAITING")
               .orderByDesc(QueueEntry::getCreateTime)
               .last("LIMIT 1");
        QueueEntry queueEntry = queueEntryMapper.selectOne(wrapper);
        if (queueEntry != null) {
            queueEntry.setStatus("CALLED");
            queueEntry.setSessionId(sessionId);
            queueEntry.setUpdateTime(LocalDateTime.now());
            queueEntryMapper.updateById(queueEntry);
        }

        // 通知患者被叫到
        messagingTemplate.convertAndSend("/topic/queue." + patientId + ".called", sessionId);

        // 更新医生的问诊数
        String countKey = "doctor:consult:count:" + doctorId;
        redisTemplate.opsForValue().increment(countKey);

        // 广播更新排队位置
        broadcastQueuePosition(doctorId);
    }

    @Override
    public QueuePositionVO getQueuePosition(Long patientId) {
        LambdaQueryWrapper<QueueEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueueEntry::getPatientId, patientId)
               .in(QueueEntry::getStatus, "WAITING", "CALLED")
               .orderByDesc(QueueEntry::getCreateTime)
               .last("LIMIT 1");
        QueueEntry queueEntry = queueEntryMapper.selectOne(wrapper);

        if (queueEntry == null) {
            return null;
        }

        return QueuePositionVO.from(
                queueEntry.getId(),
                queueEntry.getDoctorId(),
                null, // 医生名称需要单独查询
                queueEntry.getPosition(),
                queueEntry.getEstimatedWaitMinutes(),
                queueEntry.getStatus()
        );
    }

    @Override
    public List getOnlineDoctors() {
        // 获取所有在线医生（从Redis key pattern获取）
        Set<String> keys = redisTemplate.keys("doctor:online:*");
        // 返回医生列表，需要关联查询医生信息
        return null; // 简化，后续实现
    }

    @Override
    public void cleanTimeoutQueue() {
        // 清理超时未应答的排队记录
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(QUEUE_TIMEOUT_MINUTES);
        LambdaQueryWrapper<QueueEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueueEntry::getStatus, "WAITING")
               .lt(QueueEntry::getCreateTime, timeout);
        List<QueueEntry> timeoutEntries = queueEntryMapper.selectList(wrapper);

        for (QueueEntry entry : timeoutEntries) {
            // 从Redis移除
            String queueKey = QUEUE_DOCTOR_KEY + entry.getDoctorId();
            redisTemplate.opsForList().remove(queueKey, 1, entry.getPatientId().toString());

            // 更新状态
            entry.setStatus("TIMEOUT");
            entry.setUpdateTime(LocalDateTime.now());
            queueEntryMapper.updateById(entry);

            // 通知患者
            messagingTemplate.convertAndSend("/topic/queue." + entry.getPatientId() + ".timeout", "timeout");
        }
    }

    private int calculateEstimatedWait(Long doctorId, Long currentQueueSize) {
        // 假设每个患者问诊平均5分钟，医生最多3个同时问诊
        int avgConsultMinutes = 5;
        int maxConcurrent = 3;
        if (currentQueueSize == null || currentQueueSize == 0) {
            return 0;
        }
        return (int) (currentQueueSize * avgConsultMinutes / maxConcurrent);
    }

    private void broadcastQueuePosition(Long doctorId) {
        String queueKey = QUEUE_DOCTOR_KEY + doctorId;
        Long size = redisTemplate.opsForList().size(queueKey);

        // 通知排队中的患者更新位置
        List<Object> patientObjs = redisTemplate.opsForList().range(queueKey, 0, -1);
        if (patientObjs != null) {
            for (int i = 0; i < patientObjs.size(); i++) {
                Long patientId = Long.parseLong(patientObjs.get(i).toString());
                QueuePositionVO position = new QueuePositionVO();
                position.setPosition(i + 1);
                position.setEstimatedWaitMinutes(calculateEstimatedWait(doctorId, (long) i));
                messagingTemplate.convertAndSend("/topic/queue." + patientId, position);
            }
        }
    }

    private void broadcastQueueList(Long doctorId) {
        String queueKey = QUEUE_DOCTOR_KEY + doctorId;
        List<Object> patientObjs = redisTemplate.opsForList().range(queueKey, 0, -1);
        if (patientObjs != null) {
            List<Map<String, Object>> patients = new java.util.ArrayList<>();
            for (int i = 0; i < patientObjs.size(); i++) {
                Long patientId = Long.parseLong(patientObjs.get(i).toString());
                Map<String, Object> patient = new java.util.HashMap<>();
                patient.put("id", i + 1);
                patient.put("patientId", patientId);
                patient.put("waitMinutes", calculateEstimatedWait(doctorId, (long) i));

                // 从数据库查询sessionId
                LambdaQueryWrapper<QueueEntry> qw = new LambdaQueryWrapper<>();
                qw.eq(QueueEntry::getPatientId, patientId)
                   .eq(QueueEntry::getDoctorId, doctorId)
                   .eq(QueueEntry::getStatus, "WAITING")
                   .orderByDesc(QueueEntry::getCreateTime)
                   .last("LIMIT 1");
                QueueEntry entry = queueEntryMapper.selectOne(qw);
                if (entry != null) {
                    patient.put("sessionId", entry.getSessionId());
                }

                patients.add(patient);
            }
            messagingTemplate.convertAndSend("/topic/doctor.queue." + doctorId, patients);
        }
    }
}