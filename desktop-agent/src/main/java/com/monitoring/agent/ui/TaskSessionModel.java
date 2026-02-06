package com.monitoring.agent.ui;

import javafx.beans.property.*;

public class TaskSessionModel {
    public enum TaskStatus {
        PENDING, ACTIVE, PAUSED, COMPLETED, STOPPED
    }

    private final StringProperty taskName = new SimpleStringProperty("");
    private final StringProperty estimatedTime = new SimpleStringProperty("");
    private final StringProperty duration = new SimpleStringProperty("00:00:00");
    private final ObjectProperty<TaskStatus> status = new SimpleObjectProperty<>(TaskStatus.PENDING);
    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    // Timer tracking for this specific task
    private long durationSeconds = 0;

    public TaskSessionModel() {
        // Default constructor for new empty row
    }

    public TaskSessionModel(String name, String time) {
        this.taskName.set(name);
        this.estimatedTime.set(time);
    }

    public StringProperty taskNameProperty() {
        return taskName;
    }

    public String getTaskName() {
        return taskName.get();
    }

    public void setTaskName(String name) {
        this.taskName.set(name);
    }

    public StringProperty estimatedTimeProperty() {
        return estimatedTime;
    }

    public String getEstimatedTime() {
        return estimatedTime.get();
    }

    public void setEstimatedTime(String time) {
        this.estimatedTime.set(time);
    }

    public StringProperty durationProperty() {
        return duration;
    }

    public String getDuration() {
        return duration.get();
    }

    public void setDuration(String duration) {
        this.duration.set(duration);
    }

    public ObjectProperty<TaskStatus> statusProperty() {
        return status;
    }

    public TaskStatus getStatus() {
        return status.get();
    }

    public void setStatus(TaskStatus status) {
        this.status.set(status);
        // If status becomes anything other than PENDING, it's no longer editable
        if (status != TaskStatus.PENDING) {
            this.editable.set(false);
        }
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public boolean isEditable() {
        return editable.get();
    }

    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void incrementDuration() {
        this.durationSeconds++;
        updateDurationString();
    }

    private void updateDurationString() {
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;
        this.duration.set(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }
}
