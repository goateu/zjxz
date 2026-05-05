package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.dto.ChatMessageDTO;
import com.atguigu.java.ai.langchain4j.dto.QueueDTO;
import com.atguigu.java.ai.langchain4j.dto.WebSocketResponseDTO;
import com.atguigu.java.ai.langchain4j.service.QueueService;
import com.atguigu.java.ai.langchain4j.service.ConsultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private QueueService queueService;

    @Autowired
    private ConsultService consultService;

    /**
     * 处理聊天消息
     */
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDTO message) {
        // 消息落库
        consultService.saveMessage(message);
        // 广播到会话
        messagingTemplate.convertAndSend("/topic/consult." + message.getSessionId(), message);
    }

    /**
     * 患者加入排队队列
     */
    @MessageMapping("/queue.join")
    public void joinQueue(QueueDTO queueDTO) {
        queueService.joinQueue(queueDTO);
    }

    /**
     * 患者取消排队
     */
    @MessageMapping("/queue.cancel")
    public void cancelQueue(QueueDTO queueDTO) {
        queueService.cancelQueue(queueDTO);
    }

    /**
     * 医生叫号
     */
    @MessageMapping("/queue.call")
    public void callPatient(QueueDTO queueDTO) {
        queueService.callPatient(queueDTO);
    }

    /**
     * 结束问诊
     */
    @MessageMapping("/consult.end")
    public void endConsult(ChatMessageDTO message) {
        Long sessionId = Long.parseLong(message.getSessionId());
        consultService.endConsult(sessionId);
        messagingTemplate.convertAndSend("/topic/consult." + sessionId + "/end", "closed");
    }
}