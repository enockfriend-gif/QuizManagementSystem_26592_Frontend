import React, { useState, useEffect, useCallback } from 'react';
import api from '../api';
import DataTable from '../components/ui/DataTable';
import Button from '../components/ui/Button';
import QuestionFormModal from '../components/QuestionFormModal';
import { toast } from 'react-toastify';

const Questions = () => {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState(null);
  const pageSize = 10;

  const fetchQuestions = useCallback(async (showLoading = true) => {
    try {
      if (showLoading) {
        setLoading(true);
      }
      const response = await api.get('/questions/page', {
        params: {
          page,
          size: pageSize,
          q: search
        }
      });
      setQuestions(response.data.content || response.data);
      setTotal(response.data.totalElements || response.data.length || 0);
    } catch (error) {
      console.error('Error fetching questions:', error);
      if (showLoading) {
        toast.error('Failed to load questions');
      }
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  }, [page, search]);

  useEffect(() => {
    fetchQuestions();
    
    // Set up real-time auto-refresh interval (every 5 seconds)
    const refreshInterval = setInterval(() => {
      // Only refresh if tab is visible
      if (!document.hidden) {
        fetchQuestions(false); // Don't show loading spinner on auto-refresh
      }
    }, 5000); // 5 seconds for real-time updates
    
    // Cleanup interval on unmount
    return () => {
      clearInterval(refreshInterval);
    };
  }, [fetchQuestions]);

  // Listen for visibility changes to refresh when tab becomes visible
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        fetchQuestions(false); // Don't show loading spinner on visibility refresh
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [fetchQuestions]);

  const handleDelete = async (id) => {
    if (window.confirm('Delete this question?')) {
      try {
        await api.delete(`/questions/${id}`);
        toast.success('Question deleted');
        fetchQuestions();
      } catch (error) {
        toast.error('Failed to delete question');
      }
    }
  };

  const columns = [
    { key: 'id', header: 'ID' },
    { key: 'text', header: 'Question Text' },
    { key: 'type', header: 'Type' },
    { key: 'quiz.title', header: 'Quiz' },
    { key: 'points', header: 'Points' },
    {
      key: (row) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          <Button size="sm" onClick={() => {
            setEditingQuestion(row);
            setShowModal(true);
          }}>Edit</Button>
          <Button size="sm" variant="danger" onClick={() => handleDelete(row.id)}>Delete</Button>
        </div>
      ),
      header: 'Actions'
    }
  ];

  return (
    <div className="questions-page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1>Questions Management</h1>
        <Button onClick={() => setShowModal(true)}>Add Question</Button>
      </div>

      <DataTable
        data={questions}
        columns={columns}
        loading={loading}
        page={page}
        pageSize={pageSize}
        total={total}
        onPageChange={setPage}
        onSearch={(s) => {
          setSearch(s);
          setPage(0);
        }}
      />

      <QuestionFormModal
        isOpen={showModal}
        question={editingQuestion}
        onClose={() => {
          setShowModal(false);
          setEditingQuestion(null);
        }}
        onSuccess={fetchQuestions}
      />
    </div>
  );
};

export default Questions;