import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * PublicRoute component - Redirects authenticated users away from public pages (login, etc.)
 */
const PublicRoute = ({ element }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div style={{ padding: '40px', textAlign: 'center' }}>
        <p>Loading...</p>
      </div>
    );
  }

  // If user is already authenticated, redirect to dashboard
  if (user) {
    const token = localStorage.getItem('token');
    if (token) {
      // Redirect based on user role
      const redirectTo = user.role === 'ADMIN' ? '/' : (user.role === 'STUDENT' ? '/quizzes' : '/quizzes');
      return <Navigate to={redirectTo} replace />;
    }
  }

  return element;
};

export default PublicRoute;

