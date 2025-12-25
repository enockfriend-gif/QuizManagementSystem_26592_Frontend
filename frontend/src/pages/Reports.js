import React, { useState, useEffect } from 'react';
import api from '../api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { toast } from 'react-toastify';

const COLORS = ['#7c5dff', '#ff6b6b', '#ffd93d', '#6bcf7f', '#4ecdc4', '#45b7d1'];

const Reports = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReports();
    
    // Set up real-time auto-refresh interval (every 5 seconds)
    const refreshInterval = setInterval(() => {
      // Only refresh if tab is visible
      if (!document.hidden) {
        fetchReports(false); // Don't show loading spinner on auto-refresh
      }
    }, 5000); // 5 seconds for real-time updates
    
    // Cleanup interval on unmount
    return () => {
      clearInterval(refreshInterval);
    };
  }, []);

  // Listen for visibility changes to refresh when tab becomes visible
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        fetchReports(false); // Don't show loading spinner on visibility refresh
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  const fetchReports = async (showLoading = true) => {
    try {
      if (showLoading) {
        setLoading(true);
      }
      const response = await api.get('/reports/stats');
      console.log('Reports data fetched:', response.data);
      console.log('Quiz Performance data:', response.data?.quizPerformance);
      setStats(response.data);
    } catch (error) {
      console.error('Error fetching reports:', error);
      if (showLoading) {
        toast.error('Failed to load reporting stats');
      }
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  };

  const handleExportCsv = async () => {
    try {
      toast.info('Exporting CSV file...');
      const response = await api.get('/reports/scores/csv', { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'quiz-scores.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      toast.success('CSV file exported successfully');
    } catch (error) {
      console.error('Error exporting CSV:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Failed to export CSV';
      toast.error(`Failed to export CSV: ${errorMessage}`);
    }
  };

  if (loading) {
    return (
      <div className="reports-page">
        <h1 className="page-title">Reports & Analytics</h1>
        <div style={{ padding: '40px', textAlign: 'center' }}>
          <div className="loader">Loading analytics...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="reports-page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <h1 className="page-title">Reports & Analytics</h1>
          <p className="muted">Detailed insights into quiz performance and user activity.</p>
        </div>
        <button className="btn btn--secondary" onClick={handleExportCsv}>
          Export Scores (CSV)
        </button>
      </div>

      <div className="stats-header-grid">
        <div className="stat-card">
          <span className="label">Total Quizzes</span>
          <span className="value">{stats?.totalQuizzes || 0}</span>
        </div>
        <div className="stat-card">
          <span className="label">Total Users</span>
          <span className="value">{stats?.totalUsers || 0}</span>
        </div>
        <div className="stat-card">
          <span className="label">Total Attempts</span>
          <span className="value">{stats?.totalAttempts || 0}</span>
        </div>
        <div className="stat-card">
          <span className="label">Average Score</span>
          <span className="value">{Math.round(stats?.averageScore || 0)}%</span>
        </div>
      </div>

      <div className="charts-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginTop: '32px' }}>
        <div className="chart-card glass">
          <h3>Quiz Performance (Avg. Score)</h3>
          <div style={{ height: '300px', width: '100%' }}>
            {stats?.quizPerformance && stats.quizPerformance.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={stats.quizPerformance}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,0.1)" />
                  <XAxis dataKey="title" stroke="#9fb1d0" />
                  <YAxis stroke="#9fb1d0" />
                  <Tooltip
                    contentStyle={{ background: '#1a1c2e', border: 'none', borderRadius: '8px', color: '#fff' }}
                    itemStyle={{ color: '#7c5dff' }}
                  />
                  <Bar dataKey="avgScore" fill="#7c5dff" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: 'var(--muted)' }}>
                <p>No quiz performance data available</p>
              </div>
            )}
          </div>
        </div>

        <div className="chart-card glass">
          <h3>Distribution by Totals</h3>
          <div style={{ height: '300px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={[
                    { name: 'Quizzes', value: stats?.totalQuizzes || 1 },
                    { name: 'Users', value: stats?.totalUsers || 1 },
                    { name: 'Attempts', value: stats?.totalAttempts || 1 }
                  ]}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {COLORS.map((color, index) => (
                    <Cell key={`cell-${index}`} fill={color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Reports;