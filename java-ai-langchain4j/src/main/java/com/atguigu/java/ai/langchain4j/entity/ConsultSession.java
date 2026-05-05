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
public class ConsultSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;        // 患者ID
    private Long doctorId;        // 医生ID（AI问诊时为null）
    private String type;          // AI, CONSULT
    private String status;        // ACTIVE, WAITING, CLOSED
    private LocalDateTime createTime;
    private LocalDateTime endTime;
}