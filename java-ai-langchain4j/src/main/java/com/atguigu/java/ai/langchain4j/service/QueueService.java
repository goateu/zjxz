package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.dto.QueueDTO;
import com.atguigu.java.ai.langchain4j.vo.QueuePositionVO;

import java.util.List;

public interface QueueService {

    /**
     * 患者加入排队队列
     */
    void joinQueue(QueueDTO queueDTO);

    /**
     * 患者取消排队
     */
    void cancelQueue(QueueDTO queueDTO);

    /**
     * 医生叫号
     */
    void callPatient(QueueDTO queueDTO);

    /**
     * 获取患者的排队位置
     */
    QueuePositionVO getQueuePosition(Long patientId);

    /**
     * 获取在线医生列表
     */
    List getOnlineDoctors();

    /**
     * 清理超时的排队记录
     */
    void cleanTimeoutQueue();
}