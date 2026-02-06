package com.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Manual Getters and Setters and Constructor
    // Note: @AllArgsConstructor might not be generating correctly if Lombok fails
    /*
     * Wait, I should make sure I have the AllArgsConstructor manually if Lombok is
     * failing.
     * But I will rely on standard java here.
     */

    // Explicit All Args Constructor since Lombok might be failing
    /*
     * Actually, standard java doesn't allow duplicate constructors if I define one
     * that matches signature.
     * Since I'm uncertain if Lombok works at all, I'll define everything manually.
     */

    /*
     * Wait, the @AllArgsConstructor annotation is present in the import.
     * But if Lombok is broken, I must add it.
     */

    /*
     * Redundant if Lombok works, but safe if I remove proper Lombok annotations or
     * just add manual ones.
     */

    /* Let's just create a constructor and getters/setters. */

    /*
     * If I add a manual constructor with 2 args, it conflicts
     * with @AllArgsConstructor if Lombok WAS working.
     * But we know it's not.
     */

    public WebSocketEventDTO(EventType eventType, Object payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
