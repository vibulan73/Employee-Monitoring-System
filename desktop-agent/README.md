# Desktop Monitoring Agent

JavaFX desktop application for employee activity monitoring.

## Features

- Modern JavaFX user interface
- Global keyboard/mouse activity monitoring
- Automatic idle detection
- Periodic screenshot capture
- Real-time backend synchronization

## Running the Agent

### Using Maven
```bash
mvn javafx:run
```

### Using JAR
```bash
mvn clean package
java -jar target/desktop-agent-1.0.0.jar
```

## Configuration

Edit `src/main/resources/agent.properties`:

```properties
# Backend URL
backend.url=http://localhost:8080

# Screenshot capture interval (minutes)
screenshot.interval.minutes=5

# Idle detection threshold (seconds)
activity.idle.threshold.seconds=60

# Activity update frequency (seconds)
activity.update.interval.seconds=30
```

## Usage

1. Enter your User ID
2. Click "Start Work" to begin monitoring
3. Activity and screenshots are automatically tracked
4. Click "Stop Work" to end the session

## Requirements

- Java 17 or higher
- JavaFX 21
- Backend server running at configured URL

## Permissions

The agent requires permission to:
- Capture keyboard and mouse events (global hooks)
- Capture screenshots
- Write to temp directory
- Network access to backend server
