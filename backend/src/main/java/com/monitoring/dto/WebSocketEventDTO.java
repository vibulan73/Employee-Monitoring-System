package com.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEventDTO {

    public enum EventType {
        SESSION_CREATED,
        SESSION_UPDATED,
        SESSION_STOPPED,
        ACTIVITY_LOGGED,
        SCREENSHOT_UPLOADED,
        EMPLOYEE_CREATED,
        EMPLOYEE_UPDATED,
        EMPLOYEE_DELETED
    }

    private EventType eventType;
    private Object payload;

    public static WebSocketEventDTO of(EventType eventType, Object payload) {
        return new WebSocketEventDTO(eventType, payload);
    }
}
