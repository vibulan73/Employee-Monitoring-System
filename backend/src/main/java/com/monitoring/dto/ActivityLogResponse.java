package com.monitoring.dto;

import com.monitoring.entity.ActivityLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private UUID sessionId;
    private LocalDateTime loggedAt;
    private ActivityLog.ActivityStatus activityStatus;
    private String metadata;

    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getSessionId(),
                log.getLoggedAt(),
                log.getActivityStatus(),
                log.getMetadata());
    }
}
