package com.atguigu.java.ai.langchain4j.tools;

import com.atguigu.java.ai.langchain4j.entity.Appointment;
import com.atguigu.java.ai.langchain4j.entity.Doctor;
import com.atguigu.java.ai.langchain4j.mapper.DoctorMapper;
import com.atguigu.java.ai.langchain4j.service.AppointmentService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppointmentTools {
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorMapper doctorMapper;

    @Tool(name = "查询医生列表", value = "查询数据库中可用的医生列表，返回医生姓名、职称、科室和擅长领域")
    public String queryDoctors(
            @P(value = "科室名称，可为空") String department,
            @P(value = "擅长领域，可为空", required = false) String expertise
    ) {
        System.out.println("查询医生列表 - 科室:" + department + ", 擅长:" + expertise);

        List<Doctor> doctors = doctorMapper.selectList(null);

        if (doctors.isEmpty()) {
            return "暂无医生信息";
        }

        StringBuilder sb = new StringBuilder("可挂号医生：\n");
        for (Doctor doctor : doctors) {
            sb.append("- ").append(doctor.getName())
              .append(" (").append(doctor.getTitle()).append(")")
              .append(" | 科室：").append(doctor.getDepartment())
              .append(" | 擅长：").append(doctor.getExpertise())
              .append("\n");
        }
        return sb.toString();
    }

    @Tool(name = "查询是否有号源", value = "根据科室名称，日期，时间查询是否有号源，并返回给用户")
    public boolean queryDepartment(
            @P(value = "科室名称") String name,
            @P(value = "日期") String date,
            @P(value = "时间，可选值：上午、下午") String time,
            @P(value = "医生名称", required = false) String doctorName
    ) {
        System.out.println("查询是否有号源");
        System.out.println("科室名称：" + name);
        System.out.println("日期：" + date);
        System.out.println("时间：" + time);
        System.out.println("医生名称：" + doctorName);

        // TODO: 维护医生的排班信息
        // 暂时返回true模拟有号源
        return true;
    }

    @Tool(name = "预约挂号", value = "根据参数，先查询是否有号源，确认后再执行预约。预约成功返回预约详情。")
    public String bookAppointment(Appointment appointment) {
        // 查找数据库中是否包含对应的预约记录
        Appointment appointmentDB = appointmentService.getOne(appointment);

        if (appointmentDB == null) {
            appointment.setId(null);
            if (appointmentService.save(appointment)) {
                return "预约成功！\n预约信息：\n科室：" + appointment.getDepartment()
                    + "\n日期：" + appointment.getDate()
                    + "\n时间：" + appointment.getTime()
                    + "\n医生：" + appointment.getDoctorName();
            } else {
                return "预约失败，请稍后重试";
            }
        }
        return "您在相同的科室和时间已有预约";
    }

    @Tool(name = "取消预约挂号", value = "根据参数，查询预约是否存在，如果存在则删除预约记录并返回取消预约成功，否则返回取消预约失败")
    public String cancelAppointment(Appointment appointment) {
        Appointment appointmentDB = appointmentService.getOne(appointment);
        if (appointmentDB != null) {
            if (appointmentService.removeById(appointmentDB.getId())) {
                return "取消预约成功";
            } else {
                return "取消预约失败";
            }
        }
        return "您没有预约记录，请核对预约科室和时间";
    }
}