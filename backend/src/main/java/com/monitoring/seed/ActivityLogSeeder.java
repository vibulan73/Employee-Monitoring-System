package com.monitoring.seed;

import com.monitoring.entity.ActivityLog;
import com.monitoring.entity.WorkSession;
import com.monitoring.repository.ActivityLogRepository;
import com.monitoring.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(2) // Run after SessionSeeder
@RequiredArgsConstructor
@Slf4j
public class ActivityLogSeeder implements CommandLineRunner {

    private final ActivityLogRepository activityLogRepository;
    private final SessionRepository sessionRepository;

    @Override
    public void run(String... args) {
        if (activityLogRepository.count() == 0) {
            log.info("Seeding activity logs...");

            // Get existing sessions
            List<WorkSession> sessions = sessionRepository.findAll();

            if (sessions.isEmpty()) {
                log.warn("No sessions found. Skipping activity log seeding.");
                return;
            }

            List<ActivityLog> activityLogs = new ArrayList<>();

            // Create activity logs for the first session (if exists)
            if (sessions.size() > 0) {
                WorkSession session1 = sessions.get(0);

                ActivityLog log1 = new ActivityLog();
                log1.setSessionId(session1.getId());
                log1.setActivityStatus(ActivityLog.ActivityStatus.ACTIVE);
                log1.setMetadata("User is actively working on code");
                activityLogs.add(log1);

                ActivityLog log2 = new ActivityLog();
                log2.setSessionId(session1.getId());
                log2.setActivityStatus(ActivityLog.ActivityStatus.ACTIVE);
                log2.setMetadata("User is browsing documentation");
                activityLogs.add(log2);

                ActivityLog log3 = new ActivityLog();
                log3.setSessionId(session1.getId());
                log3.setActivityStatus(ActivityLog.ActivityStatus.IDLE);
                log3.setMetadata("User idle - taking a break");
                activityLogs.add(log3);
            }

            // Create activity logs for the second session (if exists)
            if (sessions.size() > 1) {
                WorkSession session2 = sessions.get(1);

                ActivityLog log4 = new ActivityLog();
                log4.setSessionId(session2.getId());
                log4.setActivityStatus(ActivityLog.ActivityStatus.ACTIVE);
                log4.setMetadata("User reviewing pull requests");
                activityLogs.add(log4);

                ActivityLog log5 = new ActivityLog();
                log5.setSessionId(session2.getId());
                log5.setActivityStatus(ActivityLog.ActivityStatus.IDLE);
                log5.setMetadata("User idle for 5 minutes");
                activityLogs.add(log5);
            }

            activityLogRepository.saveAll(activityLogs);
            log.info("Seeded {} activity logs", activityLogs.size());
        }
    }
}