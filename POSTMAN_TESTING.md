# Testing APIs with Postman - Employee Monitoring System

This walkthrough demonstrates how to test all backend REST APIs using Postman. The system consists of session management, screenshot upload, and activity logging endpoints.

## Prerequisites

- **Postman** installed ([Download Here](https://www.postman.com/downloads/))
- **Backend server** running on `http://localhost:8080` (default)

## Setup

### 1. Create Postman Environment

Create an environment with these variables:

| Variable | Value |
|----------|-------|
| `baseUrl` | `http://localhost:8080` |
| `sessionId` | _(will be set dynamically)_ |

**To create**: Click the **Environment** icon → **Create Environment** → Add variables above.

---

## Session Management APIs

### 1. Start Session

**Creates a new monitoring session for a user.**

```
POST {{baseUrl}}/api/sessions/start
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "userId": "john.doe"
}
```

**Expected Response (201 Created):**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "john.doe",
  "startTime": "2026-01-18T11:00:00",
  "endTime": null,
  "status": "ACTIVE"
}
```

> **💡 Tip**: Copy the `sessionId` from the response and set it in your environment variables for subsequent requests.

**Postman Script** (to auto-save `sessionId`):
- Go to **Tests** tab and add:
```javascript
pm.environment.set("sessionId", pm.response.json().sessionId);
```

---

### 2. Get All Sessions

**Retrieves all sessions, optionally filtered by user or status.**

```
GET {{baseUrl}}/api/sessions
```

**Query Parameters (all optional):**
| Parameter | Example | Description |
|-----------|---------|-------------|
| `userId` | `john.doe` | Filter by specific user |
| `status` | `ACTIVE` | Filter by status (ACTIVE/COMPLETED) |

**Examples:**
- Get all sessions: `GET {{baseUrl}}/api/sessions`
- Get user's sessions: `GET {{baseUrl}}/api/sessions?userId=john.doe`
- Get active sessions: `GET {{baseUrl}}/api/sessions?status=ACTIVE`

**Expected Response (200 OK):**
```json
[
  {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "john.doe",
    "startTime": "2026-01-18T11:00:00",
    "endTime": null,
    "status": "ACTIVE"
  }
]
```

---

### 3. Get Specific Session

**Retrieves details of a single session by ID.**

```
GET {{baseUrl}}/api/sessions/{{sessionId}}
```

**Expected Response (200 OK):**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "john.doe",
  "startTime": "2026-01-18T11:00:00",
  "endTime": null,
  "status": "ACTIVE"
}
```

---

### 4. Stop Session

**Ends an active session.**

```
POST {{baseUrl}}/api/sessions/{{sessionId}}/stop
```

**No body required.**

**Expected Response (200 OK):**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "john.doe",
  "startTime": "2026-01-18T11:00:00",
  "endTime": "2026-01-18T12:30:00",
  "status": "COMPLETED"
}
```

---

## Screenshot APIs

### 5. Upload Screenshot

**Uploads a screenshot for a session with optional metadata.**

```
POST {{baseUrl}}/api/screenshots
```

**Body (form-data):**

| Key | Type | Value | Description |
|-----|------|-------|-------------|
| `sessionId` | Text | `{{sessionId}}` | Session UUID |
| `file` | File | _(select image file)_ | PNG/JPG screenshot |
| `metadata` | Text | `{"windowTitle":"Chrome - Gmail","processId":1234}` | Optional: Window context |

**Steps in Postman:**
1. Select **Body** tab
2. Choose **form-data**
3. Add keys as shown above
4. For `file`: Change type dropdown to **File**, then click **Select Files**

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "capturedAt": "2026-01-18T11:15:00",
  "fileSize": 245678,
  "metadata": "{\"windowTitle\":\"Chrome - Gmail\",\"processId\":1234}"
}
```

---

### 6. Get Screenshots for Session

**Retrieves all screenshots for a specific session.**

