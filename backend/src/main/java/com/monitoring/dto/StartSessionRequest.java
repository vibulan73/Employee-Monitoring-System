package com.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartSessionRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Task name is required")
    private String taskName;

    private Long estimatedDurationMinutes;
}
