import React, { useState, useEffect } from 'react';
import api from '../api';
import { toast } from 'react-toastify';

const QuestionFormModal = ({ isOpen, question, quizId, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    text: '',
    type: 'MULTIPLE_CHOICE',
    points: 1,
    options: [{ text: '', isCorrect: false }, { text: '', isCorrect: false }]
  });

  useEffect(() => {
    if (question) {
      setFormData({
        text: question.text || '',
        type: question.type || 'MULTIPLE_CHOICE',
        points: question.points || 1,
        options: question.options || [{ text: '', isCorrect: false }, { text: '', isCorrect: false }]
      });
    } else {
      // Reset form when creating new question
      setFormData({
        text: '',
        type: 'MULTIPLE_CHOICE',
        points: 1,
        options: [{ text: '', isCorrect: false }, { text: '', isCorrect: false }]
      });
    }
  }, [question, isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!formData.text || formData.text.trim() === '') {
      toast.error('Question text is required');
      return;
    }
    
    if (!quizId) {
      toast.error('Quiz ID is missing');
      return;
    }
    
    // For MULTIPLE_CHOICE, validate options
    if (formData.type === 'MULTIPLE_CHOICE') {
      const validOptions = formData.options.filter(opt => opt.text && opt.text.trim() !== '');
      if (validOptions.length < 2) {
        toast.error('At least 2 options are required for multiple choice questions');
        return;
      }
      const hasCorrect = validOptions.some(opt => opt.isCorrect);
      if (!hasCorrect) {
        toast.error('At least one option must be marked as correct');
        return;
      }
    }
    
    // For TRUE_FALSE, create default options if not provided
    let optionsToSend = formData.options;
    if (formData.type === 'TRUE_FALSE') {
      optionsToSend = [
        { text: 'True', isCorrect: false },
        { text: 'False', isCorrect: false }
      ];
      // Note: The correct answer should be set based on the question logic
      // For now, we'll let the instructor set it manually
    }
    
    try {
      const payload = { 
        ...formData, 
        quizId,
        options: optionsToSend
      };
      
      console.log('[QuestionFormModal] Submitting question:', payload);
      
      if (question) {
        await api.put(`/questions/${question.id}`, payload);
        toast.success('Question updated successfully');
      } else {
        await api.post('/questions', payload);
        toast.success('Question created successfully');
      }
      onSuccess();
      onClose();
    } catch (error) {
      console.error('[QuestionFormModal] Error saving question:', error);
      console.error('[QuestionFormModal] Error response:', error.response);
      
      let errorMessage = 'Failed to save question';
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error;
        } else {
          errorMessage = JSON.stringify(error.response.data);
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      toast.error(`Failed to save question: ${errorMessage}`);
    }
  };

  const addOption = () => {
    setFormData({
      ...formData,
      options: [...formData.options, { text: '', isCorrect: false }]
    });
  };

  const updateOption = (index, field, value) => {
    const newOptions = [...formData.options];
    newOptions[index][field] = value;
    if (field === 'isCorrect' && value) {
      newOptions.forEach((opt, i) => {
        if (i !== index) opt.isCorrect = false;
      });
    }
    setFormData({ ...formData, options: newOptions });
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>{question ? 'Edit Question' : 'Add Question'}</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Question Text:</label>
            <textarea
              value={formData.text}
              onChange={(e) => setFormData({...formData, text: e.target.value})}
              required
            />
          </div>
          
          <div className="form-group">
            <label>Type:</label>
            <select
              value={formData.type}
              onChange={(e) => setFormData({...formData, type: e.target.value})}
            >
              <option value="MULTIPLE_CHOICE">Multiple Choice</option>
              <option value="TRUE_FALSE">True/False</option>
            </select>
          </div>
          
          <div className="form-group">
            <label>Points:</label>
            <input
              type="number"
              value={formData.points}
              onChange={(e) => setFormData({...formData, points: parseInt(e.target.value)})}
              min="1"
              required
            />
          </div>
          
          {formData.type === 'MULTIPLE_CHOICE' && (
            <div className="form-group">
              <label>Options:</label>
              {formData.options.map((option, index) => (
                <div key={index} className="option-input">
                  <input
                    type="text"
                    placeholder={`Option ${index + 1}`}
                    value={option.text}
                    onChange={(e) => updateOption(index, 'text', e.target.value)}
                  />
                  <label>
                    <input
                      type="radio"
                      name="correct"
                      checked={option.isCorrect}
                      onChange={(e) => updateOption(index, 'isCorrect', e.target.checked)}
                    />
                    Correct
                  </label>
                </div>
              ))}
              <button type="button" onClick={addOption}>Add Option</button>
            </div>
          )}
          
          <div className="modal-actions">
            <button type="button" onClick={onClose}>Cancel</button>
            <button type="submit">Save</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default QuestionFormModal;