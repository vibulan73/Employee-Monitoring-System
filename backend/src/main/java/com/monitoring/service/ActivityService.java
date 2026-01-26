package com.monitoring.service;

import com.monitoring.dto.ActivityLogResponse;
import com.monitoring.dto.WebSocketEventDTO;
import com.monitoring.entity.ActivityLog;
import com.monitoring.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ActivityLog logActivity(UUID sessionId, ActivityLog.ActivityStatus status, String metadata) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setSessionId(sessionId);
        activityLog.setActivityStatus(status);
        activityLog.setMetadata(metadata);

        ActivityLog savedLog = activityLogRepository.save(activityLog);
        log.debug("Logged activity for session {}: {}", sessionId, status);

        // Broadcast activity log via WebSocket
        publishActivityEvent(savedLog);

        return savedLog;
    }

    public List<ActivityLog> getActivityLogs(UUID sessionId) {
        return activityLogRepository.findBySessionIdOrderByLoggedAtAsc(sessionId);
    }

    private void publishActivityEvent(ActivityLog activityLog) {
        try {
            ActivityLogResponse response = ActivityLogResponse.from(activityLog);
            WebSocketEventDTO event = WebSocketEventDTO.of(WebSocketEventDTO.EventType.ACTIVITY_LOGGED, response);
            messagingTemplate.convertAndSend("/topic/activity/" + activityLog.getSessionId(), event);
            log.debug("Published activity event for session {}", activityLog.getSessionId());
        } catch (Exception e) {
            log.error("Failed to publish activity event", e);
        }
    }
}
