package com.monitoring.dto;

import com.monitoring.entity.Screenshot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenshotResponse {
    private Long id;
    private UUID sessionId;
    private LocalDateTime capturedAt;
    private Long fileSize;
    private String metadata;

    public static ScreenshotResponse from(Screenshot screenshot) {
        return new ScreenshotResponse(
                screenshot.getId(),
                screenshot.getSessionId(),
                screenshot.getCapturedAt(),
                screenshot.getFileSize(),
                screenshot.getMetadata());
    }
}
