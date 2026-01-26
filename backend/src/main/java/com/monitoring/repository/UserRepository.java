package com.monitoring.repository;

import com.monitoring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    /**
     * Count how many employees are assigned to a specific login rule.
     * Used to prevent deletion of rules that are in use.
     */
    long countByLoginRuleId(Long loginRuleId);
}
