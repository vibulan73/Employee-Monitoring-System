package com.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartSessionRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
}
