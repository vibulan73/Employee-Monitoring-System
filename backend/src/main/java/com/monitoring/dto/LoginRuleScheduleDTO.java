package com.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRuleScheduleDTO {

    private Long id;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;

    public static LoginRuleScheduleDTO from(com.monitoring.entity.LoginRuleSchedule schedule) {
        LoginRuleScheduleDTO dto = new LoginRuleScheduleDTO();
        dto.setId(schedule.getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setIsActive(schedule.getIsActive());
        return dto;
    }
}
