package com.atguigu.java.ai.langchain4j.dto;

import lombok.Data;

@Data
public class WebSocketResponseDTO {
    private String code;          // 0 成功, -1 失败
    private String message;
    private Object data;

    public static WebSocketResponseDTO success(Object data) {
        WebSocketResponseDTO resp = new WebSocketResponseDTO();
        resp.setCode("0");
        resp.setMessage("success");
        resp.setData(data);
        return resp;
    }

    public static WebSocketResponseDTO error(String message) {
        WebSocketResponseDTO resp = new WebSocketResponseDTO();
        resp.setCode("-1");
        resp.setMessage(message);
        return resp;
    }
}