package com.monitoring.agent;

import com.monitoring.agent.client.BackendClient;
import com.monitoring.agent.model.AuthResponse;
import com.monitoring.agent.model.SessionResponse;
import com.monitoring.agent.monitor.ActivityMonitor;
import com.monitoring.agent.monitor.ScreenshotCapture;
import com.monitoring.agent.ui.AgentUI;
import com.monitoring.agent.ui.LoginUI;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonitoringAgent extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringAgent.class);

    private BackendClient backendClient;
    private ActivityMonitor activityMonitor;
    private ScreenshotCapture screenshotCapture;
    private AuthResponse currentUser;
    private AgentUI ui;
    private Stage stage;

    private ScheduledExecutorService scheduler;
    private UUID currentSessionId;
    private boolean isMonitoring = false;

    // Timer tracking
    private long sessionStartTime;
    private long activeDurationSeconds = 0;
    private long idleDurationSeconds = 0;
    private long pausedDurationSeconds = 0;
    private String lastActivityStatus = "IDLE";

    // Pause tracking
    private boolean isPaused = false;
    private long pauseStartTime;

    // Configuration
    private String backendUrl;
    private double screenshotIntervalMinutes;
    private int idleThresholdSeconds;
    private int activityUpdateIntervalSeconds;

    @Override
    public void init() throws Exception {
        loadConfiguration();

        backendClient = new BackendClient(backendUrl);
        activityMonitor = new ActivityMonitor(idleThresholdSeconds);
        screenshotCapture = new ScreenshotCapture();
        scheduler = Executors.newScheduledThreadPool(2);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        showLoginScreen();
    }

    public void showLoginScreen() {
        // Show login screen first
        LoginUI loginUI = new LoginUI(this, backendClient, stage);
        loginUI.show();
    }

    public void logout() {
        if (isMonitoring) {
            stopMonitoring();
        }

        this.currentUser = null;
        this.currentSessionId = null;
        this.ui = null;

        logger.info("User logged out");
        showLoginScreen();
    }

    public void onLoginSuccess(AuthResponse user) {
        this.currentUser = user;
        logger.info("User logged in: {} {}", user.getFirstName(), user.getLastName());

        // Show the main monitoring UI
        ui = new AgentUI(this, user);
        ui.start(stage);
    }

    public void startMonitoring() {
        if (isMonitoring) {
            logger.warn("Monitoring already started");
            return;
        }

        String userId = currentUser.getUserId();

        try {
            // Start session on backend
            SessionResponse session = backendClient.startSession(userId);
            currentSessionId = session.getSessionId();

            logger.info("Started monitoring session: {}", currentSessionId);

            // Start activity monitoring
            activityMonitor.start();
            activityMonitor.recordActivity();

            // Schedule activity logging
            scheduler.scheduleAtFixedRate(
                    this::logActivity,
                    0,
                    activityUpdateIntervalSeconds,
                    TimeUnit.SECONDS);

            // Schedule screenshot capture
            long intervalSeconds = (long) (screenshotIntervalMinutes * 60);
            scheduler.scheduleAtFixedRate(
                    this::captureAndUploadScreenshot,
                    intervalSeconds,
                    intervalSeconds,
                    TimeUnit.SECONDS);

            isMonitoring = true;

            // Initialize timers
            sessionStartTime = System.currentTimeMillis();
            activeDurationSeconds = 0;
            idleDurationSeconds = 0;
            pausedDurationSeconds = 0;
            isPaused = false;
            lastActivityStatus = "IDLE";

            // Schedule timer updates every second
            scheduler.scheduleAtFixedRate(
                    this::updateTimers,
                    0,
                    1,
                    TimeUnit.SECONDS);

            // Schedule session status check every 30 seconds to detect backend auto-stop
            scheduler.scheduleAtFixedRate(
                    this::checkSessionStatus,
                    30,
                    30,
                    TimeUnit.SECONDS);

            // Update UI
            ui.setMonitoringStarted(true);
            ui.updateStatus("Monitoring Active", Color.web("#27ae60"));
            ui.updateSessionId(currentSessionId.toString());

        } catch (Exception e) {
            logger.error("Failed to start monitoring", e);

            // Check if it's a tracking restriction error
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.startsWith("TRACKING_NOT_ALLOWED:")) {
                // Parse the error to extract the next allowed window message
                try {
                    String jsonPart = errorMessage.substring("TRACKING_NOT_ALLOWED:".length()).trim();
                    // Extract the message field from JSON
                    String nextWindow = "Contact your administrator";
                    if (jsonPart.contains("\"nextAllowedWindow\"")) {
                        int start = jsonPart.indexOf("\"nextAllowedWindow\":\"") + "\"nextAllowedWindow\":\"".length();
                        int end = jsonPart.indexOf("\"", start);
                        if (start > 0 && end > start) {
                            nextWindow = jsonPart.substring(start, end);
                        }
                    }

                    String displayMessage = "Tracking Not Permitted\n\n" + nextWindow;
                    ui.updateStatus(displayMessage, Color.web("#e74c3c"));
                    logger.warn("Tracking blocked by login rule: {}", nextWindow);
                } catch (Exception parseError) {
                    ui.updateStatus("Tracking not permitted at this time", Color.web("#e74c3c"));
                }
            } else {
                ui.updateStatus("Failed to start: " + e.getMessage(), Color.web("#e74c3c"));
            }
        }
    }

    public void stopMonitoring() {
        if (!isMonitoring) {
            logger.warn("Monitoring not started");
            return;
        }

        // If paused, calculate final paused duration
        if (isPaused) {
            long pauseDuration = (System.currentTimeMillis() - pauseStartTime) / 1000;
            pausedDurationSeconds += pauseDuration;
            isPaused = false;
        }

        try {
            // Stop session on backend
            backendClient.stopSession(currentSessionId);

            // Stop activity monitoring
            activityMonitor.stop();

            // Cancel scheduled tasks
            scheduler.shutdownNow();
            scheduler = Executors.newScheduledThreadPool(2);

            logger.info("Stopped monitoring session: {}", currentSessionId);

            isMonitoring = false;
            currentSessionId = null;

            // Update UI
            ui.setMonitoringStarted(false);
            ui.updateStatus("Monitoring Stopped", Color.web("#7f8c8d"));
            ui.updateActivity("N/A");
            ui.updateSessionId("");
            ui.resetTimers();

        } catch (Exception e) {
            logger.error("Failed to stop monitoring", e);
            ui.updateStatus("Failed to stop: " + e.getMessage(), Color.web("#e74c3c"));
        }
    }

    public void togglePause() {
        if (!isMonitoring)
            return;

        if (isPaused) {
            // Resume monitoring
            long pauseDuration = (System.currentTimeMillis() - pauseStartTime) / 1000;
            pausedDurationSeconds += pauseDuration;
            isPaused = false;
            ui.setPaused(false);

            // Force activity update to prevent stuck in IDLE state
            activityMonitor.recordActivity();

            logger.info("Monitoring resumed after {} seconds pause", pauseDuration);
        } else {
            // Pause monitoring
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
            ui.setPaused(true);
            logger.info("Monitoring paused");
        }
    }

    private void logActivity() {
        if (!isMonitoring || isPaused)
            return;

        try {
            String activityStatus = activityMonitor.getActivityStatus();
            backendClient.logActivity(currentSessionId, activityStatus, null);
            ui.updateActivity(activityStatus);
            lastActivityStatus = activityStatus;
        } catch (IOException e) {
            logger.error("Failed to log activity", e);
        }
    }

    private void updateTimers() {
        if (!isMonitoring)
            return;

        // Calculate session time
        long sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 1000;

        // Update active/idle/paused duration based on current status
        if (isPaused) {
            // Calculate current pause duration
            long currentPauseDuration = (System.currentTimeMillis() - pauseStartTime) / 1000;
            ui.updatePausedTimer(formatDuration(pausedDurationSeconds + currentPauseDuration));
        } else {
            // Check current activity status immediately
            // This ensures the UI updates instantly when user becomes active after being
            // idle
            // instead of waiting for the next log interval
            String currentStatus = activityMonitor.getActivityStatus();
            if (!currentStatus.equals(lastActivityStatus)) {
                lastActivityStatus = currentStatus;
                ui.updateActivity(lastActivityStatus);
            }

            if ("ACTIVE".equals(lastActivityStatus)) {
                activeDurationSeconds++;
            } else {
                idleDurationSeconds++;
            }
            ui.updatePausedTimer(formatDuration(pausedDurationSeconds));
        }

        // Update UI
        ui.updateSessionTimer(formatDuration(sessionDuration));
        ui.updateActiveTimer(formatDuration(activeDurationSeconds));
        ui.updateIdleTimer(formatDuration(idleDurationSeconds));
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void captureAndUploadScreenshot() {
        if (!isMonitoring || isPaused)
            return;

        try {
            com.monitoring.agent.model.ScreenshotData screenshotData = screenshotCapture
                    .captureScreenshot("screenshot_" + currentSessionId);
            backendClient.uploadScreenshot(
                    currentSessionId,
                    screenshotData.getFile(),
                    screenshotData.getMetadata());
            logger.info("Screenshot uploaded successfully");
        } catch (Exception e) {
            logger.error("Failed to capture/upload screenshot", e);
        }
    }

    public void shutdown() {
        if (isMonitoring) {
            stopMonitoring();
        }

        try {
            scheduler.shutdown();
            activityMonitor.shutdown();
            backendClient.close();
        } catch (IOException e) {
            logger.error("Error closing backend client", e);
        }
    }

    private void loadConfiguration() throws IOException {
        Properties props = new Properties();

        // Try to load from classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("agent.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                // Use defaults
                logger.warn("agent.properties not found, using defaults");
                setDefaults();
                return;
            }
        }

        backendUrl = props.getProperty("backend.url", "http://localhost:8080");
        screenshotIntervalMinutes = Double.parseDouble(props.getProperty("screenshot.interval.minutes", "5"));
        idleThresholdSeconds = Integer.parseInt(props.getProperty("activity.idle.threshold.seconds", "60"));
        activityUpdateIntervalSeconds = Integer.parseInt(props.getProperty("activity.update.interval.seconds", "30"));

        logger.info("Configuration loaded - Backend: {}, Screenshot Interval: {}min, Idle Threshold: {}s",
                backendUrl, screenshotIntervalMinutes, idleThresholdSeconds);
    }

    private void setDefaults() {
        backendUrl = "http://localhost:8080";
        screenshotIntervalMinutes = 1.0;
        idleThresholdSeconds = 15;
        activityUpdateIntervalSeconds = 30;
    }

    /**
     * Check if the session is still active on the backend
     * This detects when backend auto-stops a session due to idle timeout
     */
    private void checkSessionStatus() {
        if (!isMonitoring || currentSessionId == null) {
            return;
        }

        try {
            SessionResponse session = backendClient.getSession(currentSessionId);
            logger.info("Session status check: sessionId={}, status={}, isMonitoring={}",
                    currentSessionId, session.getStatus(), isMonitoring);

            // If backend says session is STOPPED but we're still monitoring, stop locally
            if (session.getStatus() != null &&
                    session.getStatus().equalsIgnoreCase("STOPPED") &&
                    isMonitoring) {

                logger.warn(
                        "Session {} was stopped remotely (possibly auto-stopped by backend). Stopping local monitoring.",
                        currentSessionId);

                javafx.application.Platform.runLater(() -> {
                    // Stop local monitoring without calling backend (it's already stopped)
                    stopMonitoringLocal();
                    ui.updateStatus("Session Auto-Stopped by Server", Color.web("#e74c3c"));
                });
            }
        } catch (Exception e) {
            logger.error("Error checking session status for {}: {}", currentSessionId, e.getMessage(), e);
        }
    }

    /**
     * Stop monitoring locally without calling backend
     * Used when backend has already stopped the session
     */
    private void stopMonitoringLocal() {
        if (!isMonitoring) {
            return;
        }

        // Calculate final paused duration if currently paused
        if (isPaused) {
            long pauseDuration = (System.currentTimeMillis() - pauseStartTime) / 1000;
            pausedDurationSeconds += pauseDuration;
            isPaused = false;
        }

        activityMonitor.stop();

        // Cancel scheduled tasks
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(2);

        isMonitoring = false;
        currentSessionId = null;

        // Reset all timer variables to prevent issues on next session start
        sessionStartTime = 0;
        activeDurationSeconds = 0;
        idleDurationSeconds = 0;
        pausedDurationSeconds = 0;
        lastActivityStatus = "IDLE";

        logger.info("Stopped monitoring locally (session already stopped on backend)");

        // Update UI
        ui.setMonitoringStarted(false);
        ui.setPaused(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
