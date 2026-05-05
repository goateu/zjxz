package com.atguigu.java.ai.langchain4j.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueuePositionVO {
    private Long queueId;
    private Long doctorId;
    private String doctorName;
    private Integer position;
    private Integer estimatedWaitMinutes;
    private String status;  // WAITING, CALLED, CANCEL, TIMEOUT

    public static QueuePositionVO from(Long queueId, Long doctorId, String doctorName,
                                       Integer position, Integer estimatedWaitMinutes, String status) {
        QueuePositionVO vo = new QueuePositionVO();
        vo.setQueueId(queueId);
        vo.setDoctorId(doctorId);
        vo.setDoctorName(doctorName);
        vo.setPosition(position);
        vo.setEstimatedWaitMinutes(estimatedWaitMinutes);
        vo.setStatus(status);
        return vo;
    }
}