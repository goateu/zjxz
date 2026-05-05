package com.atguigu.java.ai.langchain4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;       // 会话ID
    private String senderType;    // PATIENT, DOCTOR, AI
    private Long senderId;       // 发送者ID
    private String content;       // 消息内容
    private LocalDateTime createTime;
}