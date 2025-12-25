import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';
import Button from '../components/ui/Button';
import QuestionFormModal from '../components/QuestionFormModal';
import { toast } from 'react-toastify';

const QuizBuilder = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [quiz, setQuiz] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState(null);

  const fetchQuizData = useCallback(async () => {
    try {
      const [quizRes, questionsRes] = await Promise.all([
        api.get(`/quizzes/${id}`),
        api.get(`/quizzes/${id}/questions`)
      ]);
      setQuiz(quizRes.data);
      setQuestions(questionsRes.data);
    } catch (error) {
      console.error('Error loading quiz builder:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Failed to load quiz';
      toast.error(`Failed to load quiz: ${errorMessage}`);
      navigate('/quizzes');
    }
  }, [id, navigate]);

  useEffect(() => {
    fetchQuizData();
  }, [fetchQuizData]);

  const totalPoints = questions.reduce((sum, q) => sum + (q.points || 0), 0);

  const handleDeleteQuestion = async (questionId) => {
    if (window.confirm('Are you sure you want to delete this question?')) {
      try {
        await api.delete(`/questions/${questionId}`);
        toast.success('Question deleted');
        fetchQuizData();
      } catch (error) {
        toast.error('Failed to delete question');
      }
    }
  };

  const handlePreview = () => {
    navigate(`/quiz-preview/${id}`);
  };

  const handlePublish = async () => {
    try {
      await api.put(`/quizzes/${id}`, { ...quiz, status: 'PUBLISHED' });
      toast.success('Quiz published successfully');
      navigate('/quizzes');
    } catch (error) {
      toast.error('Failed to publish quiz');
    }
  };

  if (!quiz) return (
    <div style={{ padding: '40px', textAlign: 'center' }}>
      <div className="loader">Loading editor...</div>
    </div>
  );

  return (
    <div className="quiz-builder-container">
      {/* Sticky Header with Stats */}
      <div className="builder-sticky-header glass">
        <div className="container">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h1 style={{ margin: 0, fontSize: '1.5rem' }}>Editor: {quiz.title}</h1>
              <div style={{ display: 'flex', gap: '16px', marginTop: '4px' }}>
                <span className="badge">{questions.length} Questions</span>
                <span className="badge badge--primary">{totalPoints} Total Points</span>
                <span className={`badge ${quiz.status === 'PUBLISHED' ? 'badge--success' : 'badge--warning'}`}>
                  {quiz.status}
                </span>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '12px' }}>
              <Button variant="secondary" onClick={handlePreview}>Preview</Button>
              <Button onClick={handlePublish} disabled={questions.length === 0}>
                {quiz.status === 'PUBLISHED' ? 'Update & Republish' : 'Publish Quiz'}
              </Button>
              <button className="btn--close" onClick={() => navigate('/quizzes')}>×</button>
            </div>
          </div>
        </div>
      </div>

      <div className="builder-content container" style={{ marginTop: '100px', paddingBottom: '100px' }}>
        <div className="section-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <h2>Question Bank</h2>
          <Button onClick={() => setShowModal(true)}>+ Add New Question</Button>
        </div>

        {questions.length === 0 ? (
          <div className="empty-builder-state glass">
            <div className="empty-icon">📝</div>
            <h3>No questions yet</h3>
            <p className="muted">Start building your quiz by adding your first question.</p>
            <Button onClick={() => setShowModal(true)}>Create First Question</Button>
          </div>
        ) : (
          <div className="builder-questions-list">
            {questions.map((question, index) => (
              <div key={question.id} className="builder-q-card glass">
                <div className="q-card-header">
                  <span className="q-index">Question {index + 1}</span>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <span className="q-type-badge">{question.type}</span>
                    <span className="q-points-badge">{question.points} pts</span>
                  </div>
                </div>

                <div className="q-body">
                  <p className="q-text">{question.text}</p>

                  {question.options && (
                    <div className="q-options-preview">
                      {question.options.map((opt, i) => (
                        <div key={i} className={`q-opt-item ${opt.isCorrect ? 'is-correct' : ''}`}>
                          <span className="opt-bullet">{String.fromCharCode(65 + i)}</span>
                          <span className="opt-text">{opt.text}</span>
                          {opt.isCorrect && <span className="correct-mark">✓</span>}
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <div className="q-footer">
                  <button className="q-btn-edit" onClick={() => {
                    setEditingQuestion(question);
                    setShowModal(true);
                  }}>Edit</button>
                  <button className="q-btn-delete" onClick={() => handleDeleteQuestion(question.id)}>Remove</button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <QuestionFormModal
        isOpen={showModal}
        question={editingQuestion}
        quizId={id}
        onClose={() => {
          setShowModal(false);
          setEditingQuestion(null);
        }}
        onSuccess={fetchQuizData}
      />
    </div>
  );
};

export default QuizBuilder;