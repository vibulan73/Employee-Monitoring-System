package com.monitoring.repository;

import com.monitoring.entity.LoginRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginRuleRepository extends JpaRepository<LoginRule, Long> {

    /**
     * Find the default login rule (should be only one).
     */
    Optional<LoginRule> findByIsDefaultTrue();

    /**
     * Check if a rule with the given name exists.
     */
    boolean existsByRuleName(String ruleName);

    /**
     * Find a rule by exact name.
     */
    Optional<LoginRule> findByRuleName(String ruleName);

    /**
     * Get all rules ordered by name for admin display.
     */
    List<LoginRule> findAllByOrderByRuleNameAsc();

    /**
     * Count how many users are assigned to this rule.
     * This will be used via UserRepository, but defined here for clarity.
     */
    // The actual count will be done via UserRepository.countByLoginRuleId(Long
    // ruleId)
}
