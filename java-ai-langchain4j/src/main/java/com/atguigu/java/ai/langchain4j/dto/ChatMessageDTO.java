package com.atguigu.java.ai.langchain4j.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String type;          // TEXT, IMAGE, etc.
    private String content;
    private String sessionId;
    private Long senderId;
    private String senderType;    // PATIENT, DOCTOR, AI
    private String senderName;
    private Long timestamp;
}