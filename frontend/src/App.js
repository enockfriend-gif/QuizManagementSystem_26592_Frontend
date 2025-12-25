import React from 'react';
import { ROLES } from './config/navigationConfig';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import PublicRoute from './components/PublicRoute';
import Dashboard from './pages/Dashboard';
import Users from './pages/Users';
import Quizzes from './pages/Quizzes';
import Questions from './pages/Questions';
import Attempts from './pages/Attempts';
import Reports from './pages/Reports';
import Profile from './pages/Profile';
import Settings from './pages/Settings';
import Locations from './pages/Locations';
import TakeQuiz from './pages/TakeQuiz';
import QuizBuilder from './pages/QuizBuilder';
import QuizPreview from './pages/QuizPreview';
import QuizResults from './pages/QuizResults';
import Login from './pages/Login';
import VerifyOtp from './pages/VerifyOtp';
import ResetPassword from './pages/ResetPassword';
import MainLayout from './components/layout/MainLayout';
import './App.css';
import './styles/quiz.css';
import './styles/quiz-extended.css';

// NotFoundRoute component - Handles 404 with role-based redirect
// Must be inside AuthProvider to access useAuth
const NotFoundRoute = () => {
  const { user } = useAuth();
  const location = useLocation();
  
  // If user is authenticated, redirect based on role
  if (user) {
    const token = localStorage.getItem('token');
    if (token) {
      const redirectTo = user.role === 'ADMIN' ? '/' : (user.role === 'STUDENT' ? '/quizzes' : '/quizzes');
      return <Navigate to={redirectTo} replace />;
    }
  }
  
  // If not authenticated, redirect to login
  return <Navigate to="/login" replace state={{ from: location }} />;
};

// AppRoutes component - Contains all routes, must be inside AuthProvider
const AppRoutes = () => {
  const { user, isLoading } = useAuth();
  
  // Default route handler - redirects to login if not authenticated
  const DefaultRoute = () => {
    if (isLoading) {
      return (
        <div style={{ padding: '40px', textAlign: 'center' }}>
          <p>Loading...</p>
        </div>
      );
    }
    
    // If not authenticated, redirect to login immediately
    const token = localStorage.getItem('token');
    if (!user || !token) {
      return <Navigate to="/login" replace />;
    }
    
    // If authenticated, show the protected dashboard
    return (
      <ProtectedRoute
        requiredRole={[ROLES.ADMIN, ROLES.INSTRUCTOR, ROLES.STUDENT]}
        element={
          <MainLayout>
            <Dashboard />
          </MainLayout>
        }
      />
    );
  };

  return (
    <BrowserRouter>
      <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} newestOnTop={false} closeOnClick rtl={false} pauseOnFocusLoss draggable pauseOnHover />
      <Routes>
          <Route 
            path="/login" 
            element={
              <PublicRoute element={<Login />} />
            } 
          />
          <Route 
            path="/verify-otp" 
            element={
              <PublicRoute element={<VerifyOtp />} />
            } 
          />
          <Route 
            path="/reset-password" 
            element={
              <PublicRoute element={<ResetPassword />} />
            } 
          />
          <Route
            path="/"
            element={<DefaultRoute />}
          />
          <Route
            path="/users"
            element={
              <ProtectedRoute
                requiredRole={ROLES.ADMIN}
                element={
                  <MainLayout>
                    <Users />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/quizzes"
            element={
              <ProtectedRoute
                requiredRole={[ROLES.INSTRUCTOR, ROLES.STUDENT]}
                element={
                  <MainLayout>
                    <Quizzes />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/attempts"
            element={
              <ProtectedRoute
                requiredRole={[ROLES.INSTRUCTOR, ROLES.STUDENT]}
                element={
                  <MainLayout>
                    <Attempts />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/questions"
            element={
              <ProtectedRoute
                requiredRole={ROLES.INSTRUCTOR}
                element={
                  <MainLayout>
                    <Questions />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/reports"
            element={
              <ProtectedRoute
                requiredRole={[ROLES.ADMIN, ROLES.INSTRUCTOR]}
                element={
                  <MainLayout>
                    <Reports />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute
                requiredRole={[ROLES.ADMIN, ROLES.INSTRUCTOR, ROLES.STUDENT]}
                element={
                  <MainLayout>
                    <Profile />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/settings"
            element={
              <ProtectedRoute
                requiredRole={ROLES.ADMIN}
                element={
                  <MainLayout>
                    <Settings />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/take-quiz/:id"
            element={
              <ProtectedRoute
                requiredRole={ROLES.STUDENT}
                element={
                  <MainLayout>
                    <TakeQuiz />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/quiz-builder/:id"
            element={
              <ProtectedRoute
                requiredRole={ROLES.INSTRUCTOR}
                element={
                  <MainLayout>
                    <QuizBuilder />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/quiz-preview/:id"
            element={
              <ProtectedRoute
                requiredRole={ROLES.INSTRUCTOR}
                element={
                  <MainLayout>
                    <QuizPreview />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/quiz-results/:attemptId"
            element={
              <ProtectedRoute
                requiredRole={[ROLES.ADMIN, ROLES.INSTRUCTOR, ROLES.STUDENT]}
                element={
                  <MainLayout>
                    <QuizResults />
                  </MainLayout>
                }
              />
            }
          />
          <Route
            path="/locations"
            element={
              <ProtectedRoute
                requiredRole={ROLES.ADMIN}
                element={
                  <MainLayout>
                    <Locations />
                  </MainLayout>
                }
              />
            }
          />
          <Route 
            path="*" 
            element={<NotFoundRoute />} 
          />
      </Routes>
    </BrowserRouter>
  );
};

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}

export default App;
