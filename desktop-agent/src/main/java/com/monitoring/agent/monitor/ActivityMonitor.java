package com.monitoring.agent.monitor;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ActivityMonitor implements NativeKeyListener, NativeMouseInputListener {
    private static final Logger logger = LoggerFactory.getLogger(ActivityMonitor.class);

    private volatile LocalDateTime lastActivityTime;
    private final int idleThresholdSeconds;
    private boolean isMonitoring = false;

    public ActivityMonitor(int idleThresholdSeconds) {
        this.idleThresholdSeconds = idleThresholdSeconds;
        this.lastActivityTime = LocalDateTime.now();
    }

    public void start() throws NativeHookException {
        if (!isMonitoring) {
            // Disable verbose JNativeHook logging which can interfere with event capture
            java.util.logging.Logger jnativeLogger = java.util.logging.Logger
                    .getLogger(GlobalScreen.class.getPackage().getName());
            jnativeLogger.setLevel(java.util.logging.Level.OFF);
            jnativeLogger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseListener(this);
            GlobalScreen.addNativeMouseMotionListener(this);
            isMonitoring = true;
            lastActivityTime = LocalDateTime.now();
            logger.info("Activity monitoring started with idle threshold: {} seconds", idleThresholdSeconds);
        }
    }

    public void stop() {
        if (isMonitoring) {
            try {
                GlobalScreen.removeNativeKeyListener(this);
                GlobalScreen.removeNativeMouseListener(this);
                GlobalScreen.removeNativeMouseMotionListener(this);
                // Don't unregister the native hook here - it terminates the event dispatcher
                // and prevents re-registration. Only remove our listeners.
                isMonitoring = false;
                logger.info("Activity monitoring stopped");
            } catch (Exception e) {
                logger.error("Error stopping activity monitor", e);
            }
        }
    }

    public void shutdown() {
        // Call this only when completely shutting down the application
        if (isMonitoring) {
            stop();
        }
        try {
            if (GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.unregisterNativeHook();
                logger.info("Native hook unregistered");
            }
        } catch (NativeHookException e) {
            logger.error("Error unregistering native hook", e);
        }
    }

    public boolean isIdle() {
        long secondsSinceLastActivity = ChronoUnit.SECONDS.between(lastActivityTime, LocalDateTime.now());
        return secondsSinceLastActivity >= idleThresholdSeconds;
    }

    public String getActivityStatus() {
        return isIdle() ? "IDLE" : "ACTIVE";
    }

    public void recordActivity() {
        lastActivityTime = LocalDateTime.now();
        logger.debug("Activity recorded at: {}", lastActivityTime);
    }

    // NativeKeyListener methods
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        recordActivity();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Not used
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Not used
    }

    // NativeMouseInputListener methods
    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        recordActivity();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        recordActivity();
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        // Not used
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        recordActivity();
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        recordActivity();
    }
}
