package com.monitoring.controller;

import com.monitoring.dto.EmployeeListResponse;
import com.monitoring.dto.EmployeeRequest;
import com.monitoring.dto.EmployeeResponse;
import com.monitoring.dto.LoginRuleResponse;
import com.monitoring.dto.WebSocketEventDTO;
import com.monitoring.dto.WebSocketEventDTO.EventType;
import com.monitoring.service.LoginRuleService;
import com.monitoring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class EmployeeController {

    private final UserService userService;
    private final LoginRuleService loginRuleService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<EmployeeListResponse> getAllEmployees() {
        List<EmployeeResponse> employees = userService.getAllEmployees();
        return ResponseEntity.ok(EmployeeListResponse.from(employees));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        try {
            EmployeeResponse employee = userService.getEmployeeById(id);
            return ResponseEntity.ok(employee);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        try {
            EmployeeResponse employee = userService.createEmployee(request);

            // Broadcast employee creation event via WebSocket
            WebSocketEventDTO event = WebSocketEventDTO.of(EventType.EMPLOYEE_CREATED, employee);
            messagingTemplate.convertAndSend("/topic/employees", event);

            return ResponseEntity.status(HttpStatus.CREATED).body(employee);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        try {
            EmployeeResponse employee = userService.updateEmployee(id, request);

            // Broadcast employee update event via WebSocket
            WebSocketEventDTO event = WebSocketEventDTO.of(EventType.EMPLOYEE_UPDATED, employee);
            messagingTemplate.convertAndSend("/topic/employees", event);

            return ResponseEntity.ok(employee);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        try {
            userService.deleteEmployee(id);

            // Broadcast employee deletion event via WebSocket
            WebSocketEventDTO event = WebSocketEventDTO.of(EventType.SESSION_STOPPED, id);
            messagingTemplate.convertAndSend("/topic/employees", event);

            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get current employee's assigned login rule (read-only for employees).
     * Employees can view their tracking restrictions but cannot modify them.
     */
    @GetMapping("/me/login-rule")
    public ResponseEntity<?> getMyLoginRule(@RequestParam String userId) {
        try {
            EmployeeResponse employee = userService.getEmployeeByUserId(userId);

            if (employee.getLoginRuleId() == null) {
                return ResponseEntity.ok(new ErrorResponse("No login rule assigned. Contact administrator."));
            }

            LoginRuleResponse loginRule = loginRuleService.getLoginRuleById(employee.getLoginRuleId());
            return ResponseEntity.ok(loginRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Inner class for error responses
    private record ErrorResponse(String message) {
    }
}
