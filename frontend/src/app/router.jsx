import { createBrowserRouter, Navigate } from 'react-router-dom';
import AppLayout from '../components/AppLayout';
import DashboardPage from '../features/dashboard/DashboardPage';
import ShipmentsPage from '../features/shipments/ShipmentsPage';
import ExceptionsPage from '../features/exceptions/ExceptionsPage';
import CustomersPage from '../features/customers/CustomersPage';
import DriversPage from '../features/drivers/DriversPage';
import VehiclesPage from '../features/vehicles/VehiclesPage';
import ReportsPage from '../features/reports/ReportsPage';
import UsersPage from '../features/users/UsersPage';
import ProfilePage from '../features/users/ProfilePage';
import SecurityPage from '../features/security/SecurityPage';
import AccessDeniedPage from '../features/auth/AccessDeniedPage';
import ProtectedRoute from '../components/ProtectedRoute';

export const router = createBrowserRouter([
  { path: '/access-denied', element: <AccessDeniedPage /> },
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'shipments', element: <ShipmentsPage /> },
      { path: 'exceptions', element: <ExceptionsPage /> },
      { path: 'customers', element: <CustomersPage /> },
      { path: 'drivers', element: <DriversPage /> },
      { path: 'vehicles', element: <VehiclesPage /> },
      { path: 'reports', element: <ReportsPage /> },
      { path: 'users', element: <ProtectedRoute allowedRoles={['ADMIN']}><UsersPage /></ProtectedRoute> },
      { path: 'profile', element: <ProfilePage /> },
      { path: 'security', element: <ProtectedRoute allowedRoles={['ADMIN', 'SECURITY_AUDITOR']}><SecurityPage /></ProtectedRoute> },
    ],
  },
  { path: '*', element: <Navigate to="/dashboard" replace /> },
]);
