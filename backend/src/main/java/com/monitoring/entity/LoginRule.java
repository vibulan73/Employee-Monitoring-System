package com.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "login_rules")
@NoArgsConstructor
@AllArgsConstructor
public class LoginRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loginRule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoginRuleSchedule> schedules = new ArrayList<>();

    public enum RuleType {
        ALL_DAYS, // No restrictions - tracking allowed anytime
        ALL_DAYS_WITH_TIME, // Fixed time range every day (e.g., 9 AM - 5 PM daily)
        DAY_ANY_TIME, // Specific days only, any time on those days
        CUSTOM // Specific days with specific time ranges
    }

    // Helper method to add a schedule
    public void addSchedule(LoginRuleSchedule schedule) {
        schedules.add(schedule);
        schedule.setLoginRule(this);
    }

    // Helper method to remove a schedule
    public void removeSchedule(LoginRuleSchedule schedule) {
        schedules.remove(schedule);
        schedule.setLoginRule(null);
    }

    // Manual Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<LoginRuleSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<LoginRuleSchedule> schedules) {
        this.schedules = schedules;
    }
}
