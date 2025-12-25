import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api';
import { toast } from 'react-toastify';

const QuizResults = () => {
  const { attemptId } = useParams();
  const [attempt, setAttempt] = useState(null);
  const [quiz, setQuiz] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState([]);

  const fetchResults = useCallback(async () => {
    try {
      const attemptRes = await api.get(`/attempts/${attemptId}`);
      const attempt = attemptRes.data;
      setAttempt(attempt);

      // Get quizId from attempt - try multiple possible locations
      let quizId = attempt.quizId;
      
      // If quizId property doesn't exist, try to get it from quiz object
      if (!quizId && attempt.quiz) {
        quizId = attempt.quiz.id;
      }
      
      // Log the attempt data for debugging
      console.log('Attempt data received:', {
        attemptId: attempt.id,
        quizId: quizId,
        hasQuizIdProperty: attempt.hasOwnProperty('quizId'),
        quizObject: attempt.quiz,
        quizObjectId: attempt.quiz?.id,
        allKeys: Object.keys(attempt)
      });

      if (!quizId) {
        console.error('Full attempt data:', JSON.stringify(attempt, null, 2));
        console.error('Attempt quiz object:', attempt.quiz);
        console.error('Attempt quizId property:', attempt.quizId);
        toast.error('Quiz ID not found. Unable to load quiz results.');
        return;
      }

      // Use quiz data from attempt if available, otherwise fetch it
      let quizData = attempt.quiz;
      if (!quizData || !quizData.title) {
        try {
          const quizRes = await api.get(`/quizzes/${quizId}`);
          quizData = quizRes.data;
        } catch (quizError) {
          // If we can't fetch quiz details (e.g., 403 for students who attempted),
          // try to use minimal quiz data from attempt
          if (quizError.response?.status === 403) {
            console.warn('Cannot fetch quiz details (403), using data from attempt');
            quizData = attempt.quiz || { id: quizId, title: 'Quiz', durationMinutes: 0 };
          } else {
            throw quizError;
          }
        }
      }

      // Fetch questions and answers
      const [questionsRes, answersRes] = await Promise.all([
        api.get(`/quizzes/${quizId}/questions`).catch(err => {
          // If questions endpoint fails, return empty array
          console.warn('Could not fetch questions:', err);
          return { data: [] };
        }),
        api.get(`/attempts/${attemptId}/answers`)
      ]);

      setQuiz(quizData);
      setQuestions(questionsRes.data || []);
      setAnswers(answersRes.data);
    } catch (error) {
      console.error('Error loading quiz results:', error);
      console.error('Error response:', error.response);
      console.error('Error details:', error.response?.data);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Failed to load results';
      toast.error(`Error: ${errorMessage}`);
    }
  }, [attemptId]);

  useEffect(() => {
    fetchResults();
  }, [fetchResults]);

  const getAnswerForQuestion = (questionId) => {
    // Handle both questionId property and question.id nested object
    return answers.find(a => a.questionId === questionId || a.question?.id === questionId);
  };

  const isCorrectAnswer = (question, answer) => {
    if (!answer) return false;
    
    if (question.type === 'MULTIPLE_CHOICE') {
      const correctOption = question.options?.find(o => o.isCorrect);
      if (!correctOption) return false;
      
      // Handle both selectedOptionId (Long) and selectedOption (String) formats
      const selectedOptionId = answer.selectedOptionId || answer.selectedOption;
      const correctOptionId = correctOption.id.toString();
      
      return selectedOptionId && selectedOptionId.toString() === correctOptionId;
    }
    
    // For TRUE_FALSE or other types, check if isCorrect flag exists
    if (answer.isCorrect !== undefined) {
      return answer.isCorrect;
    }
    
    return false;
  };

  if (!attempt || !quiz) return <div>Loading results...</div>;

  const totalPoints = questions.reduce((sum, q) => sum + (q.points || 0), 0);
  const percentage = totalPoints > 0 ? Math.round((attempt.score / totalPoints) * 100) : 0;

  return (
    <div className="quiz-results">
      <div className="results-header">
        <h1>Quiz Results</h1>
        <div className="score-summary">
          <div className="score-card">
            <h2>{attempt.score}/{totalPoints}</h2>
            <p>Points Earned</p>
          </div>
          <div className="score-card">
            <h2>{percentage}%</h2>
            <p>Percentage</p>
          </div>
          <div className="score-card">
            <h2>{attempt.status}</h2>
            <p>Status</p>
          </div>
        </div>
      </div>

      <div className="quiz-info">
        <h3>{quiz.title}</h3>
        <p>Submitted: {new Date(attempt.submittedAt).toLocaleString()}</p>
        <p>Duration: {quiz.durationMinutes} minutes</p>
      </div>

      <div className="questions-review">
        <h3>Question Review</h3>
        {questions.map((question, index) => {
          const answer = getAnswerForQuestion(question.id);
          const isCorrect = answer && isCorrectAnswer(question, answer);
          
          return (
            <div key={question.id} className={`question-review ${isCorrect ? 'correct' : 'incorrect'}`}>
              <div className="question-header">
                <h4>Question {index + 1} ({question.points} points)</h4>
                <span className={`result-badge ${isCorrect ? 'correct' : 'incorrect'}`}>
                  {isCorrect ? '✓ Correct' : '✗ Incorrect'}
                </span>
              </div>
              
              <p className="question-text">{question.text}</p>
              
              {question.type === 'MULTIPLE_CHOICE' && (
                <div className="options-review">
                  {question.options?.map((option, optIndex) => {
                    // Handle both selectedOptionId (Long) and selectedOption (String) formats
                    const selectedOptionId = answer?.selectedOptionId || answer?.selectedOption;
                    const isSelected = selectedOptionId && selectedOptionId.toString() === option.id.toString();
                    const isCorrectOption = option.isCorrect;
                    
                    return (
                      <div key={option.id} className={`option-review ${
                        isSelected ? 'selected' : ''
                      } ${isCorrectOption ? 'correct-option' : ''}`}>
                        <span className="option-letter">{String.fromCharCode(65 + optIndex)}.</span>
                        <span className="option-text">{option.text}</span>
                        {isSelected && <span className="selected-indicator">Your Answer</span>}
                        {isCorrectOption && <span className="correct-indicator">✓ Correct Answer</span>}
                      </div>
                    );
                  })}
                </div>
              )}
              
              {question.type === 'TRUE_FALSE' && (
                <div className="tf-review">
                  <p><strong>Your Answer:</strong> {answer?.textAnswer || answer?.selectedOption || 'Not answered'}</p>
                  <p><strong>Correct Answer:</strong> {question.correctAnswer || 'True'}</p>
                  {answer?.isCorrect !== undefined && (
                    <p><strong>Result:</strong> {answer.isCorrect ? '✓ Correct' : '✗ Incorrect'}</p>
                  )}
                </div>
              )}
              
              {question.type === 'SHORT_ANSWER' && answer && (
                <div className="text-review">
                  <p><strong>Your Answer:</strong> {answer.textAnswer || 'Not answered'}</p>
                  {answer.isCorrect !== undefined && (
                    <p><strong>Result:</strong> {answer.isCorrect ? '✓ Correct' : '✗ Incorrect'}</p>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default QuizResults;