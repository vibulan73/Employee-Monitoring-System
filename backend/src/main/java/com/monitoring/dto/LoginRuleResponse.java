package com.monitoring.dto;

import com.monitoring.entity.LoginRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRuleResponse {

    private Long id;
    private String ruleName;
    private String ruleType;
    private String description;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LoginRuleScheduleDTO> schedules;
    private Long assignedEmployeeCount; // Optional, populated when needed

    public static LoginRuleResponse from(LoginRule rule) {
        LoginRuleResponse response = new LoginRuleResponse();
        response.setId(rule.getId());
        response.setRuleName(rule.getRuleName());
        response.setRuleType(rule.getRuleType().name());
        response.setDescription(rule.getDescription());
        response.setIsDefault(rule.getIsDefault());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());

        // Convert schedules to DTOs
        if (rule.getSchedules() != null) {
            response.setSchedules(
                    rule.getSchedules().stream()
                            .map(LoginRuleScheduleDTO::from)
                            .collect(Collectors.toList()));
        }

        return response;
    }
}
