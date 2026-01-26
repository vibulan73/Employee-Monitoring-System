package com.monitoring.seed;

import com.monitoring.entity.Screenshot;
import com.monitoring.entity.WorkSession;
import com.monitoring.repository.ScreenshotRepository;
import com.monitoring.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(3) // Run after SessionSeeder and ActivityLogSeeder
@RequiredArgsConstructor
@Slf4j
public class ScreenshotSeeder implements CommandLineRunner {

    private final ScreenshotRepository screenshotRepository;
    private final SessionRepository sessionRepository;

    @Override
    public void run(String... args) {
        if (screenshotRepository.count() == 0) {
            log.info("Seeding screenshots...");

            // Get existing sessions
            List<WorkSession> sessions = sessionRepository.findAll();

            if (sessions.isEmpty()) {
                log.warn("No sessions found. Skipping screenshot seeding.");
                return;
            }

            List<Screenshot> screenshots = new ArrayList<>();

            // Create screenshots for the first session (if exists)
            if (sessions.size() > 0) {
                WorkSession session1 = sessions.get(0);

                Screenshot screenshot1 = new Screenshot();
                screenshot1.setSessionId(session1.getId());
                screenshot1.setFilePath("./screenshots/" + session1.getUserId() + "/screen_001.png");
                screenshot1.setFileSize(245678L);
                screenshot1.setMetadata("{\"windowTitle\":\"Visual Studio Code\",\"processId\":1234}");
                screenshots.add(screenshot1);

                Screenshot screenshot2 = new Screenshot();
                screenshot2.setSessionId(session1.getId());
                screenshot2.setFilePath("./screenshots/" + session1.getUserId() + "/screen_002.png");
                screenshot2.setFileSize(298456L);
                screenshot2.setMetadata("{\"windowTitle\":\"Chrome - Stack Overflow\",\"processId\":5678}");
                screenshots.add(screenshot2);
            }

            // Create screenshots for the second session (if exists)
            if (sessions.size() > 1) {
                WorkSession session2 = sessions.get(1);

                Screenshot screenshot3 = new Screenshot();
                screenshot3.setSessionId(session2.getId());
                screenshot3.setFilePath("./screenshots/" + session2.getUserId() + "/screen_001.png");
                screenshot3.setFileSize(512340L);
                screenshot3.setMetadata("{\"windowTitle\":\"Slack - Engineering Team\",\"processId\":9012}");
                screenshots.add(screenshot3);

                Screenshot screenshot4 = new Screenshot();
                screenshot4.setSessionId(session2.getId());
                screenshot4.setFilePath("./screenshots/" + session2.getUserId() + "/screen_002.png");
                screenshot4.setFileSize(423789L);
                screenshot4.setMetadata("{\"windowTitle\":\"Figma - Design Review\",\"processId\":3456}");
                screenshots.add(screenshot4);
            }

            screenshotRepository.saveAll(screenshots);
            log.info("Seeded {} screenshots", screenshots.size());
        }
    }
}
