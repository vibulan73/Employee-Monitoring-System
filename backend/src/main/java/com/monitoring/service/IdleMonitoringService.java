package com.monitoring.service;

import com.monitoring.entity.ActivityLog;
import com.monitoring.entity.WorkSession;
import com.monitoring.repository.ActivityLogRepository;
import com.monitoring.repository.SessionRepository;
import com.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdleMonitoringService {

    private final SessionRepository sessionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SessionService sessionService;

    @Value("${monitoring.idle.warning-minutes:30}")
    private int warningMinutes;

    @Value("${monitoring.idle.auto-stop-minutes:60}")
    private int autoStopMinutes;

    /**
     * Check for idle sessions every 5 minutes
     */
    // Check every 1 minute (60000 ms)
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    @Transactional
    public void checkIdleSessions() {
        log.info("=== CHECKING FOR IDLE SESSIONS ===");

        List<WorkSession> activeSessions = sessionRepository
                .findByStatusOrderByStartTimeDesc(WorkSession.SessionStatus.ACTIVE);

        log.info("Found {} active sessions to check", activeSessions.size());

        // DEBUG: Log all sessions to see what's in the database
        List<WorkSession> allSessions = sessionRepository.findAll();
        log.info("DEBUG: Total sessions in database: {}", allSessions.size());
        for (WorkSession s : allSessions) {
            log.info("DEBUG: Session {} - Status: {}, User: {}, Task: {}",
                    s.getId(), s.getStatus(), s.getUserId(), s.getTaskName());
        }

        for (WorkSession session : activeSessions) {
            try {
                processSession(session);
            } catch (Exception e) {
                log.error("Error processing session {}", session.getId(), e);
            }
        }
    }

    private void processSession(WorkSession session) {
        log.info("Processing session {} for user {}", session.getId(), session.getUserId());

        // Get the most recent activity logs (last 15 minutes)
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        List<ActivityLog> recentLogs = activityLogRepository
                .findBySessionIdAndLoggedAtAfterOrderByLoggedAtDesc(session.getId(), fifteenMinutesAgo);

        if (recentLogs.isEmpty()) {
            log.info("No recent activity logs for session {}, skipping", session.getId());
            return;
        }

        log.info("Found {} recent activity logs for session {}", recentLogs.size(), session.getId());

        // Check if user has been continuously idle
        int continuousIdleMinutes = calculateContinuousIdleMinutes(recentLogs);

        log.info("Session {} has {} continuous idle minutes (warning threshold: {}, auto-stop threshold: {})",
                session.getId(), continuousIdleMinutes, warningMinutes, autoStopMinutes);

        // Auto-stop if idle for configured threshold (default 60 minutes)
        if (continuousIdleMinutes >= autoStopMinutes) {
            log.info("Session {} exceeds auto-stop threshold, stopping session", session.getId());
            autoStopSession(session, continuousIdleMinutes);
            return;
        }

        // Send warning email if idle for configured threshold (default 30 minutes) and
        // not already sent
        if (continuousIdleMinutes >= warningMinutes && !Boolean.TRUE.equals(session.getIdleWarningSent())) {
            log.info("Session {} exceeds warning threshold and warning not sent, sending warning", session.getId());
            sendIdleWarning(session, continuousIdleMinutes);
        } else if (continuousIdleMinutes >= warningMinutes) {
            log.info("Session {} exceeds warning threshold but warning already sent", session.getId());
        } else {
            log.info("Session {} idle time below warning threshold", session.getId());
        }
    }

    private int calculateContinuousIdleMinutes(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            return 0;
        }

        // Check from most recent backwards
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActiveTime = null;

        for (ActivityLog log : logs) {
            if (log.getActivityStatus() == ActivityLog.ActivityStatus.ACTIVE) {
                lastActiveTime = log.getLoggedAt();
                break;
            }
        }

        // If all recent logs are IDLE, calculate duration from oldest IDLE log
        if (lastActiveTime == null) {
            // All logs in the window are IDLE - get the oldest one
            ActivityLog oldestIdleLog = logs.get(logs.size() - 1);
            return (int) Duration.between(oldestIdleLog.getLoggedAt(), now).toMinutes();
        }

        // Calculate idle time since last active
        return (int) Duration.between(lastActiveTime, now).toMinutes();
    }

    private void sendIdleWarning(WorkSession session, int idleMinutes) {
        log.info("Sending idle warning for session {} - {} minutes idle", session.getId(), idleMinutes);

        // Get user details
        userRepository.findByUserId(session.getUserId()).ifPresentOrElse(
                user -> {
                    emailService.sendIdleWarningEmail(
                            session.getId(),
                            session.getUserId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getJobRole(),
                            idleMinutes);

                    // Mark warning as sent
                    session.setIdleWarningSent(true);
                    session.setLastIdleCheckTime(LocalDateTime.now());
                    sessionRepository.save(session);
                },
                () -> log.warn("User not found for session {}", session.getId()));
    }

    private void autoStopSession(WorkSession session, int idleMinutes) {
        log.info("Auto-stopping session {} due to {} minutes of inactivity", session.getId(), idleMinutes);

        // Get user details and send notification
        userRepository.findByUserId(session.getUserId()).ifPresent(user -> {
            emailService.sendAutoStopEmail(
                    session.getId(),
                    session.getUserId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getJobRole(),
                    idleMinutes);
        });

        // Stop the session
        try {
            sessionService.stopSession(session.getId());
            log.info("Session {} automatically stopped", session.getId());
        } catch (Exception e) {
            log.error("Failed to auto-stop session {}", session.getId(), e);
        }
    }
}
