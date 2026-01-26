package com.monitoring.service;

import com.monitoring.dto.SessionResponse;
import com.monitoring.dto.WebSocketEventDTO;
import com.monitoring.entity.User;
import com.monitoring.entity.WorkSession;
import com.monitoring.exception.TrackingNotAllowedException;
import com.monitoring.repository.SessionRepository;
import com.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LoginRuleService loginRuleService;

    @Transactional
    public WorkSession startSession(String userId) {
        // Get user and check if tracking is allowed
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check if tracking is allowed based on login rule
        if (!loginRuleService.isTrackingAllowed(user)) {
            String nextWindow = loginRuleService.getNextAllowedWindow(user);
            throw new TrackingNotAllowedException(
                    "Tracking is not permitted at this time. " + nextWindow,
                    nextWindow);
        }

        // Auto-stop any existing ACTIVE sessions for this user
        List<WorkSession> activeSessions = sessionRepository
                .findByUserIdAndStatus(userId, WorkSession.SessionStatus.ACTIVE);

        for (WorkSession activeSession : activeSessions) {
            activeSession.setEndTime(LocalDateTime.now());
            activeSession.setStatus(WorkSession.SessionStatus.STOPPED);
            sessionRepository.save(activeSession);
            log.info("Auto-stopped previous session {} for user {}", activeSession.getId(), userId);

            // Broadcast session stop for the auto-stopped session
            publishSessionEvent(WebSocketEventDTO.EventType.SESSION_STOPPED, activeSession);
        }

        // Create new session
        WorkSession session = new WorkSession();
        session.setUserId(userId);
        session.setStatus(WorkSession.SessionStatus.ACTIVE);

        WorkSession savedSession = sessionRepository.save(session);
        log.info("Started new session {} for user {}", savedSession.getId(), userId);

        // Broadcast session creation via WebSocket
        publishSessionEvent(WebSocketEventDTO.EventType.SESSION_CREATED, savedSession);

        return savedSession;
    }

    @Transactional
    public WorkSession stopSession(UUID sessionId) {
        WorkSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() == WorkSession.SessionStatus.STOPPED) {
            throw new RuntimeException("Session already stopped: " + sessionId);
        }

        session.setEndTime(LocalDateTime.now());
        session.setStatus(WorkSession.SessionStatus.STOPPED);

        WorkSession savedSession = sessionRepository.save(session);
        log.info("Stopped session {}", sessionId);

        // Broadcast session stop via WebSocket
        publishSessionEvent(WebSocketEventDTO.EventType.SESSION_STOPPED, savedSession);

        return savedSession;
    }

    public WorkSession getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

    public List<WorkSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    public List<WorkSession> getSessionsByUser(String userId) {
        return sessionRepository.findByUserIdOrderByStartTimeDesc(userId);
    }

    public List<WorkSession> getActiveSessions() {
        return sessionRepository.findByStatusOrderByStartTimeDesc(WorkSession.SessionStatus.ACTIVE);
    }

    private void publishSessionEvent(WebSocketEventDTO.EventType eventType, WorkSession session) {
        try {
            SessionResponse response = SessionResponse.from(session);

            // Fetch and populate user details
            userRepository.findByUserId(session.getUserId()).ifPresent(user -> {
                response.setFirstName(user.getFirstName());
                response.setLastName(user.getLastName());
                response.setJobRole(user.getJobRole());
            });

            WebSocketEventDTO event = WebSocketEventDTO.of(eventType, response);
            messagingTemplate.convertAndSend("/topic/sessions", event);
            log.debug("Published {} event for session {}", eventType, session.getId());
        } catch (Exception e) {
            log.error("Failed to publish session event", e);
        }
    }
}
