import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api';
import Button from '../components/ui/Button';
import { toast } from 'react-toastify';

const StudentDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [availableQuizzes, setAvailableQuizzes] = useState([]);
  const [myAttempts, setMyAttempts] = useState([]);
  const [stats, setStats] = useState({
    totalQuizzes: 0,
    completedQuizzes: 0,
    averageScore: 0,
    bestScore: 0
  });

  useEffect(() => {
    if (user?.role === 'STUDENT') {
      fetchStudentData();
      
      // Set up real-time auto-refresh interval (every 5 seconds)
      const refreshInterval = setInterval(() => {
        // Only refresh if tab is visible
        if (!document.hidden) {
          fetchStudentData();
        }
      }, 5000); // 5 seconds for real-time updates

      // Cleanup interval on unmount
      return () => {
        clearInterval(refreshInterval);
      };
    }
  }, [user]);

  // Listen for visibility changes to refresh when tab becomes visible
  useEffect(() => {
    if (user?.role !== 'STUDENT') return;

    const handleVisibilityChange = () => {
      if (!document.hidden) {
        fetchStudentData();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [user]);

  const fetchStudentData = async () => {
    try {
      const [quizzesRes, attemptsRes] = await Promise.all([
        api.get('/quizzes?status=PUBLISHED'),
        api.get('/attempts/my-attempts')
      ]);

      const quizzes = quizzesRes.data;
      const attempts = attemptsRes.data;

      setAvailableQuizzes(quizzes);
      setMyAttempts(attempts);

      // Calculate stats
      const completedAttempts = attempts.filter(a => a.status === 'GRADED');
      const scores = completedAttempts.map(a => a.score).filter(s => s != null);
      
      setStats({
        totalQuizzes: quizzes.length,
        completedQuizzes: completedAttempts.length,
        averageScore: scores.length > 0 ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length) : 0,
        bestScore: scores.length > 0 ? Math.max(...scores) : 0
      });
    } catch (error) {
      toast.error('Failed to load student data');
    }
  };

  const hasAttempted = (quizId) => {
    return myAttempts.some(attempt => {
      const attemptQuizId = attempt.quizId || attempt.quiz?.id;
      return attemptQuizId === quizId || String(attemptQuizId) === String(quizId);
    });
  };

  const getAttemptForQuiz = (quizId) => {
    return myAttempts.find(attempt => {
      const attemptQuizId = attempt.quizId || attempt.quiz?.id;
      return attemptQuizId === quizId || String(attemptQuizId) === String(quizId);
    });
  };

  const handleTakeQuiz = (quizId) => {
    // Check if user has already attempted this quiz
    const existingAttempt = getAttemptForQuiz(quizId);
    
    if (existingAttempt) {
      toast.error('You have already taken this quiz.');
      // Navigate to results page to view their attempt
      navigate(`/quiz-results/${existingAttempt.id}`);
      return;
    }
    
    // If not attempted, navigate to take quiz
    navigate(`/take-quiz/${quizId}`);
  };

  if (user?.role !== 'STUDENT') {
    return null;
  }

  return (
    <div className="student-dashboard">
      <div className="welcome-section">
        <h1>Welcome back, {user.username}!</h1>
        <p>Ready to take some quizzes?</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <h3>{stats.totalQuizzes}</h3>
          <p>Available Quizzes</p>
        </div>
        <div className="stat-card">
          <h3>{stats.completedQuizzes}</h3>
          <p>Completed</p>
        </div>
        <div className="stat-card">
          <h3>{stats.averageScore}%</h3>
          <p>Average Score</p>
        </div>
        <div className="stat-card">
          <h3>{stats.bestScore}%</h3>
          <p>Best Score</p>
        </div>
      </div>

      <div className="available-quizzes">
        <h2>Available Quizzes</h2>
        {availableQuizzes.length === 0 ? (
          <p>No quizzes available at the moment.</p>
        ) : (
          <div className="quiz-grid">
            {availableQuizzes.map(quiz => {
              const attempt = getAttemptForQuiz(quiz.id);
              const attempted = hasAttempted(quiz.id);
              
              return (
                <div key={quiz.id} className="quiz-card">
                  <h3>{quiz.title}</h3>
                  <p>{quiz.description || 'No description available'}</p>
                  <div className="quiz-meta">
                    <span>Duration: {quiz.durationMinutes} min</span>
                    <span>Status: {quiz.status}</span>
                  </div>
                  
                  {attempted ? (
                    <div className="attempt-info">
                      <p>Status: {attempt.status}</p>
                      {attempt.score != null && (
                        <p>Score: {attempt.score}%</p>
                      )}
                      <Button 
                        size="sm" 
                        onClick={() => navigate(`/quiz-results/${attempt.id}`)}
                      >
                        View Results
                      </Button>
                    </div>
                  ) : (
                    <Button 
                      onClick={() => handleTakeQuiz(quiz.id)}
                    >
                      Take Quiz
                    </Button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div className="recent-attempts">
        <h2>Recent Attempts</h2>
        {myAttempts.length === 0 ? (
          <p>No quiz attempts yet.</p>
        ) : (
          <div className="attempts-list">
            {myAttempts.slice(0, 5).map(attempt => (
              <div key={attempt.id} className="attempt-item">
                <div className="attempt-info">
                  <h4>{attempt.quiz?.title || 'Quiz'}</h4>
                  <p>Submitted: {new Date(attempt.submittedAt).toLocaleDateString()}</p>
                  <p>Status: {attempt.status}</p>
                  {attempt.score != null && <p>Score: {attempt.score}%</p>}
                </div>
                <Button 
                  size="sm" 
                  onClick={() => navigate(`/quiz-results/${attempt.id}`)}
                >
                  View
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default StudentDashboard;