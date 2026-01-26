package com.monitoring.controller;

import com.monitoring.dto.SessionResponse;
import com.monitoring.dto.StartSessionRequest;
import com.monitoring.entity.WorkSession;
import com.monitoring.exception.TrackingNotAllowedException;
import com.monitoring.repository.UserRepository;
import com.monitoring.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    @PostMapping("/start")
    public ResponseEntity<?> startSession(@Valid @RequestBody StartSessionRequest request) {
        try {
            WorkSession session = sessionService.startSession(request.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(enrichWithUserDetails(SessionResponse.from(session)));
        } catch (TrackingNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new TrackingErrorResponse(e.getMessage(), e.getNextAllowedWindow()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/stop")
    public ResponseEntity<SessionResponse> stopSession(@PathVariable UUID sessionId) {
        WorkSession session = sessionService.stopSession(sessionId);
        return ResponseEntity.ok(enrichWithUserDetails(SessionResponse.from(session)));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable UUID sessionId) {
        WorkSession session = sessionService.getSession(sessionId);
        return ResponseEntity.ok(enrichWithUserDetails(SessionResponse.from(session)));
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getAllSessions(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status) {

        List<WorkSession> sessions;

        if (userId != null) {
            sessions = sessionService.getSessionsByUser(userId);
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            sessions = sessionService.getActiveSessions();
        } else {
            sessions = sessionService.getAllSessions();
        }

        List<SessionResponse> response = sessions.stream()
                .map(SessionResponse::from)
                .map(this::enrichWithUserDetails)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private SessionResponse enrichWithUserDetails(SessionResponse response) {
        userRepository.findByUserId(response.getUserId()).ifPresent(user -> {
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setJobRole(user.getJobRole());
        });
        return response;
    }

    // Error response records
    private record ErrorResponse(String message) {
    }

    private record TrackingErrorResponse(String message, String nextAllowedWindow) {
    }
}
