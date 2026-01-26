package com.monitoring.exception;

public class TrackingNotAllowedException extends RuntimeException {

    private final String nextAllowedWindow;

    public TrackingNotAllowedException(String message) {
        super(message);
        this.nextAllowedWindow = null;
    }

    public TrackingNotAllowedException(String message, String nextAllowedWindow) {
        super(message);
        this.nextAllowedWindow = nextAllowedWindow;
    }

    public String getNextAllowedWindow() {
        return nextAllowedWindow;
    }
}
