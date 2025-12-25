import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';
import { toast } from 'react-toastify';

const TakeQuiz = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [quiz, setQuiz] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [timeLeft, setTimeLeft] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchQuiz = useCallback(async () => {
    try {
      // First check if user has already attempted this quiz
      const attemptsRes = await api.get('/attempts/my-attempts');
      const quizIdNum = parseInt(id);
      const existingAttempt = attemptsRes.data.find(attempt => {
        const attemptQuizId = attempt.quizId || attempt.quiz?.id;
        return attemptQuizId === quizIdNum || attemptQuizId === id || String(attemptQuizId) === String(id);
      });
      
      if (existingAttempt) {
        toast.error('You have already attempted this quiz. Each quiz can only be taken once.');
        navigate('/student-dashboard');
        return;
      }

      // Try to load quiz questions - this will also check on backend
      const [quizRes, questionsRes] = await Promise.all([
        api.get(`/quizzes/${id}`),
        api.get(`/quizzes/${id}/questions`)
      ]);
      
      // If we get here, backend check passed
      setQuiz(quizRes.data);
      setQuestions(questionsRes.data);
      setTimeLeft(quizRes.data.durationMinutes * 60);
    } catch (error) {
      console.error('Error loading quiz:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Failed to load quiz';
      
      // Check if error is about already attempted
      if (errorMessage.includes('already attempted') || errorMessage.includes('already attempted')) {
        toast.error('You have already attempted this quiz. Each quiz can only be taken once.');
        navigate('/student-dashboard');
        return;
      }
      
      toast.error(errorMessage);
      navigate('/student-dashboard');
    } finally {
      setLoading(false);
    }
  }, [id, navigate]);

  useEffect(() => {
    fetchQuiz();
  }, [fetchQuiz]);

  const handleSubmit = useCallback(async () => {
    try {
      console.log('[TakeQuiz] Submitting quiz:', id, 'with answers:', answers);
      const response = await api.post(`/quizzes/${id}/submit`, { answers });
      console.log('[TakeQuiz] Quiz submitted successfully:', response.data);
      toast.success('Quiz submitted successfully!');
      // Navigate to results page if attempt ID is available
      if (response.data?.id) {
        navigate(`/quiz-results/${response.data.id}`);
      } else {
        navigate('/student-dashboard');
      }
    } catch (error) {
      console.error('[TakeQuiz] Error submitting quiz:', error);
      console.error('[TakeQuiz] Error response:', error.response?.data);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Failed to submit quiz';
      toast.error(`Failed to submit quiz: ${errorMessage}`);
      
      // If already attempted, redirect to dashboard
      if (errorMessage.includes('already attempted')) {
        navigate('/student-dashboard');
      }
    }
  }, [id, answers, navigate]);

  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    } else if (timeLeft === 0 && quiz) {
      handleSubmit();
    }
  }, [timeLeft, quiz, handleSubmit]);
  const handleAnswerChange = (questionId, answer) => {
    setAnswers(prev => ({ ...prev, [questionId]: answer }));
  };

  

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  if (loading) return <div>Loading quiz...</div>;

  return (
    <div className="take-quiz">
      <div className="quiz-header">
        <h1>{quiz.title}</h1>
        <div className="timer">Time Left: {formatTime(timeLeft)}</div>
      </div>
      
      {questions.map((question, index) => (
        <div key={question.id} className="question-card">
          <h3>Question {index + 1}</h3>
          <p>{question.text}</p>
          
          {question.type === 'MULTIPLE_CHOICE' && (
            <div className="options">
              {question.options?.map(option => (
                <label key={option.id}>
                  <input
                    type="radio"
                    name={`question_${question.id}`}
                    value={option.id}
                    onChange={(e) => handleAnswerChange(question.id, e.target.value)}
                  />
                  {option.text}
                </label>
              ))}
            </div>
          )}
          
          {question.type === 'TRUE_FALSE' && (
            <div className="options">
              {question.options?.map(option => (
                <label key={option.id}>
                  <input
                    type="radio"
                    name={`question_${question.id}`}
                    value={option.id}
                    onChange={(e) => handleAnswerChange(question.id, e.target.value)}
                  />
                  {option.text}
                </label>
              ))}
            </div>
          )}
        </div>
      ))}
      
      <button onClick={handleSubmit} className="submit-btn">
        Submit Quiz
      </button>
    </div>
  );
};

export default TakeQuiz;