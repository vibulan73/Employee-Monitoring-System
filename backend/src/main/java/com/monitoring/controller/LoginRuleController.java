package com.monitoring.controller;

import com.monitoring.dto.LoginRuleRequest;
import com.monitoring.dto.LoginRuleResponse;
import com.monitoring.service.LoginRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/login-rules")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class LoginRuleController {

    private final LoginRuleService loginRuleService;

    /**
     * Get all login rules.
     */
    @GetMapping
    public ResponseEntity<List<LoginRuleResponse>> getAllLoginRules() {
        List<LoginRuleResponse> rules = loginRuleService.getAllLoginRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * Get a specific login rule by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLoginRuleById(@PathVariable Long id) {
        try {
            LoginRuleResponse rule = loginRuleService.getLoginRuleById(id);
            return ResponseEntity.ok(rule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Create a new login rule.
     */
    @PostMapping
    public ResponseEntity<?> createLoginRule(@Valid @RequestBody LoginRuleRequest request) {
        try {
            LoginRuleResponse createdRule = loginRuleService.createLoginRule(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Update an existing login rule.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLoginRule(
            @PathVariable Long id,
            @Valid @RequestBody LoginRuleRequest request) {
        try {
            LoginRuleResponse updatedRule = loginRuleService.updateLoginRule(id, request);
            return ResponseEntity.ok(updatedRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete a login rule.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLoginRule(@PathVariable Long id) {
        try {
            loginRuleService.deleteLoginRule(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Inner class for error responses
    private record ErrorResponse(String message) {
    }
}
