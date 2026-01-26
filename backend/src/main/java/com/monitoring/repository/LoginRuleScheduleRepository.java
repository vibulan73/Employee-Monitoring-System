package com.monitoring.repository;

import com.monitoring.entity.LoginRule;
import com.monitoring.entity.LoginRuleSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginRuleScheduleRepository extends JpaRepository<LoginRuleSchedule, Long> {

    /**
     * Find all active schedules for a specific login rule.
     */
    List<LoginRuleSchedule> findByLoginRuleAndIsActiveTrue(LoginRule loginRule);

    /**
     * Find schedules for a specific rule and day of week.
     */
    List<LoginRuleSchedule> findByLoginRuleIdAndDayOfWeek(Long loginRuleId, String dayOfWeek);

    /**
     * Find all schedules for a specific rule (including inactive).
     */
    List<LoginRuleSchedule> findByLoginRule(LoginRule loginRule);

    /**
     * Delete all schedules for a specific login rule.
     * (This will be cascaded automatically by the LoginRule entity)
     */
    void deleteByLoginRule(LoginRule loginRule);
}
