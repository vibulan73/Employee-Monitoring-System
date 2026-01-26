package com.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "login_rule_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRuleSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_rule_id", nullable = false)
    private LoginRule loginRule;

    /**
     * Day of week: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
     * Special value: "ALL" for ALL_DAYS_WITH_TIME rule type
     */
    @Column(nullable = false, length = 10)
    private String dayOfWeek;

    /**
     * Start time for the allowed tracking window.
     * Nullable for DAY_ANY_TIME rule type (indicates all day).
     */
    @Column
    private LocalTime startTime;

    /**
     * End time for the allowed tracking window.
     * Nullable for DAY_ANY_TIME rule type (indicates all day).
     * For overnight shifts (e.g., 22:00-06:00), this should be split into two
     * schedules:
     * - Day 1: 22:00-23:59
     * - Day 2: 00:00-06:00
     */
    @Column
    private LocalTime endTime;

    /**
     * Allows disabling specific schedules without deletion.
     * Useful for temporary schedule changes.
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    // Validation helper
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // Valid for DAY_ANY_TIME
        }
        // For same-day schedules, startTime should be before endTime
        // We don't allow overnight in a single schedule (must be split)
        return startTime.isBefore(endTime);
    }

    // Check if a given time falls within this schedule
    public boolean containsTime(LocalTime time) {
        if (startTime == null || endTime == null) {
            return true; // All day schedule
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}
