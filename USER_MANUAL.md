# User Manual

## 1. System Overview

The Employee Activity Monitoring System is designed to help organizations track work sessions and productivity.
-   **Desktop Agent**: Installed on employee computers to track active/idle time and capture screenshots.
-   **Web Dashboard**: Used by managers/admins to view activity reports and sessions.

---

## 2. Getting Started

### For Administrators/IT
Before anyone can use the system, the Backend server must be running.
1.  Follow the [Deployment Guide](DEPLOYMENT_GUIDE.md) to set up and run the Backend.
2.  Provide employees with the **Desktop Agent** application (JAR file).
3.  Provide managers with the **Web Dashboard** URL.

---

## 3. Employee User Guide

### Step 1: Launch the Application
Double-click the `desktop-agent-1.0.0.jar` file (or run it via command line if instructed).

### Step 2: Login / Identify
1.  You will see a login screen.
2.  Enter your unique **User ID** (e.g., your employee ID or email).
3.  Click **Login**.

### Step 3: Start Working
1.  When you are ready to begin work, click the **Start Work** button.
2.  The status will change to `ACTIVE`.
3.  **Minimizing**: You can minimize the window. The agent continues running in the background.

### What is being monitored?
*   **Activity Status**: The system detects if you are using the mouse/keyboard. If you stop for a set period (e.g., 60 seconds), the status changes to `IDLE`.
*   **Screenshots**: The system captures a screenshot of your screen periodically (e.g., every 5 minutes) *only while the session is active*.

### Step 4: Stop Working
1.  When you are finished or taking a break, click **Stop Work**.
2.  Monitoring stops immediately. No screenshots or activity logs are recorded while stopped.

---

## 4. Admin User Guide (Web Dashboard)

### Accessing the Dashboard
Open the Web UI URL in your browser (e.g., `http://localhost:3000` or your production URL).

### Dashboard Overview
The main screen shows a list of all recorded work sessions.
-   **Filter**: You can filter sessions by User ID or Date.
-   **Summary**: View total hours worked per user.

### Session Details
Click on any session in the list to view details:
1.  **Timeline**: A visual graph showing Active (Green) vs. Idle (Gray) time.
2.  **Screenshots**: A gallery of screenshots captured during that session. Click on a screenshot to enlarge it.
3.  **Activity Log**: A text-based log of start/stop and idle events.

### Troubleshooting Missing Data
*   **No new sessions?** Ensure the employee has clicked "Start Work" and their agent is connected to the backend.
*   **No screenshots?** Check if the 'screenshots' folder on the server has write permissions.
