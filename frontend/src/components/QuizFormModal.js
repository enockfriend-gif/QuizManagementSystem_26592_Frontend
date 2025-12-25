import React, { useState, useEffect } from 'react';
import api from '../api';
import { toast } from 'react-toastify';

const QuizFormModal = ({ isOpen, quiz, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    durationMinutes: 60,
    status: 'DRAFT'
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (quiz) {
      setFormData({
        title: quiz.title || '',
        description: quiz.description || '',
        durationMinutes: quiz.durationMinutes || 60,
        status: quiz.status || 'DRAFT'
      });
    } else {
      setFormData({
        title: '',
        description: '',
        durationMinutes: 60,
        status: 'DRAFT'
      });
    }
  }, [quiz]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      if (quiz) {
        await api.put(`/quizzes/${quiz.id}`, formData);
        toast.success('Quiz updated successfully');
      } else {
        await api.post('/quizzes', formData);
        toast.success('Quiz created successfully');
      }
      onSuccess();
      onClose();
    } catch (error) {
      console.error('Error saving quiz:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Failed to save quiz';
      toast.error(`Failed to save quiz: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>{quiz ? 'Edit Quiz' : 'Create Quiz'}</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Title:</label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({...formData, title: e.target.value})}
              required
            />
          </div>
          
          <div className="form-group">
            <label>Description:</label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
            />
          </div>
          
          <div className="form-group">
            <label>Duration (minutes):</label>
            <input
              type="number"
              value={formData.durationMinutes}
              onChange={(e) => setFormData({...formData, durationMinutes: parseInt(e.target.value)})}
              min="1"
              required
            />
          </div>
          
          <div className="form-group">
            <label>Status:</label>
            <select
              value={formData.status}
              onChange={(e) => setFormData({...formData, status: e.target.value})}
            >
              <option value="DRAFT">Draft</option>
              <option value="PUBLISHED">Published</option>
              <option value="ARCHIVED">Archived</option>
            </select>
          </div>
          
          <div className="modal-actions">
            <button type="button" onClick={onClose}>Cancel</button>
            <button type="submit" disabled={loading}>
              {loading ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default QuizFormModal;