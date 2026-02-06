package com.monitoring.agent.ui;

import com.monitoring.agent.MonitoringAgent;
import com.monitoring.agent.model.AuthResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AgentUI {
    private final MonitoringAgent agent;
    private final AuthResponse user;
    private Stage stage;

    private Label statusLabel;
    private Label activityLabel;
    private Label sessionIdLabel;

    // Global Timers
    private Label sessionTimerLabel;
    private Label activeTimerLabel;
    private Label idleTimerLabel;
    private Label pausedTimerLabel;

    // Global Controls
    private Button startButton;
    private Button stopButton;
    private Button pauseButton;
    private Button logoutButton;
    private Button newTaskButton; // Added field
    private ComboBox<String> breakReasonComboBox;
    private String selectedBreakReason;

    // Task Table
    private TableView<TaskSessionModel> taskTable;
    private ObservableList<TaskSessionModel> taskList;

    public AgentUI(MonitoringAgent agent, AuthResponse user) {
        this.agent = agent;
        this.user = user;
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        // --- Header Section ---
        Label titleLabel = new Label("Employee Activity Monitor");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label userInfoLabel = new Label("Welcome, " + user.getFirstName() + " " + user.getLastName());
        userInfoLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        userInfoLabel.setTextFill(Color.web("#3498db"));

        Label jobRoleLabel = new Label("Role: " + user.getJobRole() + " | ID: " + user.getUserId());
        jobRoleLabel.setFont(Font.font("System", 13));
        jobRoleLabel.setTextFill(Color.web("#7f8c8d"));

        Label separator1 = new Label("────────────────────────");
        separator1.setTextFill(Color.web("#bdc3c7"));

        // --- Status & Activity ---
        statusLabel = new Label("Status: Not Started");
        statusLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        statusLabel.setTextFill(Color.web("#7f8c8d"));

        activityLabel = new Label("Activity: N/A");
        activityLabel.setFont(Font.font("System", 14));
        activityLabel.setTextFill(Color.web("#7f8c8d"));

        sessionIdLabel = new Label("");
        sessionIdLabel.setFont(Font.font("System", 12));
        sessionIdLabel.setTextFill(Color.web("#95a5a6"));

        VBox headerBox = new VBox(10, titleLabel, userInfoLabel, jobRoleLabel, separator1, statusLabel, activityLabel,
                sessionIdLabel);
        headerBox.setAlignment(Pos.CENTER);

        // --- Task Table Section ---
        taskTable = new TableView<>();
        taskList = FXCollections.observableArrayList();
        taskTable.setItems(taskList);
        taskTable.setEditable(true);
        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        taskTable.setPlaceholder(new Label("Click '+ New Task' to add a task"));
        taskTable.setDisable(true); // Default disabled

        // Column 1: Task Name (Editable)
        TableColumn<TaskSessionModel, String> nameCol = new TableColumn<>("Task Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().taskNameProperty());
        nameCol.setCellFactory(col -> new EditingCell());
        nameCol.setOnEditCommit(e -> {
            if (e.getRowValue().isEditable()) {
                e.getRowValue().setTaskName(e.getNewValue());
            } else {
                taskTable.refresh();
            }
        });
        nameCol.setEditable(true);

        // Column 2: Estimated Time (Editable)
        TableColumn<TaskSessionModel, String> timeCol = new TableColumn<>("Est. Time (min)");
        timeCol.setCellValueFactory(cellData -> cellData.getValue().estimatedTimeProperty());
        timeCol.setCellFactory(col -> new EditingCell());
        timeCol.setOnEditCommit(e -> {
            if (e.getRowValue().isEditable()) {
                // simple validation: force numeric
                if (e.getNewValue().matches("\\d*")) {
                    e.getRowValue().setEstimatedTime(e.getNewValue());
                } else {
                    taskTable.refresh();
                }
            }
        });
        timeCol.setEditable(true);

        // Column 3: Timer
        TableColumn<TaskSessionModel, String> timerCol = new TableColumn<>("Timer");
        timerCol.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
        timerCol.setEditable(false);

        // Column 4: Status
        TableColumn<TaskSessionModel, TaskSessionModel.TaskStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setEditable(false);

        // Column 5: Actions (Start/Stop/Pause/Delete)
        TableColumn<TaskSessionModel, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TaskSessionModel, Void> call(final TableColumn<TaskSessionModel, Void> param) {
                return new TableCell<>() {
                    private final Button mainBtn = new Button();
                    private final Button stopBtn = new Button("⏹");
                    private final Button deleteBtn = new Button("−"); // Minus sign for delete
                    private final HBox pane = new HBox(5, mainBtn, stopBtn, deleteBtn);

                    {
                        pane.setAlignment(Pos.CENTER);
                        stopBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
                        stopBtn.setOnAction(e -> {
                            TaskSessionModel task = getTableView().getItems().get(getIndex());
                            handleTaskStop(task);
                        });

                        deleteBtn.setStyle(
                                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
                        deleteBtn.setOnAction(e -> {
                            TaskSessionModel task = getTableView().getItems().get(getIndex());
                            taskList.remove(task);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            TaskSessionModel task = getTableView().getItems().get(getIndex());
                            TaskSessionModel.TaskStatus status = task.getStatus();

                            if (status == TaskSessionModel.TaskStatus.PENDING) {
                                mainBtn.setText("▶");
                                mainBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                                mainBtn.setOnAction(e -> handleTaskStart(task));
                                stopBtn.setVisible(false);
                                stopBtn.setManaged(false);

                                deleteBtn.setVisible(true); // Allow delete for pending
                                deleteBtn.setManaged(true);
                            } else if (status == TaskSessionModel.TaskStatus.ACTIVE) {
                                mainBtn.setText("⏸");
                                mainBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                                mainBtn.setOnAction(e -> handleTaskPause(task));
                                stopBtn.setVisible(true);
                                stopBtn.setManaged(true);

                                deleteBtn.setVisible(false);
                                deleteBtn.setManaged(false);
                            } else if (status == TaskSessionModel.TaskStatus.PAUSED) {
                                mainBtn.setText("▶");
                                mainBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                                mainBtn.setOnAction(e -> handleTaskStart(task));
                                stopBtn.setVisible(true);
                                stopBtn.setManaged(true);

                                deleteBtn.setVisible(false);
                                deleteBtn.setManaged(false);
                            } else {
                                // Completed/Stopped
                                mainBtn.setText("✓");
                                mainBtn.setDisable(true);
                                mainBtn.setStyle(
                                        "-fx-background-color: transparent; -fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                                stopBtn.setVisible(false);
                                stopBtn.setManaged(false);
                                deleteBtn.setVisible(false);
                                deleteBtn.setManaged(false);
                            }
                            setGraphic(pane);
                        }
                    }
                };
            }
        });

        taskTable.getColumns().addAll(nameCol, timeCol, timerCol, statusCol, actionCol);

        // Fix table visual height - dynamic binding
        taskTable.setFixedCellSize(35);
        // Bind height: (rows * cell_size) + header_size (approx 30) + padding
        taskTable.prefHeightProperty().bind(taskTable.fixedCellSizeProperty()
                .multiply(javafx.beans.binding.Bindings.size(taskTable.getItems())).add(30));
        taskTable.setMinHeight(30);

        newTaskButton = new Button("+ New Task");
        newTaskButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        newTaskButton.setDisable(true); // Default disabled
        newTaskButton.setOnAction(e -> handleNewTask());

        VBox tableBox = new VBox(5, taskTable, newTaskButton);
        tableBox.setPadding(new Insets(10, 0, 10, 0));
        // VBox.setVgrow(taskTable, Priority.ALWAYS); // Removed to allow dynamic sizing

        // --- Global Timers & Controls ---
        sessionTimerLabel = new Label("Session Time: 00:00:00");
        sessionTimerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        sessionTimerLabel.setTextFill(Color.web("#3498db"));

        activeTimerLabel = new Label("Active: 00:00:00");
        activeTimerLabel.setTextFill(Color.web("#27ae60"));

        idleTimerLabel = new Label("Idle: 00:00:00");
        idleTimerLabel.setTextFill(Color.web("#e67e22"));

        pausedTimerLabel = new Label("Paused: 00:00:00");
        pausedTimerLabel.setTextFill(Color.web("#9b59b6"));

        HBox timersBox = new HBox(15, sessionTimerLabel, activeTimerLabel, idleTimerLabel, pausedTimerLabel);
        timersBox.setAlignment(Pos.CENTER);

        Label separator2 = new Label("────────────────────────");
        separator2.setTextFill(Color.web("#bdc3c7"));

        // Global Break/Pause
        breakReasonComboBox = new ComboBox<>();
        breakReasonComboBox.getItems().addAll("Tea Break", "Lunch Break", "Personal Break", "Meeting", "Other");
        breakReasonComboBox.setPromptText("Break Reason");
        breakReasonComboBox.setOnAction(e -> {
            if (breakReasonComboBox.getValue() != null && !startButton.isDisabled()) {
                pauseButton.setDisable(false);
            }
        });

        pauseButton = new Button("⏸ Pause Work");
        pauseButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> handlePause()); // Global Pause

        startButton = new Button("Start Work");
        startButton.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        startButton.setPrefWidth(150);
        startButton.setOnAction(e -> handleStart()); // Global Start

        stopButton = new Button("Stop Work");
        stopButton.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        stopButton.setPrefWidth(150);
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> handleStop()); // Global Stop

        logoutButton = new Button("Log Out");
        logoutButton.setOnAction(e -> handleLogout());

        HBox controlsBox = new HBox(10, breakReasonComboBox, pauseButton);
        controlsBox.setAlignment(Pos.CENTER);

        HBox mainControlsBox = new HBox(15, startButton, stopButton);
        mainControlsBox.setAlignment(Pos.CENTER);

        VBox bottomBox = new VBox(10, separator2, timersBox, controlsBox, mainControlsBox, logoutButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));

        // Main Layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(headerBox);
        mainLayout.setCenter(tableBox); // Table in center
        mainLayout.setBottom(bottomBox);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #ecf0f1;");

        // Init Scene
        Scene scene = new Scene(mainLayout, 600, 800);
        stage.setTitle("Employee Activity Monitor");
        stage.setScene(scene);
        stage.show();
    }

    // --- Handlers ---

    private void handleStart() {
        // Global Start
        agent.startMonitoring(); // Calls internal monitoring start

        taskTable.setDisable(false); // Enable table
        if (newTaskButton != null)
            newTaskButton.setDisable(false);

        // Buttons
        startButton.setDisable(true);
        stopButton.setDisable(false);
        breakReasonComboBox.setDisable(false);
    }

    private void handleNewTask() {
        taskList.add(new TaskSessionModel("", ""));
    }

    private void handleTaskStart(TaskSessionModel task) {
        if (task.getTaskName().isEmpty() || task.getEstimatedTime().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter Task Name and Estimated Time.");
            alert.show();
            return;
        }

        // Stop/Pause any other active tasks?
        for (TaskSessionModel t : taskList) {
            if (t != task && t.getStatus() == TaskSessionModel.TaskStatus.ACTIVE) {
                // Auto-pause others
                handleTaskPause(t);
            }
        }

        task.setStatus(TaskSessionModel.TaskStatus.ACTIVE);
        agent.startTask(task); // Tell Agent to track this task
        taskTable.refresh();
    }

    private void handleTaskPause(TaskSessionModel task) {
        task.setStatus(TaskSessionModel.TaskStatus.PAUSED);
        // If this was the active task in Agent, calling startTask(null) or similar?
        // Or just let Agent know.
        // Effectively pausing task timer
        taskTable.refresh();
    }

    private void handleTaskStop(TaskSessionModel task) {
        task.setStatus(TaskSessionModel.TaskStatus.STOPPED);
        agent.stopTask(task);
        taskTable.refresh();
    }

    private void handleStop() {
        // Global Stop
        agent.stopMonitoring();

        taskTable.setDisable(true);
        if (newTaskButton != null)
            newTaskButton.setDisable(true);

        startButton.setDisable(false);
        stopButton.setDisable(true);
        pauseButton.setDisable(true);
        breakReasonComboBox.setDisable(true);

        // Mark all active tasks as stopped?
        for (TaskSessionModel t : taskList) {
            if (t.getStatus() == TaskSessionModel.TaskStatus.ACTIVE
                    || t.getStatus() == TaskSessionModel.TaskStatus.PAUSED) {
                t.setStatus(TaskSessionModel.TaskStatus.STOPPED);
            }
        }
        taskTable.refresh();
    }

    private void handleLogout() {
        agent.logout();
    }

    private void handlePause() {
        selectedBreakReason = breakReasonComboBox.getValue();
        agent.togglePause();
    }

    // --- Update Methods from Agent ---

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
        // Handled in handleStart/Stop mostly, but for external triggers:
        Platform.runLater(() -> {
            if (!started && startButton.isDisabled()) {
                handleStop(); // Sync UI if backend stopped it
            }
        });
    }

    public void setPaused(boolean paused) {
        Platform.runLater(() -> {
            if (paused) {
                pauseButton.setText("▶ Resume Work");
                pauseButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                statusLabel.setText("Status: ⏸ PAUSED");
                statusLabel.setTextFill(Color.web("#f39c12"));
                // When global pause is active, task table is visually paused (timers wont tick)
            } else {
                pauseButton.setText("⏸ Pause Work");
                pauseButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
                statusLabel.setText("Status: Monitoring Active");
                statusLabel.setTextFill(Color.web("#27ae60"));
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

            // Clear table? OR Keep history?
            // Usually reset implies clear for new session.
            taskList.clear();
        });
    }

    class EditingCell extends TableCell<TaskSessionModel, String> {
        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener((arg0, arg1, arg2) -> {
                if (!arg2) {
                    commitEdit(textField.getText());
                }
            });

            // Also commit on Enter
            textField.setOnAction(e -> commitEdit(textField.getText()));
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
}
