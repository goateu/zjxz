package com.atguigu.java.ai.langchain4j.service.impl;

import com.atguigu.java.ai.langchain4j.entity.Doctor;
import com.atguigu.java.ai.langchain4j.entity.DoctorUser;
import com.atguigu.java.ai.langchain4j.mapper.DoctorMapper;
import com.atguigu.java.ai.langchain4j.mapper.DoctorUserMapper;
import com.atguigu.java.ai.langchain4j.service.DoctorService;
import com.atguigu.java.ai.langchain4j.vo.DoctorVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    private static final String DOCTOR_ONLINE_KEY = "doctor:online:";
    private static final Duration HEARTBEAT_TTL = Duration.ofSeconds(60);

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DoctorUserMapper doctorUserMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public DoctorUser login(String username, String password) {
        // 查询医生用户表，验证用户名密码
        LambdaQueryWrapper<DoctorUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorUser::getUsername, username)
               .eq(DoctorUser::getPassword, password);
        DoctorUser doctorUser = doctorUserMapper.selectOne(wrapper);

        if (doctorUser != null) {
            // 获取关联的医生信息，更新在线状态
            Doctor doctor = doctorMapper.selectById(doctorUser.getDoctorId());
            if (doctor != null) {
                updateDoctorStatus(doctor.getId(), "ONLINE");
            }
        }
        return doctorUser;
    }

    @Override
    public void updateDoctorStatus(Long doctorId, String status) {
        String key = DOCTOR_ONLINE_KEY + doctorId;
        if ("ONLINE".equals(status)) {
            redisTemplate.opsForValue().set(key, status, HEARTBEAT_TTL);
        } else {
            redisTemplate.delete(key);
        }
        // 同时更新数据库
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setStatus(status);
        doctorMapper.updateById(doctor);
    }

    @Override
    public List<DoctorVO> getOnlineDoctors() {
        List<Doctor> doctors = doctorMapper.selectList(null);
        return doctors.stream()
                .filter(d -> "ONLINE".equals(d.getStatus()) || "BUSY".equals(d.getStatus()))
                .map(d -> DoctorVO.fromEntity(d, getCurrentConsultCount(d.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public Doctor getById(Long doctorId) {
        return doctorMapper.selectById(doctorId);
    }

    @Override
    public int getCurrentConsultCount(Long doctorId) {
        // 从会话表统计当前问诊中的数量
        String countKey = "doctor:consult:count:" + doctorId;
        Object count = redisTemplate.opsForValue().get(countKey);
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }

    @Override
    public void heartbeat(Long doctorId) {
        String key = DOCTOR_ONLINE_KEY + doctorId;
        redisTemplate.expire(key, HEARTBEAT_TTL);
    }

    @Override
    public Doctor getDoctorByDoctorUserId(Long doctorUserId) {
        DoctorUser doctorUser = doctorUserMapper.selectById(doctorUserId);
        if (doctorUser != null) {
            return doctorMapper.selectById(doctorUser.getDoctorId());
        }
        return null;
    }
}