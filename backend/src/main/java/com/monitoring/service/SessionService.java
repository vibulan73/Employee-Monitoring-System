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

import com.monitoring.dto.EmployeeStatsDTO;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LoginRuleService loginRuleService;

    @Transactional
    public WorkSession startSession(String userId, String taskName, Long estimatedDurationMinutes) {
        // DEBUG: Log incoming task data
        log.info("=== START SESSION DEBUG ===");
        log.info("Received startSession request - userId: {}, taskName: '{}', estimatedDuration: {}",
                userId, taskName, estimatedDurationMinutes);

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
        session.setTaskName(taskName);
        session.setEstimatedDurationMinutes(estimatedDurationMinutes);
        session.setStatus(WorkSession.SessionStatus.ACTIVE);

        WorkSession savedSession = sessionRepository.save(session);

        // DEBUG: Log saved session data
        log.info("Saved session to database - sessionId: {}, taskName: '{}', estimatedDuration: {}",
                savedSession.getId(), savedSession.getTaskName(), savedSession.getEstimatedDurationMinutes());
        log.info("=== END SESSION DEBUG ===");

        log.info("Started new session {} for user {} with task: {}", savedSession.getId(), userId, taskName);

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

    public EmployeeStatsDTO getEmployeeStats(String userId, LocalDate from, LocalDate to) {
        LocalDateTime startDateTime = from.atStartOfDay();
        LocalDateTime endDateTime = to.atTime(LocalTime.MAX);

        List<WorkSession> sessions = sessionRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeDesc(
                userId, startDateTime, endDateTime);

        // Fetch user for login rule
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        String ruleName = (user.getLoginRule() != null) ? user.getLoginRule().getRuleName() : "N/A";

        // Map sessions to response
        List<SessionResponse> sessionResponses = sessions.stream()
                .map(SessionResponse::from)
                .collect(Collectors.toList());

        // Calculate stats
        Set<LocalDate> workingDays = sessions.stream()
                .map(s -> s.getStartTime().toLocalDate())
                .collect(Collectors.toSet());

        long totalWorkingDays = workingDays.size();

        Duration totalDuration = Duration.ZERO;
        Duration activeDuration = Duration.ZERO;
        Duration idleDuration = Duration.ZERO;

        for (WorkSession session : sessions) {
            if (session.getEndTime() != null) {
                Duration sessionDuration = Duration.between(session.getStartTime(), session.getEndTime());
                totalDuration = totalDuration.plus(sessionDuration);

                // For now, assuming all time is active since we don't have granular idle
                // tracking in the session model yet
                // You might need to adjust this if you track idle time separately within a
                // session
                activeDuration = activeDuration.plus(sessionDuration);
            } else {
                // For active sessions, calculate duration until now
                Duration currentDuration = Duration.between(session.getStartTime(), LocalDateTime.now());
                totalDuration = totalDuration.plus(currentDuration);
                activeDuration = activeDuration.plus(currentDuration);
            }
        }

        // Convert to hours with 2 decimal places
        double totalHours = Math.round(totalDuration.toMinutes() / 60.0 * 100.0) / 100.0;
        double activeHours = Math.round(activeDuration.toMinutes() / 60.0 * 100.0) / 100.0;
        double idleHours = 0.0; // Placeholder until idle tracking logic is refined

        return EmployeeStatsDTO.builder()
                .totalWorkingDays(totalWorkingDays)
                .totalWorkingHours(totalHours)
                .totalActiveHours(activeHours)
                .totalIdleHours(idleHours)
                .loginRuleName(ruleName)
                .recentSessions(sessionResponses)
                .build();
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
