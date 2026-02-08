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
    private String taskName;
    private Long estimatedDurationMinutes;

    public static SessionResponse from(WorkSession session) {
        SessionResponse response = new SessionResponse();
        response.setSessionId(session.getId());
        response.setUserId(session.getUserId());
        response.setFirstName(null); // to be populated by service
        response.setLastName(null); // to be populated by service
        response.setJobRole(null); // to be populated by service
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setStatus(session.getStatus());
        response.setTaskName(session.getTaskName());
        response.setEstimatedDurationMinutes(session.getEstimatedDurationMinutes());
        return response;
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Long estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }
}
