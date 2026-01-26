package com.monitoring.service;

import com.monitoring.dto.LoginRuleRequest;
import com.monitoring.dto.LoginRuleResponse;
import com.monitoring.entity.LoginRule;
import com.monitoring.entity.LoginRuleSchedule;
import com.monitoring.entity.User;
import com.monitoring.repository.LoginRuleRepository;
import com.monitoring.repository.LoginRuleScheduleRepository;
import com.monitoring.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginRuleService {

    private final LoginRuleRepository loginRuleRepository;
    private final LoginRuleScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Value("${app.timezone:UTC}")
    private String timeZoneId;

    private ZoneId getApplicationZoneId() {
        return ZoneId.of(timeZoneId);
    }

    /**
     * Initialize default login rule on application startup.
     */
    @PostConstruct
    public void initializeDefaultRule() {
        if (loginRuleRepository.findByIsDefaultTrue().isEmpty()) {
            LoginRule defaultRule = new LoginRule();
            defaultRule.setRuleName("Unrestricted Access");
            defaultRule.setRuleType(LoginRule.RuleType.ALL_DAYS);
            defaultRule.setDescription("Default rule - tracking allowed at any time");
            defaultRule.setIsDefault(true);
            loginRuleRepository.save(defaultRule);
            log.info("Created default login rule: Unrestricted Access");
        }
    }

    /**
     * Get or create the default ALL_DAYS rule.
     */
    public LoginRule getOrCreateDefaultRule() {
        return loginRuleRepository.findByIsDefaultTrue()
                .orElseGet(() -> {
                    LoginRule defaultRule = new LoginRule();
                    defaultRule.setRuleName("Unrestricted Access");
                    defaultRule.setRuleType(LoginRule.RuleType.ALL_DAYS);
                    defaultRule.setDescription("Default rule - tracking allowed at any time");
                    defaultRule.setIsDefault(true);
                    return loginRuleRepository.save(defaultRule);
                });
    }

    /**
     * Main method to check if tracking is allowed for a user right now.
     */
    public boolean isTrackingAllowed(User user) {
        if (user.getLoginRule() == null) {
            log.warn("User {} has no login rule assigned, defaulting to allow", user.getUserId());
            return true; // Fail-safe: allow if no rule
        }

        LoginRule rule = user.getLoginRule();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        return evaluateRule(rule, now);
    }

    /**
     * Get a human-readable message about when tracking will next be allowed.
     */
    public String getNextAllowedWindow(User user) {
        if (user.getLoginRule() == null) {
            return "No restrictions apply.";
        }

        LoginRule rule = user.getLoginRule();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        if (rule.getRuleType() == LoginRule.RuleType.ALL_DAYS) {
            return "You can track at any time.";
        }

        // Try to find next allowed window
        // Filter only active schedules
        List<LoginRuleSchedule> schedules = rule.getSchedules().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .collect(Collectors.toList());

        if (schedules.isEmpty()) {
            return "No tracking windows are configured. Contact your administrator.";
        }

        // Check next 7 days for an allowed window
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            ZonedDateTime checkDate = now.plusDays(dayOffset);
            String dayOfWeek = checkDate.getDayOfWeek().name();

            List<LoginRuleSchedule> daySchedules = schedules.stream()
                    .filter(s -> s.getDayOfWeek().equals(dayOfWeek) || s.getDayOfWeek().equals("ALL"))
                    .collect(Collectors.toList());

            for (LoginRuleSchedule schedule : daySchedules) {
                LocalTime startTime = schedule.getStartTime();

                if (startTime == null) {
                    // All day schedule
                    if (dayOffset == 0) {
                        return "You can track today at any time.";
                    } else {
                        return String.format("Next allowed: %s at any time",
                                checkDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                    }
                }

                // For same day, only consider future times
                if (dayOffset == 0 && checkDate.toLocalTime().isBefore(startTime)) {
                    return String.format("Next allowed: Today at %s", startTime);
                } else if (dayOffset > 0) {
                    return String.format("Next allowed: %s at %s",
                            checkDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                            startTime);
                }
            }
        }

        return "No upcoming tracking windows found in the next 7 days.";
    }

    /**
     * Core rule evaluation logic.
     */
    private boolean evaluateRule(LoginRule rule, ZonedDateTime now) {
        switch (rule.getRuleType()) {
            case ALL_DAYS:
                return true;

            case ALL_DAYS_WITH_TIME:
                return evaluateAllDaysWithTime(rule, now.toLocalTime());

            case DAY_ANY_TIME:
                return evaluateDayAnyTime(rule, now.getDayOfWeek().name());

            case CUSTOM:
                return evaluateCustom(rule, now.getDayOfWeek().name(), now.toLocalTime());

            default:
                log.error("Unknown rule type: {}", rule.getRuleType());
                return false;
        }
    }

    private boolean evaluateAllDaysWithTime(LoginRule rule, LocalTime currentTime) {
        return rule.getSchedules().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .anyMatch(s -> s.containsTime(currentTime));
    }

    private boolean evaluateDayAnyTime(LoginRule rule, String currentDay) {
        return rule.getSchedules().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .anyMatch(s -> s.getDayOfWeek().equals(currentDay));
    }

    private boolean evaluateCustom(LoginRule rule, String currentDay, LocalTime currentTime) {
        return rule.getSchedules().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .filter(s -> s.getDayOfWeek().equals(currentDay))
                .anyMatch(s -> s.containsTime(currentTime));
    }

    /**
     * Get all login rules for admin display.
     */
    public List<LoginRuleResponse> getAllLoginRules() {
        List<LoginRule> rules = loginRuleRepository.findAllByOrderByRuleNameAsc();
        return rules.stream()
                .map(rule -> {
                    LoginRuleResponse response = LoginRuleResponse.from(rule);
                    // Attach employee count
                    response.setAssignedEmployeeCount(userRepository.countByLoginRuleId(rule.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get a specific login rule by ID.
     */
    public LoginRuleResponse getLoginRuleById(Long id) {
        LoginRule rule = loginRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Login rule not found: " + id));

        LoginRuleResponse response = LoginRuleResponse.from(rule);
        response.setAssignedEmployeeCount(userRepository.countByLoginRuleId(rule.getId()));
        return response;
    }

    /**
     * Create a new login rule.
     */
    @Transactional
    public LoginRuleResponse createLoginRule(LoginRuleRequest request) {
        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid rule configuration for type: " + request.getRuleType());
        }

        // Check if name already exists
        if (loginRuleRepository.existsByRuleName(request.getRuleName())) {
            throw new IllegalArgumentException("A rule with this name already exists: " + request.getRuleName());
        }

        // Create rule entity
        LoginRule rule = new LoginRule();
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(LoginRule.RuleType.valueOf(request.getRuleType()));
        rule.setDescription(request.getDescription());
        rule.setIsDefault(false); // Never allow creating new default rules

        LoginRule savedRule = loginRuleRepository.save(rule);

        // Create schedules
        if (request.getSchedules() != null) {
            for (var scheduleDTO : request.getSchedules()) {
                LoginRuleSchedule schedule = new LoginRuleSchedule();
                schedule.setLoginRule(savedRule);
                schedule.setDayOfWeek(scheduleDTO.getDayOfWeek());
                schedule.setStartTime(scheduleDTO.getStartTime());
                schedule.setEndTime(scheduleDTO.getEndTime());
                schedule.setIsActive(true);
                scheduleRepository.save(schedule);
            }
        }

        log.info("Created new login rule: {} ({})", savedRule.getRuleName(), savedRule.getRuleType());
        return LoginRuleResponse.from(savedRule);
    }

    /**
     * Update an existing login rule.
     */
    @Transactional
    public LoginRuleResponse updateLoginRule(Long id, LoginRuleRequest request) {
        LoginRule rule = loginRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Login rule not found: " + id));

        // Prevent modifying default rule type
        if (rule.getIsDefault() && !request.getRuleType().equals("ALL_DAYS")) {
            throw new IllegalArgumentException("Cannot change the type of the default rule");
        }

        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid rule configuration for type: " + request.getRuleType());
        }

        // Check name uniqueness if changed
        if (!rule.getRuleName().equals(request.getRuleName())
                && loginRuleRepository.existsByRuleName(request.getRuleName())) {
            throw new IllegalArgumentException("A rule with this name already exists: " + request.getRuleName());
        }

        // Update rule
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(LoginRule.RuleType.valueOf(request.getRuleType()));
        rule.setDescription(request.getDescription());

        // Update schedules by modifying the collection directly
        // This leverages Hibernate's orphanRemoval=true
        rule.getSchedules().clear();

        if (request.getSchedules() != null) {
            for (var scheduleDTO : request.getSchedules()) {
                LoginRuleSchedule schedule = new LoginRuleSchedule();
                schedule.setDayOfWeek(scheduleDTO.getDayOfWeek());
                schedule.setStartTime(scheduleDTO.getStartTime());
                schedule.setEndTime(scheduleDTO.getEndTime());
                schedule.setIsActive(true);

                // Add to parent - helper method handles the bidirectional relationship
                rule.addSchedule(schedule);
            }
        }

        LoginRule savedRule = loginRuleRepository.save(rule);
        log.info("Updated login rule: {} ({})", savedRule.getRuleName(), savedRule.getRuleType());
        return LoginRuleResponse.from(savedRule);
    }

    /**
     * Delete a login rule.
     */
    @Transactional
    public void deleteLoginRule(Long id) {
        LoginRule rule = loginRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Login rule not found: " + id));

        // Prevent deletion of default rule
        if (rule.getIsDefault()) {
            throw new IllegalArgumentException("Cannot delete the default login rule");
        }

        // Prevent deletion if assigned to users
        long assignedCount = userRepository.countByLoginRuleId(id);
        if (assignedCount > 0) {
            throw new IllegalArgumentException(
                    String.format("Cannot delete rule '%s' because it is assigned to %d employee(s)",
                            rule.getRuleName(), assignedCount));
        }

        loginRuleRepository.delete(rule);
        log.info("Deleted login rule: {}", rule.getRuleName());
    }
}
