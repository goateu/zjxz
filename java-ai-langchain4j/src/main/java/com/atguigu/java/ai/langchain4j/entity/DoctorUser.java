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
public class DoctorUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private String username;
    private String password;
    private String status = "ACTIVE";
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}