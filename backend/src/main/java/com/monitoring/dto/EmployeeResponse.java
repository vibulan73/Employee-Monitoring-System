package com.monitoring.dto;

import com.monitoring.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String jobRole;
    private String phoneNumber;
    private String status;
    private LocalDateTime createdAt;
    private Long loginRuleId;
    private String loginRuleName;

    public static EmployeeResponse from(User user) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(user.getId());
        response.setUserId(user.getUserId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setJobRole(user.getJobRole());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());

        // Include login rule information if available
        if (user.getLoginRule() != null) {
            response.setLoginRuleId(user.getLoginRule().getId());
            response.setLoginRuleName(user.getLoginRule().getRuleName());
        }

        return response;
    }
}
