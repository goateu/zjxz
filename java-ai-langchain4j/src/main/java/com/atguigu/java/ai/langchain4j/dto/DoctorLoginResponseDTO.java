package com.atguigu.java.ai.langchain4j.dto;

import com.atguigu.java.ai.langchain4j.entity.Doctor;
import com.atguigu.java.ai.langchain4j.entity.DoctorUser;
import lombok.Data;

@Data
public class DoctorLoginResponseDTO {
    private DoctorUser doctorUser;
    private Doctor doctor;

    public static DoctorLoginResponseDTO success(DoctorUser doctorUser, Doctor doctor) {
        DoctorLoginResponseDTO resp = new DoctorLoginResponseDTO();
        resp.setDoctorUser(doctorUser);
        resp.setDoctor(doctor);
        return resp;
    }
}