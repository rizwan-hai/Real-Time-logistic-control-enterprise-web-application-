import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user } = useAuth();

  if (allowedRoles && user && user.roles) {
    const roles = user.roles.map((role) => String(role).toUpperCase());
    const hasAccess = allowedRoles.some((role) => roles.includes(String(role).toUpperCase()));
    if (!hasAccess) {
      return <Navigate to="/access-denied" replace />;
    }
  }

  return children;
}
