import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import api, { clearAuth, setAuthToken, setAuthUser } from '../services/api';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const rawUser = localStorage.getItem('ct_user');
    return rawUser ? JSON.parse(rawUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('ct_token') || null);

  useEffect(() => {
    const clearExpiredAuth = () => {
      clearAuth();
      setToken(null);
      setUser(null);
    };

    window.addEventListener('ct:auth-expired', clearExpiredAuth);
    return () => window.removeEventListener('ct:auth-expired', clearExpiredAuth);
  }, []);

  const login = async (email, password) => {
    const loginResponse = await api.post('/api/v1/auth/login', { email, password });
    const accessToken = loginResponse.data.accessToken;
    setAuthToken(accessToken);
    setToken(accessToken);

    const profileResponse = await api.get('/api/v1/auth/me');
    setAuthUser(profileResponse.data);
    setUser(profileResponse.data);
    return profileResponse.data;
  };

  const logout = async () => {
    try {
      await api.post('/api/v1/auth/logout');
    } catch (error) {
      console.warn('Logout request failed', error);
    }

    clearAuth();
    setToken(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token && user),
      setUser,
      login,
      logout,
    }),
    [token, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
