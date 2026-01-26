package com.monitoring.dto;

import com.monitoring.entity.ActivityLog;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class LogActivityRequest {
    @NotNull(message = "Session ID is required")
    private UUID sessionId;

    @NotNull(message = "Activity status is required")
    private ActivityLog.ActivityStatus activityStatus;

    private String metadata;
}
