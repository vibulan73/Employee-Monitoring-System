package com.monitoring.dto;

import com.monitoring.entity.WorkSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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

    // Manual Getters and Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public WorkSession.SessionStatus getStatus() {
        return status;
    }

    public void setStatus(WorkSession.SessionStatus status) {
        this.status = status;
    }
}
