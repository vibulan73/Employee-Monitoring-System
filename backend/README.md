# Employee Activity Monitoring System - Backend

Spring Boot REST API backend for the employee activity monitoring system.

## Features

- Session management (start/stop work sessions)
- Activity logging (active/idle tracking)
- Screenshot upload and storage
- Multi-user support
- H2 database for development
- PostgreSQL support for production

## Running the Backend

### Development (H2 Database)
```bash
mvn spring-boot:run
```

### Production (PostgreSQL)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## API Endpoints

### Sessions
- `POST /api/sessions/start` - Start new session
- `POST /api/sessions/{id}/stop` - Stop session
- `GET /api/sessions/{id}` - Get session
- `GET /api/sessions?userId={userId}&status={status}` - List sessions

### Activity
- `POST /api/activity` - Log activity
- `GET /api/activity/session/{sessionId}` - Get activity logs

### Screenshots
- `POST /api/screenshots` - Upload screenshot
- `GET /api/screenshots/session/{sessionId}` - List screenshots
- `GET /api/screenshots/{id}/image` - Get screenshot image

## Configuration

Edit `src/main/resources/application.yml`:
- Server port
- Database connection
- Screenshot storage path
- CORS allowed origins

## H2 Console

Access at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/monitoring`
- Username: `sa`
- Password: (empty)
