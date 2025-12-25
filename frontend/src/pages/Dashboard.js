import React, { useState, useEffect, useCallback } from 'react';
import { AreaChart, Area, ResponsiveContainer, XAxis, YAxis, Tooltip } from 'recharts';
import { useAuth } from '../context/AuthContext';
import StudentDashboard from './StudentDashboard';
import api from '../api';
import { toast } from 'react-toastify';

// Add pulse animation style
const pulseStyle = `
  @keyframes pulse {
    0%, 100% { opacity: 1; transform: scale(1); }
    50% { opacity: 0.5; transform: scale(0.95); }
  }
`;

const getRoleColor = (role) => {
  switch (role) {
    case 'ADMIN':
      return '#ff6b6b';
    case 'INSTRUCTOR':
      return '#ffd93d';
    case 'STUDENT':
      return '#6bcf7f';
    default:
      return '#7c5dff';
  }
};

const Dashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState([
    { label: 'Active Quizzes', value: 0 },
    { label: 'Students', value: 0 },
    { label: 'Instructors', value: 0 },
    { label: 'Total Users', value: 0 },
    { label: 'Pass Rate', value: '0%' },
    { label: 'Avg. Score', value: '0%' },
  ]);
  const [activeQuizzesList, setActiveQuizzesList] = useState([]);
  const [allQuizzesList, setAllQuizzesList] = useState([]);
  const [filteredAttempts, setFilteredAttempts] = useState([]);
  const [trend, setTrend] = useState([
    { name: 'Mon', score: 0 },
    { name: 'Tue', score: 0 },
    { name: 'Wed', score: 0 },
    { name: 'Thu', score: 0 },
    { name: 'Fri', score: 0 },
    { name: 'Sat', score: 0 },
    { name: 'Sun', score: 0 },
  ]);
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const refreshInterval = 5000; // 5 seconds for real-time updates

  const fetchDashboardData = useCallback(async (showLoading = true) => {
    try {
      if (showLoading) {
        setLoading(true);
      } else {
        setIsRefreshing(true);
      }
      
      // Fetch all data in parallel with individual error tracking
      // Both ADMIN and INSTRUCTOR get the same real-time data fetching
      const results = await Promise.allSettled([
        api.get('/quizzes'),
        api.get('/users'),
        api.get('/attempts'),
      ]);

      // Check for failures and log which endpoint failed
      const endpoints = ['quizzes', 'users', 'attempts'];
      const failed = [];
      results.forEach((result, index) => {
        if (result.status === 'rejected') {
          console.error(`Failed to fetch ${endpoints[index]}:`, result.reason);
          failed.push(result);
        }
      });
      
      // If all failed, throw the first error for proper error handling
      if (failed.length === results.length) {
        throw failed[0].reason;
      }

      const quizzes = results[0].status === 'fulfilled' ? (results[0].value.data || results[0].value || []) : [];
      const users = results[1].status === 'fulfilled' ? (results[1].value.data || results[1].value || []) : [];
      const attempts = results[2].status === 'fulfilled' ? (results[2].value.data || results[2].value || []) : [];
      
      // Log data received for debugging
      console.log('[Dashboard] Data received:', {
        quizzes: quizzes.length,
        users: users.length,
        attempts: attempts.length,
        role: user?.role
      });

      // Role-based data filtering
      let filteredQuizzes = quizzes;
      let filteredUsers = users;
      let filteredAttempts = attempts;

      // INSTRUCTOR role: only see their own quizzes and related attempts
      if (user?.role === 'INSTRUCTOR') {
        console.log('[Dashboard] Filtering for INSTRUCTOR:', user.username, 'ID:', user.id);
        console.log('[Dashboard] Total quizzes before filtering:', quizzes.length);
        console.log('[Dashboard] Sample quiz data:', quizzes.length > 0 ? {
          id: quizzes[0].id,
          title: quizzes[0].title,
          createdBy: quizzes[0].createdBy,
          createdByType: typeof quizzes[0].createdBy,
          createdById: quizzes[0].createdBy?.id,
          createdByUsername: quizzes[0].createdBy?.username,
          allKeys: Object.keys(quizzes[0])
        } : 'No quizzes');
        
        // createdBy can be an object (User) or just an ID/username string
        // Also handle case where createdBy might be null due to serialization issues
        filteredQuizzes = quizzes.filter(q => {
          // Log each quiz for debugging (only first few to avoid spam)
          if (quizzes.indexOf(q) < 3) {
            console.log('[Dashboard] Checking quiz:', {
              id: q.id,
              title: q.title,
              createdBy: q.createdBy,
              createdByType: typeof q.createdBy,
              createdById: q.createdBy?.id,
              createdByUsername: q.createdBy?.username,
              userId: user.id,
              username: user.username,
              allKeys: Object.keys(q)
            });
          }
          
          // If createdBy is null or undefined, try to match by other means
          if (!q.createdBy) {
            console.warn('[Dashboard] Quiz has no createdBy field:', q.id, q.title);
            // TEMPORARY: If no createdBy, show all quizzes for debugging
            // In production, you might want to return false here
            // For now, let's show all quizzes if createdBy is missing to see if data exists
            return true; // Show all quizzes temporarily to debug
          }
          
          // Handle both object and string formats
          if (typeof q.createdBy === 'object') {
            // Check multiple possible matching conditions
            const matches = q.createdBy.id === user.id || 
                          q.createdBy.id === Number(user.id) ||
                          String(q.createdBy.id) === String(user.id) ||
                          q.createdBy.username === user.username ||
                          (q.createdBy.username && q.createdBy.username.toLowerCase() === user.username?.toLowerCase());
            
            if (matches && quizzes.indexOf(q) < 3) {
              console.log('[Dashboard] ✓ Quiz matches instructor:', q.id, q.title, 'createdBy:', q.createdBy);
            }
            return matches;
          }
          
          // Handle string/number formats
          const matches = q.createdBy === user.username || 
                        q.createdBy === user.id ||
                        q.createdBy === Number(user.id) ||
                        String(q.createdBy).toLowerCase() === String(user.username).toLowerCase() ||
                        String(q.createdBy) === String(user.id);
          
          if (matches && quizzes.indexOf(q) < 3) {
            console.log('[Dashboard] ✓ Quiz matches instructor (string format):', q.id, q.title);
          }
          return matches;
        });
        
        console.log('[Dashboard] Filtered quizzes for instructor:', filteredQuizzes.length);
        console.log('[Dashboard] Filtered quiz IDs:', filteredQuizzes.map(q => q.id));
        console.log('[Dashboard] Filtered quiz titles:', filteredQuizzes.map(q => q.title));
        console.log('[Dashboard] Total attempts before filtering:', attempts.length);
        
        // Filter attempts to only include those for instructor's quizzes
        filteredAttempts = attempts.filter(a => {
          // Check both quizId and quiz.id to handle different data formats
          const matchesQuiz = filteredQuizzes.some(q => {
            // Check if attempt's quizId matches quiz id
            if (a.quizId && (a.quizId === q.id || String(a.quizId) === String(q.id) || Number(a.quizId) === Number(q.id))) {
              return true;
            }
            // Check if attempt's quiz object has matching ID
            if (a.quiz && (a.quiz.id === q.id || String(a.quiz.id) === String(q.id) || Number(a.quiz.id) === Number(q.id))) {
              return true;
            }
            return false;
          });
          return matchesQuiz;
        });
        
        console.log('[Dashboard] Filtered attempts for instructor:', filteredAttempts.length);
        console.log('[Dashboard] Sample filtered attempts:', filteredAttempts.slice(0, 3));
        
        // Instructors can see all users for stats
        filteredUsers = users;
      }
      // ADMIN role: see everything (no filtering needed)
      // Other roles: apply appropriate filtering

      // Calculate stats based on filtered data
      const activeQuizzesListData = filteredQuizzes.filter(q => q.status === 'PUBLISHED');
      const activeQuizzes = activeQuizzesListData.length;
      const totalQuizzesCreated = filteredQuizzes.length; // Total quizzes created (all statuses including expired/closed)
      const students = filteredUsers.filter(u => u.role === 'STUDENT').length;
      const instructors = filteredUsers.filter(u => u.role === 'INSTRUCTOR').length;
      const totalUsers = filteredUsers.length;
      
      // Calculate pass rate (assuming 60% is passing) - use filtered attempts
      const gradedAttempts = filteredAttempts.filter(a => a.status === 'GRADED' && a.score != null);
      const passedAttempts = gradedAttempts.filter(a => a.score >= 60).length;
      const passRate = gradedAttempts.length > 0 
        ? Math.round((passedAttempts / gradedAttempts.length) * 100) 
        : 0;

      // Calculate average score
      const scores = gradedAttempts.map(a => a.score).filter(s => s != null);
      const avgScore = scores.length > 0
        ? Math.round(scores.reduce((sum, s) => sum + s, 0) / scores.length)
        : 0;

      // Calculate engagement (number of unique students who took quizzes)
      // This includes ALL attempts - both current and historical
      const uniqueStudents = new Set();
      filteredAttempts.forEach(attempt => {
        // Handle both object and ID formats for user
        if (attempt.user) {
          if (typeof attempt.user === 'object' && attempt.user.id) {
            // Only count students (not instructors or admins)
            // If role is not available, we'll check against the users list
            if (attempt.user.role === 'STUDENT') {
              uniqueStudents.add(attempt.user.id);
            } else if (!attempt.user.role) {
              // If role is not loaded, check if user exists in filteredUsers as a student
              const userInList = filteredUsers.find(u => u.id === attempt.user.id);
              if (userInList && userInList.role === 'STUDENT') {
                uniqueStudents.add(attempt.user.id);
              }
            }
          } else if (typeof attempt.user === 'string' || typeof attempt.user === 'number') {
            // If user is just an ID, check if it's a student in the users list
            const userId = typeof attempt.user === 'string' ? parseInt(attempt.user) : attempt.user;
            const userInList = filteredUsers.find(u => u.id === userId);
            if (userInList && userInList.role === 'STUDENT') {
              uniqueStudents.add(userId);
            }
          }
        } else if (attempt.userId) {
          // If only userId is available, check if it's a student in the users list
          const userId = typeof attempt.userId === 'string' ? parseInt(attempt.userId) : attempt.userId;
          const userInList = filteredUsers.find(u => u.id === userId);
          if (userInList && userInList.role === 'STUDENT') {
            uniqueStudents.add(userId);
          }
        }
      });
      const engagementValue = uniqueStudents.size; // Number of unique students who took quizzes
      
      // Debug log to verify calculation
      console.log(`[Dashboard] Engagement calculation: ${filteredAttempts.length} attempts, ${uniqueStudents.size} unique students`);
      console.log(`[Dashboard] Stats for ${user?.role}:`, {
        activeQuizzes,
        totalQuizzesCreated,
        students,
        instructors,
        totalUsers,
        engagementValue,
        passRate,
        avgScore
      });

      // Store active quizzes list, all quizzes list, and filtered attempts
      setActiveQuizzesList(activeQuizzesListData);
      setAllQuizzesList(filteredQuizzes); // Store all quizzes (including expired/closed)
      setFilteredAttempts(filteredAttempts);

      // Build stats array based on user role
      let statsArray;
      
      if (user?.role === 'ADMIN') {
        // ADMIN sees all stats including user counts
        statsArray = [
          { label: 'Active Quizzes', value: activeQuizzes },
          { label: 'Students', value: students },
          { label: 'Instructors', value: instructors },
          { label: 'Total Users', value: totalUsers },
          { label: 'Students Who Took Quizzes', value: engagementValue },
          { label: 'Pass Rate', value: `${passRate}%` },
          { label: 'Avg. Score', value: `${avgScore}%` },
        ];
      } else if (user?.role === 'INSTRUCTOR') {
        // INSTRUCTOR sees only quiz-related metrics
        statsArray = [
          { label: 'Quizzes Created', value: totalQuizzesCreated },
          { label: 'Active Quizzes', value: activeQuizzes },
          { label: 'Students Who Took Quizzes', value: engagementValue },
          { label: 'Pass Rate', value: `${passRate}%` },
          { label: 'Avg. Score', value: `${avgScore}%` },
        ];
      } else {
        // Default for other roles
        statsArray = [
          { label: 'Active Quizzes', value: activeQuizzes },
          { label: 'Students Who Took Quizzes', value: engagementValue },
          { label: 'Pass Rate', value: `${passRate}%` },
          { label: 'Avg. Score', value: `${avgScore}%` },
        ];
      }
      
      setStats(statsArray);

      // Generate trend data from recent attempts (last 7 days) - use filtered attempts
      const now = new Date();
      const trendData = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((day, index) => {
        const dayDate = new Date(now);
        dayDate.setDate(dayDate.getDate() - (6 - index));
        const dayAttempts = filteredAttempts.filter(a => {
          if (!a.submittedAt) return false;
          const attemptDate = new Date(a.submittedAt);
          return attemptDate.toDateString() === dayDate.toDateString() && a.score != null;
        });
        const dayAvg = dayAttempts.length > 0
          ? Math.round(dayAttempts.reduce((sum, a) => sum + a.score, 0) / dayAttempts.length)
          : avgScore || 0;
        return { name: day, score: dayAvg };
      });
      setTrend(trendData);
      const updateTime = new Date();
      setLastUpdated(updateTime);
      
      // Log update for debugging
      console.log(`[Dashboard] Data updated at ${updateTime.toLocaleTimeString()}`);

    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      
      // Log detailed error information
      if (error.response) {
        // The request was made and the server responded with a status code
        // that falls out of the range of 2xx
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
        console.error('Response headers:', error.response.headers);
        
        if (error.response.status === 401) {
          toast.error('Authentication failed. Please log in again.');
        } else if (error.response.status === 403) {
          toast.error('You do not have permission to access this data.');
        } else if (error.response.status === 404) {
          toast.error('Dashboard endpoint not found. Please check the API configuration.');
        } else {
          toast.error(`Failed to load dashboard data: ${error.response.status} ${error.response.statusText}`);
        }
      } else if (error.request) {
        // The request was made but no response was received
        console.error('No response received:', error.request);
        toast.error('No response from server. Please check your connection.');
      } else {
        // Something happened in setting up the request that triggered an Error
        console.error('Error setting up request:', error.message);
        toast.error(`Failed to load dashboard data: ${error.message}`);
      }
    } finally {
      setLoading(false);
      setIsRefreshing(false);
    }
  }, [user]);

  // Auto-refresh functionality - Same for both ADMIN and INSTRUCTOR
  useEffect(() => {
    // Only fetch data if user is loaded and not a student
    // Both ADMIN and INSTRUCTOR get real-time updates
    if (!user || !user.role || user.role === 'STUDENT') {
      if (user && user.role === 'STUDENT') {
        setLoading(false);
      }
      return;
    }

    // Initial fetch for both ADMIN and INSTRUCTOR
    fetchDashboardData();

    // Set up real-time auto-refresh interval (every 5 seconds for real-time updates)
    // This works the same for both ADMIN and INSTRUCTOR roles
    const intervalId = setInterval(() => {
      // Only refresh if tab is visible
      if (!document.hidden) {
        fetchDashboardData(false); // Don't show loading spinner on auto-refresh
      }
    }, refreshInterval); // 5 seconds for real-time feel

    // Cleanup interval on unmount or user change
    return () => {
      clearInterval(intervalId);
    };
  }, [user, fetchDashboardData, refreshInterval]);

  // Listen for visibility changes to refresh when tab becomes visible
  // Works for both ADMIN and INSTRUCTOR
  useEffect(() => {
    const handleVisibilityChange = () => {
      // Refresh when tab becomes visible for both ADMIN and INSTRUCTOR
      if (!document.hidden && user && user.role && user.role !== 'STUDENT') {
        fetchDashboardData(false);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [user, fetchDashboardData]);

  // Show student dashboard for students
  if (user?.role === 'STUDENT') {
    return <StudentDashboard />;
  }

  return (
    <>
      <style>{pulseStyle}</style>
      <div style={{ marginBottom: '32px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
          <div>
            <h1 style={{ marginBottom: '8px' }}>Welcome, {user?.username || 'Guest'}!</h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <span style={{ fontSize: '14px', color: 'var(--muted)' }}>Role:</span>
              <span
                style={{
                  display: 'inline-block',
                  background: getRoleColor(user?.role),
                  color: 'white',
                  padding: '4px 12px',
                  borderRadius: '20px',
                  fontSize: '14px',
                  fontWeight: '600',
                }}
              >
                {user?.role || 'Unknown'}
              </span>
              {lastUpdated && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginLeft: '8px' }}>
                  <span 
                    style={{ 
                      fontSize: '12px', 
                      color: isRefreshing ? 'var(--accent)' : 'var(--muted)',
                      transition: 'color 0.3s ease'
                    }}
                  >
                    {isRefreshing ? 'Updating...' : `Updated: ${lastUpdated.toLocaleTimeString()}`}
                  </span>
                  {isRefreshing && (
                    <span 
                      style={{
                        display: 'inline-block',
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        background: 'var(--accent)',
                        animation: 'pulse 1.5s ease-in-out infinite'
                      }}
                    />
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {loading ? (
        <div style={{ padding: '20px', textAlign: 'center' }}>Loading dashboard data...</div>
      ) : (
        <>
          <div className="cards-grid">
            {stats.map((card) => (
              <div 
                className="card" 
                key={card.label}
                style={{
                  transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                  position: 'relative'
                }}
              >
                <p className="muted">{card.label}</p>
                <h2 style={{ 
                  transition: 'color 0.3s ease',
                  color: isRefreshing ? 'var(--accent)' : 'inherit'
                }}>
                  {card.value}
                </h2>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <span 
                    className="muted"
                    style={{ fontSize: '11px' }}
                  >
                    Live data
                  </span>
                  {isRefreshing && (
                    <span 
                      style={{
                        display: 'inline-block',
                        width: '6px',
                        height: '6px',
                        borderRadius: '50%',
                        background: 'var(--accent)',
                        animation: 'pulse 1.5s ease-in-out infinite'
                      }}
                    />
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Active Quizzes List for Instructors - Always show this section */}
          {user?.role === 'INSTRUCTOR' && (
            <div className="card" style={{ marginTop: '24px' }}>
              <h3 style={{ marginBottom: '16px', fontSize: '18px', fontWeight: '600' }}>
                Active Quizzes
              </h3>
              {activeQuizzesList.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {activeQuizzesList.map((quiz) => {
                    // Count attempts for this quiz
                    const quizAttempts = filteredAttempts.filter(a => 
                      a.quizId === quiz.id || a.quiz?.id === quiz.id
                    );
                    const completedAttempts = quizAttempts.filter(a => a.status === 'GRADED').length;
                    const uniqueStudentsForQuiz = new Set();
                    quizAttempts.forEach(attempt => {
                      if (attempt.user) {
                        if (typeof attempt.user === 'object' && attempt.user.id) {
                          uniqueStudentsForQuiz.add(attempt.user.id);
                        } else if (typeof attempt.user === 'string' || typeof attempt.user === 'number') {
                          uniqueStudentsForQuiz.add(attempt.user);
                        }
                      } else if (attempt.userId) {
                        uniqueStudentsForQuiz.add(attempt.userId);
                      }
                    });
                    const studentsForQuiz = uniqueStudentsForQuiz.size;
                    const gradedScores = quizAttempts
                      .filter(a => a.status === 'GRADED' && a.score != null)
                      .map(a => a.score);
                    const avgQuizScore = gradedScores.length > 0
                      ? Math.round(gradedScores.reduce((sum, score) => sum + score, 0) / gradedScores.length)
                      : 0;

                    return (
                      <div
                        key={quiz.id}
                        style={{
                          padding: '16px',
                          background: 'var(--card-bg, #f8f9fa)',
                          borderRadius: '8px',
                          border: '1px solid var(--border, #e0e0e0)',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          transition: 'transform 0.2s ease, box-shadow 0.2s ease'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.transform = 'translateY(-2px)';
                          e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.transform = 'translateY(0)';
                          e.currentTarget.style.boxShadow = 'none';
                        }}
                      >
                        <div style={{ flex: 1 }}>
                          <h4 style={{ margin: '0 0 8px 0', fontSize: '16px', fontWeight: '600' }}>
                            {quiz.title}
                          </h4>
                          <div style={{ display: 'flex', gap: '16px', fontSize: '14px', color: 'var(--muted)' }}>
                            <span>Students: {studentsForQuiz}</span>
                            <span>Attempts: {completedAttempts}</span>
                            {avgQuizScore > 0 && <span>Avg Score: {avgQuizScore}%</span>}
                          </div>
                        </div>
                        <div style={{
                          padding: '6px 12px',
                          background: '#6bcf7f',
                          color: 'white',
                          borderRadius: '20px',
                          fontSize: '12px',
                          fontWeight: '600'
                        }}>
                          Active
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '24px' }}>
                  <p style={{ color: 'var(--muted)', fontSize: '16px' }}>
                    No active quizzes at the moment. Create and publish a quiz to get started!
                  </p>
                </div>
              )}
            </div>
          )}

          {/* All Quizzes Summary for Instructors - Always show this section */}
          {user?.role === 'INSTRUCTOR' && (
            <div className="card" style={{ marginTop: '24px' }}>
              <h3 style={{ marginBottom: '16px', fontSize: '18px', fontWeight: '600' }}>
                All Quizzes Created (Including Expired/Closed)
              </h3>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px' }}>
                {['DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED'].map((status) => {
                  const count = allQuizzesList.filter(q => q.status === status).length;
                  return (
                    <div
                      key={status}
                      style={{
                        padding: '12px 16px',
                        background: status === 'PUBLISHED' ? '#e8f5e9' : '#f5f5f5',
                        borderRadius: '8px',
                        border: `1px solid ${status === 'PUBLISHED' ? '#4caf50' : '#e0e0e0'}`,
                        flex: '1 1 auto',
                        minWidth: '120px',
                        opacity: count === 0 ? 0.6 : 1
                      }}
                    >
                      <div style={{ fontSize: '24px', fontWeight: '600', color: status === 'PUBLISHED' ? '#2e7d32' : '#666' }}>
                        {count}
                      </div>
                      <div style={{ fontSize: '12px', color: 'var(--muted)', marginTop: '4px', textTransform: 'capitalize' }}>
                        {status.toLowerCase()}
                      </div>
                    </div>
                  );
                })}
              </div>
              {allQuizzesList.length === 0 && (
                <div style={{ textAlign: 'center', padding: '16px', marginTop: '12px' }}>
                  <p style={{ color: 'var(--muted)', fontSize: '14px' }}>
                    No quizzes created yet. Create your first quiz to get started!
                  </p>
                </div>
              )}
            </div>
          )}

          <div className="chart-card" style={{ marginTop: '24px' }}>
            <h3>Engagement Trend (Last 7 Days)</h3>
            <ResponsiveContainer width="100%" height={240}>
              <AreaChart data={trend}>
                <defs>
                  <linearGradient id="grad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#7c5dff" stopOpacity={0.8} />
                    <stop offset="95%" stopColor="#7c5dff" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="name" stroke="#9fb1d0" />
                <YAxis stroke="#9fb1d0" />
                <Tooltip />
                <Area type="monotone" dataKey="score" stroke="#7c5dff" fillOpacity={1} fill="url(#grad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </>
      )}
    </>
  );
};

export default Dashboard;

