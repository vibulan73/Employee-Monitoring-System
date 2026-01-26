package com.monitoring.agent.ui;

import com.monitoring.agent.MonitoringAgent;
import com.monitoring.agent.model.AuthResponse;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AgentUI {
    private final MonitoringAgent agent;
    private final AuthResponse user;
    private Stage stage;

    private Label statusLabel;
    private Label activityLabel;
    private Label sessionIdLabel;
    private Label sessionTimerLabel;
    private Label activeTimerLabel;
    private Label idleTimerLabel;
    private Label pausedTimerLabel;
    private Label currentBreakReasonLabel;
    private ComboBox<String> breakReasonComboBox;
    private Button startButton;
    private Button stopButton;
    private Button pauseButton;
    private Button logoutButton;
    private String selectedBreakReason;

    public AgentUI(MonitoringAgent agent, AuthResponse user) {
        this.agent = agent;
        this.user = user;
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        // Create UI components
        Label titleLabel = new Label("Employee Activity Monitor");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // User info display
        Label userInfoLabel = new Label("Welcome, " + user.getFirstName() + " " + user.getLastName());
        userInfoLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        userInfoLabel.setTextFill(Color.web("#3498db"));

        Label jobRoleLabel = new Label("Role: " + user.getJobRole() + " | ID: " + user.getUserId());
        jobRoleLabel.setFont(Font.font("System", 13));
        jobRoleLabel.setTextFill(Color.web("#7f8c8d"));

        // Status labels
        statusLabel = new Label("Status: Not Started");
        statusLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        statusLabel.setTextFill(Color.web("#7f8c8d"));

        activityLabel = new Label("Activity: N/A");
        activityLabel.setFont(Font.font("System", 14));
        activityLabel.setTextFill(Color.web("#7f8c8d"));

        sessionIdLabel = new Label("");
        sessionIdLabel.setFont(Font.font("System", 12));
        sessionIdLabel.setTextFill(Color.web("#95a5a6"));

        // Timer labels
        sessionTimerLabel = new Label("Session Time: 00:00:00");
        sessionTimerLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        sessionTimerLabel.setTextFill(Color.web("#3498db"));

        activeTimerLabel = new Label("Active: 00:00:00");
        activeTimerLabel.setFont(Font.font("System", 14));
        activeTimerLabel.setTextFill(Color.web("#27ae60"));

        idleTimerLabel = new Label("Idle: 00:00:00");
        idleTimerLabel.setFont(Font.font("System", 14));
        idleTimerLabel.setTextFill(Color.web("#e67e22"));

        pausedTimerLabel = new Label("Paused: 00:00:00");
        pausedTimerLabel.setFont(Font.font("System", 14));
        pausedTimerLabel.setTextFill(Color.web("#9b59b6"));

        // Current break reason label
        currentBreakReasonLabel = new Label("");
        currentBreakReasonLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        currentBreakReasonLabel.setTextFill(Color.web("#9b59b6"));
        currentBreakReasonLabel.setVisible(false);

        // Break reason dropdown
        breakReasonComboBox = new ComboBox<>();
        breakReasonComboBox.getItems().addAll(
                "Tea Break",
                "Lunch Break",
                "Personal Break",
                "Meeting",
                "Prayer Break",
                "Other");
        breakReasonComboBox.setPromptText("Select break reason...");
        breakReasonComboBox.setPrefWidth(200);
        breakReasonComboBox.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-background-radius: 5;");

        // Add listener to enable pause button when reason is selected
        breakReasonComboBox.setOnAction(e -> {
            String selected = breakReasonComboBox.getValue();
            // Enable pause button if monitoring is started (start button is disabled) and
            // reason is selected
            if (selected != null && !selected.isEmpty() && startButton.isDisabled()) {
                pauseButton.setDisable(false);
                pauseButton.setStyle(
                        "-fx-background-color: #f39c12; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
            }
        });

        // Separator line
        Label separator = new Label("────────────────────────");
        separator.setFont(Font.font("System", 12));
        separator.setTextFill(Color.web("#bdc3c7"));

        // Pause Button
        pauseButton = new Button("⏸️ Pause");
        pauseButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        pauseButton.setPrefWidth(200);
        pauseButton.setPrefHeight(45);
        pauseButton.setStyle(
                "-fx-background-color: #f39c12; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-opacity: 0.5;");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> handlePause());

        // Buttons
        startButton = new Button("Start Work");
        startButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        startButton.setPrefWidth(200);
        startButton.setPrefHeight(45);
        startButton.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        startButton.setOnAction(e -> handleStart());

        stopButton = new Button("Stop Work");
        stopButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        stopButton.setPrefWidth(200);
        stopButton.setPrefHeight(45);
        stopButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-opacity: 0.5;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> handleStop());

        logoutButton = new Button("⬅ Log Out");
        logoutButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        logoutButton.setPrefWidth(200);
        logoutButton.setPrefHeight(45);
        logoutButton.setStyle(
                "-fx-background-color: #95a5a6; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        logoutButton.setOnAction(e -> handleLogout());

        logoutButton.setOnMouseEntered(e -> {
            logoutButton.setStyle(
                    "-fx-background-color: #7f8c8d; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;");
        });
        logoutButton.setOnMouseExited(e -> {
            logoutButton.setStyle(
                    "-fx-background-color: #95a5a6; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;");
        });

        // Add hover effects
        startButton.setOnMouseEntered(e -> {
            if (!startButton.isDisabled()) {
                startButton.setStyle(
                        "-fx-background-color: #229954; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
            }
        });
        startButton.setOnMouseExited(e -> {
            if (!startButton.isDisabled()) {
                startButton.setStyle(
                        "-fx-background-color: #27ae60; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
            }
        });

        stopButton.setOnMouseEntered(e -> {
            if (!stopButton.isDisabled()) {
                stopButton.setStyle(
                        "-fx-background-color: #c0392b; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
            }
        });
        stopButton.setOnMouseExited(e -> {
            if (!stopButton.isDisabled()) {
                stopButton.setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
            }
        });

        // Layout
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ecf0f1;");
        layout.getChildren().addAll(
                titleLabel,
                userInfoLabel,
                jobRoleLabel,
                statusLabel,
                activityLabel,
                sessionIdLabel,
                separator,
                sessionTimerLabel,
                activeTimerLabel,
                idleTimerLabel,
                pausedTimerLabel,
                currentBreakReasonLabel,
                startButton, // Will be hidden when monitoring starts
                breakReasonComboBox, // Will be shown when monitoring starts
                pauseButton, // Will be shown when monitoring starts
                logoutButton,
                stopButton);

        // Initially hide pause-related controls
        breakReasonComboBox.setVisible(false);
        breakReasonComboBox.setManaged(false);
        pauseButton.setVisible(false);
        pauseButton.setManaged(false);

        Scene scene = new Scene(layout, 500, 720);
        stage.setTitle("Employee Activity Monitor");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> {
            agent.shutdown();
            Platform.exit();
        });

        stage.show();
    }

    private void handleStart() {
        agent.startMonitoring();
    }

    private void handleStop() {
        agent.stopMonitoring();
    }

    private void handleLogout() {
        agent.logout();
    }

    private void handlePause() {
        // Store the selected break reason before pausing
        selectedBreakReason = breakReasonComboBox.getValue();
        agent.togglePause();
    }

    public void updateStatus(String status, Color color) {
        Platform.runLater(() -> {
            statusLabel.setText("Status: " + status);
            statusLabel.setTextFill(color);
        });
    }

    public void updateActivity(String activity) {
        Platform.runLater(() -> {
            activityLabel.setText("Activity: " + activity);
            if ("ACTIVE".equals(activity)) {
                activityLabel.setTextFill(Color.web("#27ae60"));
            } else {
                activityLabel.setTextFill(Color.web("#e67e22"));
            }
        });
    }

    public void updateSessionId(String sessionId) {
        Platform.runLater(() -> {
            sessionIdLabel.setText("Session ID: " + sessionId);
        });
    }

    public void setMonitoringStarted(boolean started) {
        Platform.runLater(() -> {
            startButton.setDisable(started);
            stopButton.setDisable(!started);

            // Pause button is only enabled when monitoring is started AND break reason is
            // selected
            if (started) {
                // Hide Start button, show Pause controls
                startButton.setVisible(false);
                startButton.setManaged(false);

                // Show pause-related controls
                breakReasonComboBox.setVisible(true);
                breakReasonComboBox.setManaged(true);
                pauseButton.setVisible(true);
                pauseButton.setManaged(true);

                // Enable dropdown when monitoring starts
                breakReasonComboBox.setDisable(false);
                // Pause button stays disabled until reason is selected
                pauseButton.setDisable(true);
            } else {
                // Show Start button, hide Pause controls
                startButton.setVisible(true);
                startButton.setManaged(true);

                // Hide pause-related controls
                breakReasonComboBox.setVisible(false);
                breakReasonComboBox.setManaged(false);
                pauseButton.setVisible(false);
                pauseButton.setManaged(false);

                // Disable both when monitoring stops
                pauseButton.setDisable(true);
                breakReasonComboBox.setDisable(true);
                // Disable both when monitoring stops
                pauseButton.setDisable(true);
                breakReasonComboBox.setDisable(true);
                breakReasonComboBox.setValue(null);
            }

            // Handle Logout Button Visibility
            if (started) {
                logoutButton.setVisible(false);
                logoutButton.setManaged(false);
            } else {
                logoutButton.setVisible(true);
                logoutButton.setManaged(true);
            }

            if (started) {
                startButton.setStyle(
                        "-fx-background-color: #95a5a6; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-opacity: 0.5;");
                stopButton.setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
                pauseButton.setStyle(
                        "-fx-background-color: #f39c12; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
            } else {
                startButton.setStyle(
                        "-fx-background-color: #27ae60; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
                stopButton.setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-opacity: 0.5;");
                pauseButton.setStyle(
                        "-fx-background-color: #f39c12; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-opacity: 0.5;");
            }
        });
    }

    public void setPaused(boolean paused) {
        Platform.runLater(() -> {
            if (paused) {
                // Show break reason during pause
                if (selectedBreakReason != null && !selectedBreakReason.isEmpty()) {
                    currentBreakReasonLabel.setText("Break Reason: " + selectedBreakReason);
                    currentBreakReasonLabel.setVisible(true);
                }
                pauseButton.setText("▶️ Resume");
                pauseButton.setStyle(
                        "-fx-background-color: #27ae60; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
                statusLabel.setText("Status: ⏸️ PAUSED");
                statusLabel.setTextFill(Color.web("#f39c12"));
                activityLabel.setText("Activity: --");
                activityLabel.setTextFill(Color.web("#7f8c8d"));
                // Disable dropdown during pause
                breakReasonComboBox.setDisable(true);
            } else {
                // Clear break reason and hide label
                currentBreakReasonLabel.setVisible(false);
                currentBreakReasonLabel.setText("");
                selectedBreakReason = null;

                pauseButton.setText("⏸️ Pause");
                pauseButton.setStyle(
                        "-fx-background-color: #f39c12; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-opacity: 0.5;");
                pauseButton.setDisable(true); // Disable until new reason is selected
                statusLabel.setText("Status: Monitoring Active");
                statusLabel.setTextFill(Color.web("#27ae60"));

                // Clear and re-enable dropdown
                breakReasonComboBox.setValue(null);
                breakReasonComboBox.setDisable(false);
            }
        });
    }

    public void updateSessionTimer(String time) {
        Platform.runLater(() -> sessionTimerLabel.setText("Session Time: " + time));
    }

    public void updateActiveTimer(String time) {
        Platform.runLater(() -> activeTimerLabel.setText("Active: " + time));
    }

    public void updateIdleTimer(String time) {
        Platform.runLater(() -> idleTimerLabel.setText("Idle: " + time));
    }

    public void updatePausedTimer(String time) {
        Platform.runLater(() -> pausedTimerLabel.setText("Paused: " + time));
    }

    public void resetTimers() {
        Platform.runLater(() -> {
            sessionTimerLabel.setText("Session Time: 00:00:00");
            activeTimerLabel.setText("Active: 00:00:00");
            idleTimerLabel.setText("Idle: 00:00:00");
            pausedTimerLabel.setText("Paused: 00:00:00");
        });
    }
}
