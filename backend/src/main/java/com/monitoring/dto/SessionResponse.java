package com.monitoring.dto;

import com.monitoring.entity.WorkSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private UUID sessionId;
    private String userId;
    private String firstName;
    private String lastName;
    private String jobRole;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private WorkSession.SessionStatus status;

    public static SessionResponse from(WorkSession session) {
        return new SessionResponse(
                session.getId(),
                session.getUserId(),
                null, // firstName - to be populated by service
                null, // lastName - to be populated by service
                null, // jobRole - to be populated by service
                session.getStartTime(),
                session.getEndTime(),
                session.getStatus());
    }
}
