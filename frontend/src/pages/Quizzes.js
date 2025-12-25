import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import DataTable from '../components/ui/DataTable';
import Button from '../components/ui/Button';
import QuizFormModal from '../components/QuizFormModal';
import api from '../api';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';

const Quizzes = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [total, setTotal] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [editingQuiz, setEditingQuiz] = useState(null);
  const pageSize = 8;

  const handleEdit = (quiz) => {
    setEditingQuiz(quiz);
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Delete this quiz?')) {
      try {
        await api.delete(`/quizzes/${id}`);
        toast.success('Quiz deleted');
        fetchQuizzes();
      } catch (error) {
        toast.error('Failed to delete quiz');
      }
    }
  };

  const handleTakeQuiz = (quiz) => {
    navigate(`/take-quiz/${quiz.id}`);
  };

  const columns = [
    { header: 'Title', accessor: 'title' },
    { header: 'Status', accessor: 'status' },
    { header: 'Duration (minutes)', accessor: 'durationMinutes' },
    {
      header: 'Actions',
      accessor: (row) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          {user?.role === 'STUDENT' && row.status === 'PUBLISHED' && (
            <Button size="sm" onClick={() => handleTakeQuiz(row)}>Take Quiz</Button>
          )}
          {user?.role === 'INSTRUCTOR' && (
            <>
              <Button size="sm" onClick={() => handleEdit(row)}>Edit</Button>
              <Button size="sm" onClick={() => navigate(`/quiz-builder/${row.id}`)}>Build</Button>
              <Button size="sm" variant="danger" onClick={() => handleDelete(row.id)}>Delete</Button>
            </>
          )}
        </div>
      )
    }
  ];

  const fetchQuizzes = useCallback(async (showLoading = true) => {
    try {
      if (showLoading) {
        setLoading(true);
      }
      const response = await api.get('/quizzes/page', {
        params: {
          page: page,
          size: pageSize,
          q: search,
        },
      });
      setData(response.data.content || response.data);
      setTotal(response.data.totalElements || response.data.length || 0);
    } catch (error) {
      console.error('Error fetching quizzes:', error);
      if (showLoading) {
        toast.error('Failed to load quizzes. Please try again.');
      }
      setData([]);
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  }, [page, search]);

  useEffect(() => {
    fetchQuizzes();
    
    // Set up real-time auto-refresh interval (every 5 seconds)
    const refreshInterval = setInterval(() => {
      // Only refresh if tab is visible
      if (!document.hidden) {
        fetchQuizzes(false); // Don't show loading spinner on auto-refresh
      }
    }, 5000); // 5 seconds for real-time updates
    
    // Cleanup interval on unmount
    return () => {
      clearInterval(refreshInterval);
    };
  }, [fetchQuizzes]);

  // Listen for visibility changes to refresh when tab becomes visible
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        fetchQuizzes(false); // Don't show loading spinner on visibility refresh
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [fetchQuizzes]);

  if (loading && data.length === 0) {
    return (
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2>Quizzes</h2>
          <Button>Create quiz</Button>
        </div>
        <div style={{ padding: '20px', textAlign: 'center' }}>Loading...</div>
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Quizzes</h2>
        {user?.role === 'INSTRUCTOR' && (
          <Button onClick={() => setShowModal(true)}>Create Quiz</Button>
        )}
      </div>
      
      <QuizFormModal
        isOpen={showModal}
        quiz={editingQuiz}
        onClose={() => {
          setShowModal(false);
          setEditingQuiz(null);
        }}
        onSuccess={fetchQuizzes}
      />
      <DataTable
        columns={columns}
        data={data}
        page={page}
        pageSize={pageSize}
        total={total}
        onPageChange={(p) => {
          setPage(p);
        }}
        onSearch={(s) => {
          setSearch(s);
          setPage(0);
        }}
      />
    </div>
  );
};

export default Quizzes;

