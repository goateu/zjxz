package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.dto.WebSocketResponseDTO;
import com.atguigu.java.ai.langchain4j.service.ConsultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "问诊会话")
@RestController
@RequestMapping("/consult")
public class ConsultController {

    @Autowired
    private ConsultService consultService;

    @Operation(summary = "创建问诊会话")
    @PostMapping("/create")
    public WebSocketResponseDTO createSession(
            @RequestParam("patientId") Long patientId,
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam("type") String type) {
        Long sessionId = consultService.createSession(patientId, doctorId, type);
        return WebSocketResponseDTO.success(sessionId);
    }

    @Operation(summary = "获取会话信息")
    @GetMapping("/{sessionId}")
    public WebSocketResponseDTO getSession(@PathVariable("sessionId") Long sessionId) {
        return WebSocketResponseDTO.success(consultService.getSession(sessionId));
    }

    @Operation(summary = "获取会话消息列表")
    @GetMapping("/{sessionId}/messages")
    public WebSocketResponseDTO getMessages(@PathVariable("sessionId") Long sessionId) {
        return WebSocketResponseDTO.success(consultService.getSessionMessages(sessionId));
    }

    @Operation(summary = "结束问诊")
    @PostMapping("/end/{sessionId}")
    public WebSocketResponseDTO endConsult(@PathVariable("sessionId") Long sessionId) {
        consultService.endConsult(sessionId);
        return WebSocketResponseDTO.success(null);
    }

    @Operation(summary = "获取医生今日完成问诊数")
    @GetMapping("/todayCount/{doctorId}")
    public WebSocketResponseDTO getTodayCompletedCount(@PathVariable("doctorId") Long doctorId) {
        return WebSocketResponseDTO.success(consultService.getTodayCompletedCount(doctorId));
    }
}