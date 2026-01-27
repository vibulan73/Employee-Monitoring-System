import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Login Rule APIs (Admin only)
export const getAllLoginRules = () => {
    return axios.get(`${API_BASE_URL}/admin/login-rules`);
};

export const getLoginRuleById = (id) => {
    return axios.get(`${API_BASE_URL}/admin/login-rules/${id}`);
};

export const createLoginRule = (data) => {
    return axios.post(`${API_BASE_URL}/admin/login-rules`, data);
};

export const updateLoginRule = (id, data) => {
    return axios.put(`${API_BASE_URL}/admin/login-rules/${id}`, data);
};

export const deleteLoginRule = (id) => {
    return axios.delete(`${API_BASE_URL}/admin/login-rules/${id}`);
};

// Employee read-only access
export const getMyLoginRule = (userId) => {
    return axios.get(`${API_BASE_URL}/employees/me/login-rule`, {
        params: { userId }
    });
};

const loginRuleService = {
    getAllLoginRules,
    getLoginRuleById,
    createLoginRule,
    updateLoginRule,
    deleteLoginRule,
    getMyLoginRule
};

export default loginRuleService;
