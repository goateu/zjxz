package com.atguigu.java.ai.langchain4j.handler;

import com.atguigu.java.ai.langchain4j.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventHandler {

    @Autowired
    private DoctorService doctorService;

    // 存储WebSocket session -> userId 的映射
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        // 从header中获取userId（前端连接时传入）
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
            String userId = sessionAttributes.get("userId").toString();
            sessionUserMap.put(sessionId, userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            // 医生下线处理
            if (userId.startsWith("doctor_")) {
                Long doctorId = Long.parseLong(userId.substring(7));
                doctorService.updateDoctorStatus(doctorId, "OFFLINE");
            }
        }
    }
}