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
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;           // 关联系统用户ID
    private String name;           // 医生姓名
    private String title;          // 职称：主任医师、副主任医师、主治医师
    private String department;     // 科室
    private String expertise;     // 擅长领域
    private String avatar;        // 头像URL
    private Integer maxConsultCount = 3; // 最大同时接诊数
    private String status = "OFFLINE";    // ONLINE, OFFLINE, BUSY
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}