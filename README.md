# Employee Activity Monitoring System

A comprehensive employee activity monitoring system built with Spring Boot, JavaFX, and React. The system tracks work sessions, monitors user activity (active/idle), and captures periodic screenshots with full user consent.

## 🎯 Features

- **Session Management**: Start and stop work sessions with unique tracking IDs
- **Activity Monitoring**: Real-time tracking of keyboard and mouse activity
- **Idle Detection**: Automatic detection of idle periods (configurable threshold)
- **Screenshot Capture**: Periodic full-screen screenshots (configurable interval)
- **Web Dashboard**: Beautiful web interface to view all sessions and analytics
- **Multi-User Support**: Track sessions for multiple users
- **Privacy-First**: Monitoring only occurs during active work sessions

## 🏗️ Architecture

The system consists of three main components:

### 1. **Backend (Spring Boot)**
- REST API server
- Database management (H2 for development, PostgreSQL-ready for production)
- File storage for screenshots
- Session, activity, and screenshot management

### 2. **Desktop Agent (JavaFX)**
- Modern desktop application
- Global keyboard/mouse monitoring
- Automated screenshot capture
- Real-time activity status display

### 3. **Web UI (React)**
- Dashboard with session overview
- Detailed session analytics
- Activity timeline visualization
- Screenshot gallery with lightbox viewer

## 📋 Prerequisites

- **Java 17+** (for backend and desktop agent)
- **Maven 3.6+** (for building Java projects)
- **Node.js 16+** and **npm** (for web UI)

## 🚀 Getting Started

### 1. Backend Setup

```bash
cd backend

# Run with H2 database (default)
mvn spring-boot:run

# Or run with PostgreSQL (production)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The backend will start on `http://localhost:8080`

**H2 Console** (development only): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/monitoring`
- Username: `sa`
- Password: (leave empty)

### 2. Desktop Agent Setup

```bash
cd desktop-agent

# Build the project
mvn clean package

# Run the agent
mvn javafx:run

# Or run the JAR directly
java -jar target/desktop-agent-1.0.0.jar
```

**Configuration**: Edit `src/main/resources/agent.properties` to customize:
- Backend URL
- Screenshot capture interval
- Idle detection threshold
- Activity update frequency

### 3. Web UI Setup

```bash
cd web-ui

# Install dependencies
npm install

# Start development server
npm start
```

The web UI will open at `http://localhost:3000`

## 📖 Usage Guide

### Starting a Work Session

1. **Launch the Desktop Agent**
   - Open the desktop agent application
   - Enter your User ID
   - Click "Start Work"

2. **Monitoring Begins**
   - Activity monitoring starts automatically
   - Screenshots are captured every 5 minutes (configurable)
   - Activity status updates every 30 seconds

3. **View in Web UI**
   - Open the web dashboard at `http://localhost:3000`
   - Your session appears in the dashboard
   - Click on a session to view detailed analytics

### Stopping a Work Session

1. Click "Stop Work" in the desktop agent
2. Monitoring stops immediately
3. Session is marked as "STOPPED" in the database
4. View completed session data in the web dashboard

## 🔧 Configuration

### Backend Configuration (`application.yml`)

```yaml
monitoring:
  screenshot:
    storage-path: ./screenshots  # Screenshot storage directory
  cors:
    allowed-origins: http://localhost:3000,http://localhost:5173
```

### Desktop Agent Configuration (`agent.properties`)

```properties
backend.url=http://localhost:8080
screenshot.interval.minutes=5
activity.idle.threshold.seconds=60
activity.update.interval.seconds=30
```

## 🗄️ Database Schema

### WorkSession
- `id` (UUID): Unique session identifier
- `user_id` (String): User identifier
- `start_time` (DateTime): Session start time
- `end_time` (DateTime): Session end time (nullable)
- `status` (Enum): ACTIVE | STOPPED

