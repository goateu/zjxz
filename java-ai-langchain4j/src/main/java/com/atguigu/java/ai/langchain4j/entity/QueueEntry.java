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
public class QueueEntry {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;        // 医生ID
    private Long patientId;       // 患者ID
    private Long sessionId;       // 关联的会话ID
    private Integer position;     // 排队位置
    private Integer estimatedWaitMinutes; // 预计等待分钟数
    private String status;        // WAITING, CALLED, CANCEL, TIMEOUT
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}