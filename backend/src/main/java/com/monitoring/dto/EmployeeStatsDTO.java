package com.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatsDTO {
    private long totalWorkingDays;
    private double totalWorkingHours;
    private double totalActiveHours;
    private double totalIdleHours;
    private String loginRuleName;
    private List<SessionResponse> recentSessions;

    // Manual Getters and Setters
    public long getTotalWorkingDays() {
        return totalWorkingDays;
    }

    public void setTotalWorkingDays(long totalWorkingDays) {
        this.totalWorkingDays = totalWorkingDays;
    }

    public double getTotalWorkingHours() {
        return totalWorkingHours;
    }

    public void setTotalWorkingHours(double totalWorkingHours) {
        this.totalWorkingHours = totalWorkingHours;
    }

    public double getTotalActiveHours() {
        return totalActiveHours;
    }

    public void setTotalActiveHours(double totalActiveHours) {
        this.totalActiveHours = totalActiveHours;
    }

    public double getTotalIdleHours() {
        return totalIdleHours;
    }

    public void setTotalIdleHours(double totalIdleHours) {
        this.totalIdleHours = totalIdleHours;
    }

    public String getLoginRuleName() {
        return loginRuleName;
    }

    public void setLoginRuleName(String loginRuleName) {
        this.loginRuleName = loginRuleName;
    }

    public List<SessionResponse> getRecentSessions() {
        return recentSessions;
    }

    public void setRecentSessions(List<SessionResponse> recentSessions) {
        this.recentSessions = recentSessions;
    }
}
