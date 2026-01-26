# Setup and Installation Guide

## ⚠️ Important Issues Found

### 1. Maven Not Installed
Maven is required to run the Spring Boot backend and JavaFX desktop agent.

### 2. Special Characters in Path
The directory path contains `#` which causes issues with some build tools.

---

## 🔧 Required Software

### Install Maven

**Option 1: Using Chocolatey (Recommended for Windows)**
```powershell
# Install Chocolatey if not already installed
# Then install Maven:
choco install maven
```

**Option 2: Manual Installation**
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH:
   - Open System Properties → Environment Variables
   - Add to PATH: `C:\Program Files\Apache\maven\bin`
4. Verify: `mvn --version`

### Verify Java Installation
```powershell
java --version
# Should show Java 17 or higher
```

---

## 🚀 Running the System

### Option A: Using IntelliJ IDEA or Eclipse (Recommended)

**Backend:**
1. Open IntelliJ/Eclipse
2. Import Maven project: `backend/pom.xml`
3. Run `MonitoringApplication.java` main method
4. Backend starts on `http://localhost:8080`

**Desktop Agent:**
1. Import Maven project: `desktop-agent/pom.xml`
2. Run `MonitoringAgent.java` main method
3. Desktop UI will appear

**Web UI:**
```powershell
cd "web-ui"
npm start
```

### Option B: After Installing Maven

**Terminal 1 - Backend:**
```powershell
cd "backend"
mvn spring-boot:run
```

**Terminal 2 - Desktop Agent:**
```powershell
cd "desktop-agent"
mvn javafx:run
```

**Terminal 3 - Web UI:**
```powershell  
cd "web-ui"
npm start
```

---

## 🐛 Troubleshooting

### Path with Special Characters

The `#` in the directory name may cause issues. Consider:

**Solution 1: Rename Directory**
```powershell
# Move to a simpler path
# From: D:\#Career\Companies\Employee Activity Monitoring System
# To:   D:\Projects\Employee-Monitoring
```

**Solution 2: Use IDE**
- IntelliJ IDEA and Eclipse handle special characters better
- Import as Maven projects and run from IDE

### Web UI Port 3000 Already in Use
```powershell
# Kill process on port 3000
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Or use different port
set PORT=3001 && npm start
```

### Backend Port 8080 Already in Use
Edit `backend/src/main/resources/application.yml`:
```yaml
server:
  port: 8081  # Change port
```

---

## ✅ Verification Steps

1. **Backend Running**
   - Open: http://localhost:8080/h2-console
   - Should see H2 database console

2. **Desktop Agent Running**
   - JavaFX window appears
   - Can enter User ID
   - Start/Stop buttons visible

3. **Web UI Running**
   - Opens automatically: http://localhost:3000
   - Shows dashboard

4. **Test End-to-End**
   - Enter User ID in desktop agent
   - Click "Start Work"
   - Wait 30 seconds
   - Refresh web UI dashboard
   - Your session should appear!

---

## 📦 Building Executable JARs

Once Maven is installed:

**Backend JAR:**
```powershell
cd backend
mvn clean package
java -jar target/employee-monitoring-backend-1.0.0.jar
```

**Desktop Agent JAR:**
```powershell
cd desktop-agent
mvn clean package
java -jar target/desktop-agent-1.0.0.jar
```

**Web UI Production:**
```powershell
cd web-ui
npm run build
# Serve the build/ folder
```

---

## 🎯 Quick Start (Using IDE - No Maven Install Needed)

1. **Open IntelliJ IDEA**
2. **Import Projects:**
   - File → Open → Select `backend` folder
   - File → Open → Select `desktop-agent` folder (in new window)
3. **Run Backend:**
   - Right-click `MonitoringApplication.java`
   - Click "Run"
4. **Run Desktop Agent:**
   - Right-click `MonitoringAgent.java`
   - Click "Run"
5. **Run Web UI:**
   ```powershell
   cd web-ui
   npm start
   ```

---

## 📝 Next Steps

1. Install Maven OR use IDE
2. Start all three components
3. Test the system:
   - Start a work session
   - Generate some activity
   - View in web dashboard
   - Wait for screenshot (5 minutes)
   - Stop the session

---

## 💡 Alternative: Move Project to Simpler Path

To avoid all path-related issues:

```powershell
# Create new directory
mkdir D:\Projects
# Move project
Move-Item "D:\#Career\Companies\Employee Activity Monitoring System" "D:\Projects\EmployeeMonitoring"
# Then run from new location
```
