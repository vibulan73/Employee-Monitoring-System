package com.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    private String password; // Optional for updates

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Job role is required")
    private String jobRole;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private Long loginRuleId; // Optional - defaults to system default rule if not specified
}
