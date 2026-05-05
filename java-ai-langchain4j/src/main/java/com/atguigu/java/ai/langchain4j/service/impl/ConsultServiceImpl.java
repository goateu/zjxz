package com.atguigu.java.ai.langchain4j.service.impl;

import com.atguigu.java.ai.langchain4j.dto.ChatMessageDTO;
import com.atguigu.java.ai.langchain4j.entity.ConsultMessage;
import com.atguigu.java.ai.langchain4j.entity.ConsultSession;
import com.atguigu.java.ai.langchain4j.mapper.ConsultMessageMapper;
import com.atguigu.java.ai.langchain4j.mapper.ConsultSessionMapper;
import com.atguigu.java.ai.langchain4j.service.ConsultService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultServiceImpl implements ConsultService {

    @Autowired
    private ConsultSessionMapper sessionMapper;

    @Autowired
    private ConsultMessageMapper messageMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void saveMessage(ChatMessageDTO message) {
        ConsultMessage consultMessage = new ConsultMessage();
        consultMessage.setSessionId(Long.parseLong(message.getSessionId()));
        consultMessage.setSenderType(message.getSenderType());
        consultMessage.setSenderId(message.getSenderId());
        consultMessage.setContent(message.getContent());
        consultMessage.setCreateTime(LocalDateTime.now());
        messageMapper.insert(consultMessage);
    }

    @Override
    public List<ChatMessageDTO> getSessionMessages(Long sessionId) {
        LambdaQueryWrapper<ConsultMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultMessage::getSessionId, sessionId)
               .orderByAsc(ConsultMessage::getCreateTime);
        List<ConsultMessage> messages = messageMapper.selectList(wrapper);

        return messages.stream().map(m -> {
            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setSessionId(m.getSessionId().toString());
            dto.setSenderType(m.getSenderType());
            dto.setSenderId(m.getSenderId());
            dto.setContent(m.getContent());
            dto.setTimestamp(java.time.LocalDateTime.now().toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void endConsult(Long sessionId) {
        // 更新会话状态
        ConsultSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setStatus("CLOSED");
            session.setEndTime(LocalDateTime.now());
            sessionMapper.updateById(session);

            // 减少医生的当前问诊数
            if (session.getDoctorId() != null) {
                String countKey = "doctor:consult:count:" + session.getDoctorId();
                redisTemplate.opsForValue().decrement(countKey);
            }
        }
    }

    @Override
    public Long createSession(Long patientId, Long doctorId, String type) {
        ConsultSession session = new ConsultSession();
        session.setPatientId(patientId);
        session.setDoctorId(doctorId);
        session.setType(type);
        session.setStatus("ACTIVE");
        session.setCreateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    public Object getSession(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    @Override
    public long getTodayCompletedCount(Long doctorId) {
        LambdaQueryWrapper<ConsultSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultSession::getDoctorId, doctorId)
               .eq(ConsultSession::getStatus, "CLOSED")
               .ge(ConsultSession::getEndTime, LocalDateTime.now().toLocalDate().atStartOfDay());
        return sessionMapper.selectCount(wrapper);
    }
}