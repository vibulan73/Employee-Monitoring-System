package com.monitoring.controller;

import com.monitoring.dto.ScreenshotResponse;
import com.monitoring.entity.Screenshot;
import com.monitoring.service.ScreenshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/screenshots")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class ScreenshotController {

    private final ScreenshotService screenshotService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ScreenshotResponse> uploadScreenshot(
            @RequestParam("sessionId") UUID sessionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "metadata", required = false) String metadata) {

        try {
            Screenshot screenshot = screenshotService.saveScreenshot(sessionId, file, metadata);
            return ResponseEntity.status(HttpStatus.CREATED).body(ScreenshotResponse.from(screenshot));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload screenshot: " + e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ScreenshotResponse>> getScreenshots(@PathVariable UUID sessionId) {
        List<Screenshot> screenshots = screenshotService.getScreenshots(sessionId);
        List<ScreenshotResponse> response = screenshots.stream()
                .map(ScreenshotResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{screenshotId}/image")
    public ResponseEntity<Resource> downloadScreenshot(@PathVariable Long screenshotId) {
        try {
            Path filePath = screenshotService.getScreenshotFile(screenshotId);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + filePath.getFileName().toString() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Screenshot file not found or not readable");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download screenshot: " + e.getMessage());
        }
    }
}
