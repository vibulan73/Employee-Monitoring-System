import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseUrl: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Session APIs
export const startSession = (userId) => {
    return api.post(`${API_BASE_URL}/sessions/start`, { userId });
};

export const stopSession = (sessionId) => {
    return api.post(`${API_BASE_URL}/sessions/${sessionId}/stop`);
};

export const getSession = (sessionId) => {
    return api.get(`${API_BASE_URL}/sessions/${sessionId}`);
};

export const getAllSessions = (params = {}) => {
    return api.get(`${API_BASE_URL}/sessions`, { params });
};

// Activity APIs
export const getActivityLogs = (sessionId) => {
    return api.get(`${API_BASE_URL}/activity/session/${sessionId}`);
};

export const logActivity = (data) => {
    return api.post(`${API_BASE_URL}/activity`, data);
};

// Screenshot APIs
export const getScreenshots = (sessionId) => {
    return api.get(`${API_BASE_URL}/screenshots/session/${sessionId}`);
};

export const getScreenshotImage = (screenshotId) => {
    return `${API_BASE_URL}/screenshots/${screenshotId}/image`;
};

// Employee APIs
export const getAllEmployees = () => {
    return api.get(`${API_BASE_URL}/employees`);
};

export const getEmployee = (id) => {
    return api.get(`${API_BASE_URL}/employees/${id}`);
};

export const createEmployee = (data) => {
    return api.post(`${API_BASE_URL}/employees`, data);
};

export const updateEmployee = (id, data) => {
    return api.put(`${API_BASE_URL}/employees/${id}`, data);
};

export const deleteEmployee = (id) => {
    return api.delete(`${API_BASE_URL}/employees/${id}`);
};

export default api;
