import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';
import Button from '../components/ui/Button';
import { toast } from 'react-toastify';

const QuizPreview = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [quiz, setQuiz] = useState(null);
  const [questions, setQuestions] = useState([]);

  const fetchQuizData = useCallback(async () => {
    try {
      const [quizRes, questionsRes] = await Promise.all([
        api.get(`/quizzes/${id}`),
        api.get(`/quizzes/${id}/questions`)
      ]);
      setQuiz(quizRes.data);
      setQuestions(questionsRes.data);
    } catch (error) {
      console.error('Error loading quiz preview:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Failed to load quiz';
      toast.error(`Failed to load quiz: ${errorMessage}`);
      navigate('/quizzes');
    }
  }, [id, navigate]);

  useEffect(() => {
    fetchQuizData();
  }, [fetchQuizData]);

  if (!quiz) return <div>Loading...</div>;

  return (
    <div className="quiz-preview">
      <div className="preview-header">
        <h1>Preview: {quiz.title}</h1>
        <div className="preview-actions">
          <Button onClick={() => navigate(`/quiz-builder/${id}`)}>Back to Builder</Button>
          <Button onClick={() => navigate('/quizzes')}>Close</Button>
        </div>
      </div>

      <div className="quiz-info-card">
        <h2>Quiz Information</h2>
        <p><strong>Title:</strong> {quiz.title}</p>
        <p><strong>Description:</strong> {quiz.description || 'No description'}</p>
        <p><strong>Duration:</strong> {quiz.durationMinutes} minutes</p>
        <p><strong>Total Questions:</strong> {questions.length}</p>
        <p><strong>Total Points:</strong> {questions.reduce((sum, q) => sum + (q.points || 0), 0)}</p>
      </div>

      <div className="questions-preview">
        <h2>Questions</h2>
        {questions.length === 0 ? (
          <p>No questions added to this quiz yet.</p>
        ) : (
          questions.map((question, index) => (
            <div key={question.id} className="preview-question">
              <div className="question-header">
                <h3>Question {index + 1} ({question.points} points)</h3>
                <span className="question-type-badge">{question.type}</span>
              </div>
              <p className="question-text">{question.text}</p>
              
              {question.type === 'MULTIPLE_CHOICE' && question.options && (
                <div className="options-preview">
                  {question.options.map((option, optIndex) => (
                    <div key={optIndex} className={`option-preview ${option.isCorrect ? 'correct-answer' : ''}`}>
                      <span className="option-letter">{String.fromCharCode(65 + optIndex)}.</span>
                      <span className="option-text">{option.text}</span>
                      {option.isCorrect && <span className="correct-indicator">✓ Correct</span>}
                    </div>
                  ))}
                </div>
              )}
              
              {question.type === 'TRUE_FALSE' && (
                <div className="options-preview">
                  <div className="option-preview">
                    <span className="option-letter">A.</span>
                    <span className="option-text">True</span>
                  </div>
                  <div className="option-preview">
                    <span className="option-letter">B.</span>
                    <span className="option-text">False</span>
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default QuizPreview;