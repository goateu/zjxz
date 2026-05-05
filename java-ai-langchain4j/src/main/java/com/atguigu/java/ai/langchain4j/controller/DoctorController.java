package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.dto.DoctorLoginResponseDTO;
import com.atguigu.java.ai.langchain4j.dto.WebSocketResponseDTO;
import com.atguigu.java.ai.langchain4j.entity.Doctor;
import com.atguigu.java.ai.langchain4j.entity.DoctorUser;
import com.atguigu.java.ai.langchain4j.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "医生管理")
@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Operation(summary = "医生登录")
    @PostMapping("/login")
    public WebSocketResponseDTO login(@RequestParam("username") String username, @RequestParam("password") String password) {
        DoctorUser doctorUser = doctorService.login(username, password);
        if (doctorUser != null) {
            Doctor doctor = doctorService.getById(doctorUser.getDoctorId());
            return WebSocketResponseDTO.success(DoctorLoginResponseDTO.success(doctorUser, doctor));
        }
        return WebSocketResponseDTO.error("用户名或密码错误");
    }

    @Operation(summary = "医生下线")
    @PostMapping("/logout")
    public WebSocketResponseDTO logout(@RequestParam("doctorId") Long doctorId) {
        doctorService.updateDoctorStatus(doctorId, "OFFLINE");
        return WebSocketResponseDTO.success(null);
    }

    @Operation(summary = "医生心跳")
    @PostMapping("/heartbeat")
    public WebSocketResponseDTO heartbeat(@RequestParam("doctorId") Long doctorId) {
        doctorService.heartbeat(doctorId);
        return WebSocketResponseDTO.success(null);
    }

    @Operation(summary = "获取在线医生列表")
    @GetMapping("/list")
    public WebSocketResponseDTO getOnlineDoctors() {
        return WebSocketResponseDTO.success(doctorService.getOnlineDoctors());
    }
}