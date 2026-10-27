import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('ct_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.dispatchEvent(new Event('ct:auth-expired'));
    }
    return Promise.reject(error);
  },
);

export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem('ct_token', token);
  } else {
    localStorage.removeItem('ct_token');
  }
};

export const setAuthUser = (user) => {
  if (user) {
    localStorage.setItem('ct_user', JSON.stringify(user));
  } else {
    localStorage.removeItem('ct_user');
  }
};

export const clearAuth = () => {
  localStorage.removeItem('ct_token');
  localStorage.removeItem('ct_user');
};

export default api;
