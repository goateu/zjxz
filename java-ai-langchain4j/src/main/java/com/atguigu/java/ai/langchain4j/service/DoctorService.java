package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.entity.Doctor;
import com.atguigu.java.ai.langchain4j.entity.DoctorUser;
import com.atguigu.java.ai.langchain4j.vo.DoctorVO;

import java.util.List;

public interface DoctorService {

    /**
     * 医生登录（验证用户名密码）
     */
    DoctorUser login(String username, String password);

    /**
     * 更新医生状态
     */
    void updateDoctorStatus(Long doctorId, String status);

    /**
     * 获取在线医生列表
     */
    List<DoctorVO> getOnlineDoctors();

    /**
     * 根据ID获取医生
     */
    Doctor getById(Long doctorId);

    /**
     * 获取医生当前接诊数
     */
    int getCurrentConsultCount(Long doctorId);

    /**
     * 医生发送心跳
     */
    void heartbeat(Long doctorId);

    /**
     * 根据医生用户ID获取医生信息
     */
    Doctor getDoctorByDoctorUserId(Long doctorUserId);
}