package com.monitoring.service;

import com.monitoring.dto.ScreenshotResponse;
import com.monitoring.dto.WebSocketEventDTO;
import com.monitoring.entity.Screenshot;
import com.monitoring.entity.WorkSession;
import com.monitoring.repository.ScreenshotRepository;
import com.monitoring.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenshotService {

    private final ScreenshotRepository screenshotRepository;
    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${monitoring.screenshot.storage-path}")
    private String storagePath;

    @Transactional
    public Screenshot saveScreenshot(UUID sessionId, MultipartFile file, String metadata) throws IOException {
        // Get session to extract userId
        WorkSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        String userId = session.getUserId();

        // Get current date for folder structure (YYYY-MM-DD)
        String dateFolder = java.time.LocalDate.now().toString();

        // Create storage directory: screenshots/YYYY-MM-DD/userId/
        Path storageDir = Paths.get(storagePath, dateFolder, userId);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";
        String filename = sessionId + "_" + System.currentTimeMillis() + extension;

        // Save file to disk
        Path filePath = storageDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Save metadata to database
        Screenshot screenshot = new Screenshot();
        screenshot.setSessionId(sessionId);
        screenshot.setFilePath(filePath.toString());
        screenshot.setFileSize(file.getSize());
        screenshot.setMetadata(metadata);

        Screenshot savedScreenshot = screenshotRepository.save(screenshot);
        log.info("Saved screenshot {} for session {} (user: {}) at {}",
                savedScreenshot.getId(), sessionId, userId, filePath);

        // Broadcast screenshot upload via WebSocket
        publishScreenshotEvent(savedScreenshot);

        return savedScreenshot;
    }

    public Screenshot getScreenshot(Long screenshotId) {
        return screenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new RuntimeException("Screenshot not found: " + screenshotId));
    }

    public List<Screenshot> getScreenshots(UUID sessionId) {
        return screenshotRepository.findBySessionIdOrderByCapturedAtAsc(sessionId);
    }

    public Path getScreenshotFile(Long screenshotId) {
        Screenshot screenshot = getScreenshot(screenshotId);
        return Paths.get(screenshot.getFilePath());
    }

    private void publishScreenshotEvent(Screenshot screenshot) {
        try {
            ScreenshotResponse response = ScreenshotResponse.from(screenshot);
            WebSocketEventDTO event = WebSocketEventDTO.of(WebSocketEventDTO.EventType.SCREENSHOT_UPLOADED, response);
            messagingTemplate.convertAndSend("/topic/screenshots/" + screenshot.getSessionId(), event);
            log.debug("Published screenshot event for session {}", screenshot.getSessionId());
        } catch (Exception e) {
            log.error("Failed to publish screenshot event", e);
        }
    }
}
