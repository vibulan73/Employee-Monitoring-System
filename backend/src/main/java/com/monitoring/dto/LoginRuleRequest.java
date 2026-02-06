package com.monitoring.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class LoginRuleRequest {

    @NotBlank(message = "Rule name is required")
    private String ruleName;

    @NotBlank(message = "Rule type is required")
    private String ruleType; // ALL_DAYS, ALL_DAYS_WITH_TIME, DAY_ANY_TIME, CUSTOM

    private String description;

    @Valid
    private List<LoginRuleScheduleDTO> schedules;

    // Validation method to ensure rule type matches schedule requirements
    public boolean isValid() {
        if (ruleType == null)
            return false;

        switch (ruleType) {
            case "ALL_DAYS":
                // No schedules needed
                return schedules == null || schedules.isEmpty();

            case "ALL_DAYS_WITH_TIME":
                // Should have exactly one schedule with dayOfWeek = "ALL"
                return schedules != null && schedules.size() == 1
                        && "ALL".equals(schedules.get(0).getDayOfWeek())
                        && schedules.get(0).getStartTime() != null
                        && schedules.get(0).getEndTime() != null;

            case "DAY_ANY_TIME":
                // Should have at least one schedule with dayOfWeek set but no times
                return schedules != null && !schedules.isEmpty()
                        && schedules.stream().allMatch(s -> s.getDayOfWeek() != null
                                && s.getStartTime() == null
                                && s.getEndTime() == null);

            case "CUSTOM":
                // Should have at least one schedule with both day and times
                return schedules != null && !schedules.isEmpty()
                        && schedules.stream().allMatch(s -> s.getDayOfWeek() != null
                                && s.getStartTime() != null
                                && s.getEndTime() != null);

            default:
                return false;
        }
    }

    // Manual Getters and Setters
    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LoginRuleScheduleDTO> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<LoginRuleScheduleDTO> schedules) {
        this.schedules = schedules;
    }
}
