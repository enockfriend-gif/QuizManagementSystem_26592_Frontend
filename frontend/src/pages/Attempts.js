import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import DataTable from '../components/ui/DataTable';
import Button from '../components/ui/Button';
import api from '../api';
import { toast } from 'react-toastify';

const Attempts = () => {
  const navigate = useNavigate();
  
  const columns = [
    { header: 'Quiz', accessor: (row) => row.quiz?.title || 'N/A' },
    { header: 'Student', accessor: (row) => row.user?.username || 'N/A' },
    { header: 'Score', accessor: 'score' },
    { header: 'Status', accessor: 'status' },
    {
      header: 'Actions',
      accessor: (row) => (
        <Button size="sm" onClick={() => navigate(`/quiz-results/${row.id}`)}>View Results</Button>
      )
    }
  ];
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const pageSize = 10;

  const fetchAttempts = useCallback(async (showLoading = true) => {
    try {
      if (showLoading) {
        setLoading(true);
      }
      const response = await api.get('/attempts/page', {
        params: {
          page: page,
          size: pageSize,
        },
      });
      setData(response.data.content || response.data);
      setTotal(response.data.totalElements || response.data.length || 0);
    } catch (error) {
      console.error('Error fetching attempts:', error);
      if (showLoading) {
        toast.error('Failed to load attempts. Please try again.');
      }
      setData([]);
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  }, [page]);

  useEffect(() => {
    fetchAttempts();
    
    // Set up real-time auto-refresh interval (every 5 seconds)
    const refreshInterval = setInterval(() => {
      // Only refresh if tab is visible
      if (!document.hidden) {
        fetchAttempts(false); // Don't show loading spinner on auto-refresh
      }
    }, 5000); // 5 seconds for real-time updates
    
    // Cleanup interval on unmount
    return () => {
      clearInterval(refreshInterval);
    };
  }, [fetchAttempts]);

  // Listen for visibility changes to refresh when tab becomes visible
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        fetchAttempts(false); // Don't show loading spinner on visibility refresh
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [fetchAttempts]);

  
  if (loading && data.length === 0) {
    return (
      <div>
        <h2>Attempts & Reports</h2>
        <div style={{ padding: '20px', textAlign: 'center' }}>Loading...</div>
      </div>
    );
  }

  return (
    <div>
      <h2>Attempts & Reports</h2>
      <DataTable
        columns={columns}
        data={data}
        page={page}
        pageSize={pageSize}
        total={total}
        onPageChange={setPage}
      />
    </div>
  );
};

export default Attempts;

