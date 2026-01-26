package com.monitoring.service;

import com.monitoring.dto.AuthResponse;
import com.monitoring.dto.EmployeeRequest;
import com.monitoring.dto.EmployeeResponse;
import com.monitoring.dto.LoginRequest;
import com.monitoring.dto.SignupRequest;
import com.monitoring.entity.LoginRule;
import com.monitoring.entity.User;
import com.monitoring.repository.LoginRuleRepository;
import com.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LoginRuleRepository loginRuleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse signup(SignupRequest request) {
        // Check if user already exists
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists");
        }

        // Create new user with encrypted password
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setJobRole(request.getJobRole());
        user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userRepository.save(user);
        return AuthResponse.from(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid user ID or password");
        }

        return AuthResponse.from(user);
    }

    // Employee Management Methods

    public List<EmployeeResponse> getAllEmployees() {
        return userRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployeeById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        return EmployeeResponse.from(user);
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // Check if user ID already exists
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists: " + request.getUserId());
        }

        // Create new employee
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setJobRole(request.getJobRole());
        user.setPhoneNumber(request.getPhoneNumber());

        // Assign login rule (default rule if not specified)
        if (request.getLoginRuleId() != null) {
            LoginRule rule = loginRuleRepository.findById(request.getLoginRuleId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Login rule not found: " + request.getLoginRuleId()));
            user.setLoginRule(rule);
        } else {
            // Assign default rule
            LoginRule defaultRule = loginRuleRepository.findByIsDefaultTrue()
                    .orElseThrow(() -> new IllegalStateException("No default login rule found"));
            user.setLoginRule(defaultRule);
        }

        User savedUser = userRepository.save(user);
        return EmployeeResponse.from(savedUser);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));

        // Check if userId is being changed and if it already exists
        if (!user.getUserId().equals(request.getUserId()) &&
                userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists: " + request.getUserId());
        }

        // Update employee details
        user.setUserId(request.getUserId());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setJobRole(request.getJobRole());
        user.setPhoneNumber(request.getPhoneNumber());

        // Update login rule if specified
        if (request.getLoginRuleId() != null) {
            LoginRule rule = loginRuleRepository.findById(request.getLoginRuleId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Login rule not found: " + request.getLoginRuleId()));
            user.setLoginRule(rule);
        }

        User updatedUser = userRepository.save(user);
        return EmployeeResponse.from(updatedUser);
    }

    public void deleteEmployee(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public EmployeeResponse getEmployeeByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with userId: " + userId));
        return EmployeeResponse.from(user);
    }
}
