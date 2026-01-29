# Deployment Guide

This guide covers the deployment process for the Employee Activity Monitoring System, including the Backend, Desktop Agent, and Web UI.

## 📋 Prerequisites

Ensure the following software is installed on the target machine:
- **Java Development Kit (JDK) 17+** (Required for Backend & Desktop Agent)
- **Maven 3.6+** (Required for building Java projects)
- **Node.js 16+ & npm** (Required for Web UI)

---

## 1. Backend Deployment (Spring Boot)

The backend is a Spring Boot application that can be run as a standalone JAR file.

### A. Local Development (H2 Database)
The default configuration uses an in-memory H2 database.

1.  Navigate to the `backend` directory:
    ```powershell
    cd backend
    ```
2.  Run using Maven:
    ```powershell
    mvn spring-boot:run
    ```
    *The server will start at `http://localhost:8080`.*

### B. Production Deployment (PostgreSQL)
For production, use a PostgreSQL database and the production profile.

1.  **Build the JAR:**
    ```powershell
    cd backend
    mvn clean package -DskipTests
    ```
    *The JAR file will be created in `backend/target/`.*

2.  **Environment Variables:**
    Set the following environment variables on your server (or in your deployment platform like Render/Heroku):
    *   `spring.profiles.active`: `prod`
    *   `DB_URL`: `jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>`
    *   `DB_USERNAME`: `your_db_username`
    *   `DB_PASSWORD`: `your_db_password`
    *   `GMAIL_CREDENTIALS_PATH`: Path to `credentials.json` (if using Gmail API)
    *   `GMAIL_TOKENS_PATH`: Path to tokens directory

3.  **Run the JAR:**
    ```powershell
    java -jar target/employee-monitoring-backend-1.0.0.jar
    ```

---

## 2. Desktop Agent Deployment

The desktop agent is a JavaFX application that runs on the employee's machine.

### A. Run from Source
1.  Navigate to the `desktop-agent` directory:
    ```powershell
    cd desktop-agent
    ```
2.  Run using Maven:
    ```powershell
    mvn javafx:run
    ```

### B. Build Executable JAR
To distribute to employees:

1.  **Build the JAR:**
    ```powershell
    cd desktop-agent
    mvn clean package
    ```

2.  **Distribute:**
    Copy the `target/desktop-agent-1.0.0.jar` file to the employee's machine.

3.  **Run:**
    ```powershell
    java -jar desktop-agent-1.0.0.jar
    ```
    *Note: Ensure `agent.properties` is configured correctly (see Configuration Guide).*

---

## 3. Web UI Deployment (React)

The Web UI is a React application.

### A. Local Development
1.  Navigate to the `web-ui` directory:
    ```powershell
    cd web-ui
    ```
2.  Install dependencies:
    ```powershell
    npm install
    ```
3.  Start the development server:
    ```powershell
    npm start
    ```
    *The app will open at `http://localhost:3000`.*

### B. Production Build
1.  **Build static assets:**
    ```powershell
    cd web-ui
    npm run build
    ```
    *This creates a `build` folder containing static HTML, CSS, and JS files.*

2.  **Serve:**
    Deploy the contents of the `build` folder to any static hosting service (Vercel, Netlify, AWS S3, or Nginx).
    *   **Vercel/Netlify:** Simply upload the repo and set the build command to `npm run build` and publish directory to `build`.
    *   **Nginx:** Copy `build/*` to `/var/www/html/`.

---

## 4. Verification

After deployment, verify the system:
1.  **Backend:** Visit `http://<BACKEND_HOST>/actuator/health` (if enabled) or check logs for "Started MonitoringApplication".
2.  **Web UI:** Open the web application URL. It should load the dashboard.
3.  **Desktop Agent:** Launch the agent; it should connect to the backend without error.