### ActivityLog
- `id` (Long): Auto-generated ID
- `session_id` (UUID): Reference to work session
- `logged_at` (DateTime): Timestamp of activity
- `activity_status` (Enum): ACTIVE | IDLE
- `metadata` (String): Additional information

### Screenshot
- `id` (Long): Auto-generated ID
- `session_id` (UUID): Reference to work session
- `captured_at` (DateTime): Capture timestamp
- `file_path` (String): Path to screenshot file
- `file_size` (Long): File size in bytes

## 🌐 API Endpoints

### Session Management
- `POST /api/sessions/start` - Start a new session
- `POST /api/sessions/{id}/stop` - Stop a session
- `GET /api/sessions/{id}` - Get session details
- `GET /api/sessions` - List all sessions (with filters)

### Activity Logging
- `POST /api/activity` - Log activity event
- `GET /api/activity/session/{sessionId}` - Get activity logs

### Screenshots
- `POST /api/screenshots` - Upload screenshot
- `GET /api/screenshots/session/{sessionId}` - List screenshots
- `GET /api/screenshots/{id}/image` - Download screenshot

## 🎨 Web UI Features

- **Dashboard**: Overview of all work sessions with filtering
- **Session Details**: Comprehensive session analytics
  - Session metadata and duration
  - Activity statistics (active vs idle percentages)
  - Activity timeline with timestamps
  - Screenshot gallery with lightbox viewer
- **Responsive Design**: Works on desktop and mobile
- **Modern UI**: Gradient backgrounds, glassmorphism effects, smooth animations

## 🔒 Security & Privacy

- ✅ Monitoring only active during work sessions
- ✅ Clear user consent via Start/Stop buttons
- ✅ Screenshots stored securely on server
- ✅ Session IDs use non-guessable UUIDs
- ✅ CORS configured to limit API access
- ✅ File upload validation and sanitization

## 🐛 Troubleshooting

### Desktop Agent Won't Start
- Ensure Java 17+ is installed: `java --version`
- Check backend is running at `http://localhost:8080`
- Verify JavaFX is properly installed

### Screenshots Not Capturing
- Check file system permissions for screenshot directory
- Verify screenshot interval in configuration
- Check backend logs for upload errors

### Web UI Not Connecting
- Ensure backend is running on port 8080
- Check CORS configuration in `application.yml`
- Verify axios API base URL in `api.js`

## 📦 Building for Production

### Backend
```bash
cd backend
mvn clean package
java -jar target/employee-monitoring-backend-1.0.0.jar --spring.profiles.active=prod
```

### Desktop Agent
```bash
cd desktop-agent
mvn clean package shade:shade
# Executable JAR: target/desktop-agent-1.0.0.jar
```

### Web UI
```bash
cd web-ui
npm run build
# Serve the build/ directory with your web server
```

## 🛠️ Technologies Used

### Backend
- Spring Boot 3.2.1
- Spring Data JPA
- H2 Database / PostgreSQL
- Maven

### Desktop Agent
- JavaFX 21
- JNativeHook 2.2.2
- Apache HttpClient 5
- Java AWT Robot

### Web UI
- React 18
- React Router
- Axios
- CSS3 (modern gradients, animations)

## 📝 License

This project is created for educational and assignment purposes.

## 👨‍💻 Developer Notes

- The system uses H2 in-memory database by default for easy development
- Production deployments should use PostgreSQL
- Screenshot storage defaults to `./screenshots` directory
- All timestamps are stored in local server time
- Activity monitoring uses global hooks (requires appropriate permissions)

## 🎓 Assignment Requirements Checklist

✅ System design thinking
✅ Backend development with Spring Boot
✅ Desktop-level monitoring concepts
✅ Secure data handling
✅ REST API implementation
✅ Database schema design
✅ Activity tracking (Active/Idle)
✅ Screenshot capture and storage
✅ User consent mechanism (Start/Stop)
✅ Web UI for data visualization
✅ Multi-user support
✅ Clear documentation

---

**Note**: This monitoring system should only be used with proper user consent and in compliance with applicable privacy laws and regulations.
