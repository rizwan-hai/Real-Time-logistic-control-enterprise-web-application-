import api from './api';

export const fetchDashboardSummary = async () => {
  const response = await api.get('/api/v1/dashboard/summary');
  return response.data;
};

export const fetchShipments = async ({ page = 0, size = 20, status, priority, customerId, driverId, tracking } = {}) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));
  if (status) params.set('status', status);
  if (priority) params.set('priority', priority);
  if (customerId) params.set('customerId', String(customerId));
  if (driverId) params.set('driverId', String(driverId));
  if (tracking) params.set('tracking', tracking);

  const response = await api.get(`/api/v1/shipments?${params.toString()}`);
  return response.data;
};

export const createShipment = async (payload) => {
  const response = await api.post('/api/v1/shipments', payload);
  return response.data;
};

export const updateShipmentAssignment = async (id, payload) => {
  const response = await api.put(`/api/v1/shipments/${id}/assignment`, payload);
  return response.data;
};

export const updateShipmentStatus = async (id, payload) => {
  const response = await api.put(`/api/v1/shipments/${id}/status`, payload);
  return response.data;
};

export const fetchShipmentById = async (id) => {
  const response = await api.get(`/api/v1/shipments/${id}`);
  return response.data;
};

export const fetchShipmentHistory = async (id) => {
  const response = await api.get(`/api/v1/shipments/${id}/history`);
  return response.data;
};

export const fetchCustomers = async () => {
  const response = await api.get('/api/v1/customers?page=0&size=100');
  return response.data.content || response.data;
};

export const fetchDrivers = async () => {
  const response = await api.get('/api/v1/drivers?page=0&size=100');
  return response.data.content || response.data;
};

export const fetchVehicles = async () => {
  const response = await api.get('/api/v1/vehicles?page=0&size=100');
  return response.data.content || response.data;
};

export const fetchExceptions = async ({ page = 0, size = 20, type, status, severity } = {}) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));
  if (type) params.set('type', type);
  if (status) params.set('status', status);
  if (severity) params.set('severity', severity);

  const response = await api.get(`/api/v1/exceptions?${params.toString()}`);
  return response.data;
};

export const createException = async (payload) => {
  const response = await api.post('/api/v1/exceptions', payload);
  return response.data;
};

export const fetchExceptionById = async (id) => {
  const response = await api.get(`/api/v1/exceptions/${id}`);
  return response.data;
};

export const updateExceptionStatus = async (id, payload) => {
  const response = await api.put(`/api/v1/exceptions/${id}/status`, payload);
  return response.data;
};

export const updateExceptionSeverity = async (id, payload) => {
  const response = await api.put(`/api/v1/exceptions/${id}/severity`, payload);
  return response.data;
};

export const assignException = async (id, payload) => {
  const response = await api.put(`/api/v1/exceptions/${id}/assignment`, payload);
  return response.data;
};

export const fetchUsers = async () => {
  const response = await api.get('/api/v1/users?page=0&size=100');
  return response.data.content || response.data;
};

export const fetchCurrentUser = async () => {
  const response = await api.get('/api/v1/auth/me');
  return response.data;
};

export const updateCurrentUser = async (payload) => {
  const response = await api.put('/api/v1/auth/me', payload);
  return response.data;
};

export const changePassword = async (payload) => {
  const response = await api.post('/api/v1/auth/change-password', payload);
  return response.data;
};

export const fetchNotifications = async () => {
  const response = await api.get('/api/v1/notifications?page=0&size=20');
  return response.data;
};

export const markNotificationRead = async (id) => {
  const response = await api.patch(`/api/v1/notifications/${id}/read`);
  return response.data;
};
