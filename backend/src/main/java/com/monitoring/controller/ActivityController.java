package com.monitoring.controller;

import com.monitoring.dto.ActivityLogResponse;
import com.monitoring.dto.LogActivityRequest;
import com.monitoring.entity.ActivityLog;
import com.monitoring.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityLogResponse> logActivity(@Valid @RequestBody LogActivityRequest request) {
        ActivityLog log = activityService.logActivity(
                request.getSessionId(),
                request.getActivityStatus(),
                request.getMetadata());
        return ResponseEntity.status(HttpStatus.CREATED).body(ActivityLogResponse.from(log));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ActivityLogResponse>> getActivityLogs(@PathVariable UUID sessionId) {
        List<ActivityLog> logs = activityService.getActivityLogs(sessionId);
        List<ActivityLogResponse> response = logs.stream()
                .map(ActivityLogResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
