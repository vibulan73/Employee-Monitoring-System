# Configuration Guide

This guide details the configuration files and parameters that need to be changed for each component of the Employee Activity Monitoring System.

## 1. Backend Configuration

**File:** `backend/src/main/resources/application.yml` (or `application-prod.yml` for production)

### Database Settings
Change these to point to your PostgreSQL database in production.
```yaml
spring:
  datasource:
    url: jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>
    username: <YOUR_DB_USER>
    password: <YOUR_DB_PASSWORD>
```

### Gmail API Integration
Required for sending emails.
```yaml
spring:
  gmail:
    credentials:
      file-path: src/main/resources/credentials.json # Path to Google Cloud credentials file
    tokens:
      directory-path: tokens # Directory to store auth tokens
```

### Application Settings
Customize general application behavior.
```yaml
monitoring:
  screenshot:
    storage-path: ./screenshots  # Where to save screenshot files on the server
  cors:
    allowed-origins: http://localhost:3000,https://your-frontend-domain.com
  admin:
    email: admin@company.com     # Primary admin email
    cc-email: manager@company.com
```

---

## 2. Desktop Agent Configuration

**File:** `desktop-agent/src/main/resources/agent.properties`

This file is packaged inside the JAR. If you need to change these after building, you can extract the properties file or modify the source before building.

### Critical Settings
```properties
# URL of the deployed backend. MUST be reachable from employee machines.
backend.url=http://localhost:8080
# Example for production:
# backend.url=https://api.monitoring.company.com
```

### Monitoring Behavior
```properties
# How often to take screenshots (in minutes)
screenshot.interval.minutes=5

# Seconds of inactivity before marking status as IDLE
activity.idle.threshold.seconds=60

# How often to send activity heartbeats to the server (in seconds)
activity.update.interval.seconds=30
```

---

## 3. Web UI Configuration

**File:** `web-ui/.env`

Create this file in the root of the `web-ui` directory if it doesn't exist.

### A. Local Development
```ini
REACT_APP_API_URL=http://localhost:8080/api
```

### B. Production Build
When building for production (`npm run build`), ensuring this variable is set correctly in your build environment.
```ini
REACT_APP_API_URL=https://api.monitoring.company.com/api
```
*Note: The React app needs to know where to send API requests. If this is incorrect, the dashboard will show network errors.*
