import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';

const ProtectedRoute = ({ element, requiredRole }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div style={{ padding: '40px', textAlign: 'center' }}>
        <p>Loading...</p>
      </div>
    );
  }

  // Check if user is authenticated
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Verify token exists and is valid
  const token = localStorage.getItem('token');
  if (!token) {
    localStorage.removeItem('user');
    return <Navigate to="/login" replace />;
  }

  // Check role-based access. `requiredRole` can be a string or an array.
  if (requiredRole) {
    const allowed = Array.isArray(requiredRole) ? requiredRole : [requiredRole];
    if (!allowed.includes(user.role)) {
      // Show error message
      toast.error('You do not have permission to access this page');
      // Choose sensible redirects based on role
      const redirectTo = user.role === 'ADMIN' ? '/' : (user.role === 'STUDENT' ? '/quizzes' : '/quizzes');
      return <Navigate to={redirectTo} replace />;
    }
  }

  return element;
};

export default ProtectedRoute;
