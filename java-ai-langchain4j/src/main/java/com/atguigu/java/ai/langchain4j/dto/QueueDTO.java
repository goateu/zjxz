package com.atguigu.java.ai.langchain4j.dto;

import lombok.Data;

@Data
public class QueueDTO {
    private String type;          // QUEUE_JOIN, QUEUE_CANCEL, QUEUE_CALL, QUEUE_POSITION, QUEUE_CALLED, QUEUE_TIMEOUT
    private Long doctorId;
    private Long patientId;
    private Long sessionId;
    private Integer position;     // 排队位置
    private Integer estimatedWaitMinutes; // 预计等待时间
}