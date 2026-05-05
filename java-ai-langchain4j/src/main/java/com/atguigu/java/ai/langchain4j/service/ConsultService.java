package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.dto.ChatMessageDTO;

import java.util.List;

public interface ConsultService {

    /**
     * 保存消息
     */
    void saveMessage(ChatMessageDTO message);

    /**
     * 获取会话消息历史
     */
    List<ChatMessageDTO> getSessionMessages(Long sessionId);

    /**
     * 结束问诊
     */
    void endConsult(Long sessionId);

    /**
     * 创建问诊会话
     */
    Long createSession(Long patientId, Long doctorId, String type);

    /**
     * 获取会话信息
     */
    Object getSession(Long sessionId);

    /**
     * 获取医生今日完成问诊数
     */
    long getTodayCompletedCount(Long doctorId);
}