```
GET {{baseUrl}}/api/screenshots/session/{{sessionId}}
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "capturedAt": "2026-01-18T11:15:00",
    "fileSize": 245678,
    "metadata": "{\"windowTitle\":\"Chrome - Gmail\",\"processId\":1234}"
  },
  {
    "id": 2,
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "capturedAt": "2026-01-18T11:20:00",
    "fileSize": 198432,
    "metadata": "{\"windowTitle\":\"Visual Studio Code\",\"processId\":5678}"
  }
]
```

---

### 7. Download Screenshot Image

**Downloads the actual screenshot file.**

```
GET {{baseUrl}}/api/screenshots/{{screenshotId}}/image
```

Replace `{{screenshotId}}` with the `id` from previous responses (e.g., `1`).

**Expected Response:**
- **Status**: 200 OK
- **Content-Type**: `image/png`
- **Body**: Binary image data

**To view in Postman**: Click **Send** → The image will display in the response preview.

---

## Activity APIs

### 8. Log Activity

**Records user activity status (ACTIVE/IDLE) during a session.**

```
POST {{baseUrl}}/api/activity
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "activityStatus": "ACTIVE",
  "metadata": "5 keystrokes, 3 mouse clicks"
}
```

**Activity Status Values:**
- `ACTIVE` - User is actively using the computer
- `IDLE` - User is idle (no keyboard/mouse activity)

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "loggedAt": "2026-01-18T11:10:00",
  "activityStatus": "ACTIVE",
  "metadata": "5 keystrokes, 3 mouse clicks"
}
```

---

### 9. Get Activity Logs for Session

**Retrieves all activity logs for a specific session.**

```
GET {{baseUrl}}/api/activity/session/{{sessionId}}
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "loggedAt": "2026-01-18T11:10:00",
    "activityStatus": "ACTIVE",
    "metadata": "5 keystrokes, 3 mouse clicks"
  },
  {
    "id": 2,
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "loggedAt": "2026-01-18T11:11:00",
    "activityStatus": "IDLE",
    "metadata": null
  }
]
```

---

## Complete Testing Workflow

Here's a typical testing sequence:

1. **Start Session** → Save `sessionId` to environment
2. **Upload Screenshot** (with metadata) → Note the screenshot `id`
3. **Download Screenshot Image** using the `id`
4. **Log Activity** (ACTIVE)
5. **Log Activity** (IDLE)
6. **Get Activity Logs** for the session
7. **Get Screenshots** for the session
8. **Get All Sessions** to verify it exists
9. **Get Specific Session** by `sessionId`
10. **Stop Session**

---

## Tips & Best Practices

### Using Variables

- **Environment Variables**: Use `{{variableName}}` syntax
- **Auto-populate**: Use Scripts tab to save response values to variables
- Example auto-save script for screenshot ID:
  ```javascript
  pm.environment.set("screenshotId", pm.response.json().id);
  ```

### Testing Error Cases

Try these to test error handling:

- **Invalid Session ID**: Use a random UUID in session endpoints
- **Missing Required Fields**: Omit `userId` in start session
- **Invalid Activity Status**: Use value other than `ACTIVE`/`IDLE`

### Collections

Create a Postman Collection to organize all these requests:
1. Click **Collections** → **Create Collection**
2. Name it "Employee Monitoring System"
3. Add all requests to the collection
4. Use **Collection Variables** for `baseUrl`

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure backend is running on port 8080 |
| 404 Not Found | Check base URL and endpoint paths |
| 400 Bad Request | Validate JSON syntax and required fields |
| 500 Server Error | Check backend logs for details |
| File upload fails | Ensure `Content-Type` is not manually set (Postman auto-sets it for form-data) |

---

## Summary

You now have complete documentation for testing all 9 API endpoints:

**Sessions (4 endpoints):**
- POST `/api/sessions/start`
- GET `/api/sessions`
- GET `/api/sessions/{sessionId}`
- POST `/api/sessions/{sessionId}/stop`

**Screenshots (3 endpoints):**
- POST `/api/screenshots` (with metadata support)
- GET `/api/screenshots/session/{sessionId}`
- GET `/api/screenshots/{screenshotId}/image`

**Activity (2 endpoints):**
- POST `/api/activity`
- GET `/api/activity/session/{sessionId}`

Each endpoint is now fully documented with request format, expected responses, and practical examples!
