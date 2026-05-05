package com.atguigu.java.ai.langchain4j.vo;

import com.atguigu.java.ai.langchain4j.entity.Doctor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorVO {
    private Long id;
    private String name;
    private String title;
    private String department;
    private String expertise;
    private String avatar;
    private String status;       // ONLINE, OFFLINE, BUSY
    private Integer currentConsultCount; // 当前接诊数

    public static DoctorVO fromEntity(Doctor doctor, Integer currentConsultCount) {
        DoctorVO vo = new DoctorVO();
        vo.setId(doctor.getId());
        vo.setName(doctor.getName());
        vo.setTitle(doctor.getTitle());
        vo.setDepartment(doctor.getDepartment());
        vo.setExpertise(doctor.getExpertise());
        vo.setAvatar(doctor.getAvatar());
        vo.setStatus(doctor.getStatus());
        vo.setCurrentConsultCount(currentConsultCount);
        return vo;
    }
}