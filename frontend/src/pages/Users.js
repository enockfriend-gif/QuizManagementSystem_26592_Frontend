import React, { useState, useEffect, useMemo, useCallback } from 'react';
import DataTable from '../components/ui/DataTable';
import UserFormModal from '../components/UserFormModal';
import Button from '../components/ui/Button';
import api from '../api';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const Users = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [total, setTotal] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [viewingUser, setViewingUser] = useState(null);
  const [viewModalOpen, setViewModalOpen] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(null);
  const pageSize = 8;

  const fetchUsers = useCallback(async (showLoading = true) => {
    try {
      if (showLoading) {
        setLoading(true);
      }
      console.log('[Users] Fetching users - page:', page, 'size:', pageSize, 'search:', search);
      // Pagination Api
      const response = await api.get('/users/page', {
        params: {
          page: page,
          size: pageSize,
          q: search,
        },
      });
      
      console.log('[Users] API Response:', response.data);
      
      // Handle Spring Data Page response structure
      let users = [];
      let totalCount = 0;
      
      if (response.data) {
        // Check if it's a Page object (has content and totalElements)
        if (response.data.content !== undefined) {
          users = Array.isArray(response.data.content) ? response.data.content : [];
          totalCount = response.data.totalElements || 0;
          console.log('[Users] Page response - users:', users.length, 'total:', totalCount);
        } 
        // Check if it's a direct array
        else if (Array.isArray(response.data)) {
          users = response.data;
          totalCount = response.data.length;
          console.log('[Users] Array response - users:', users.length);
        }
        // Check if it's wrapped in another structure
        else if (response.data.data && Array.isArray(response.data.data)) {
          users = response.data.data;
          totalCount = response.data.total || response.data.totalElements || users.length;
          console.log('[Users] Wrapped response - users:', users.length);
        }
      }
      
      console.log('[Users] Final users array:', users);
      console.log('[Users] Setting data - count:', users.length, 'total:', totalCount);
      
      setData(users);
      setTotal(totalCount);
      setLastUpdated(new Date());
      
      if (users.length === 0 && totalCount === 0) {
        console.warn('[Users] No users found in response');
      }
    } catch (error) {
      console.error('[Users] Error fetching users:', error);
      console.error('[Users] Error response:', error.response?.data);
      console.error('[Users] Error status:', error.response?.status);
      
      // Try fallback to non-paginated endpoint
      try {
        console.log('[Users] Trying fallback to /users endpoint...');
        const fallbackResponse = await api.get('/users');
        const fallbackUsers = Array.isArray(fallbackResponse.data) ? fallbackResponse.data : [];
        console.log('[Users] Fallback response - users:', fallbackUsers.length);
        
        // Apply search filter if needed
        let filteredUsers = fallbackUsers;
        if (search && search.trim()) {
          filteredUsers = fallbackUsers.filter(user => 
            user.username?.toLowerCase().includes(search.toLowerCase()) ||
            user.email?.toLowerCase().includes(search.toLowerCase())
          );
        }
        
        // Apply pagination manually
        const startIndex = page * pageSize;
        const endIndex = startIndex + pageSize;
        const paginatedUsers = filteredUsers.slice(startIndex, endIndex);
        
        setData(paginatedUsers);
        setTotal(filteredUsers.length);
        
        if (paginatedUsers.length > 0) {
          toast.success(`Loaded ${paginatedUsers.length} users (using fallback)`);
        }
      } catch (fallbackError) {
        console.error('[Users] Fallback also failed:', fallbackError);
        toast.error(`Failed to load users: ${error.response?.data?.message || error.message || 'Unknown error'}`);
        setData([]);
        setTotal(0);
      }
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  }, [page, search, pageSize]);

  // Check if user is admin and fetch users
  useEffect(() => {
    if (user) {
      if (user.role !== 'ADMIN') {
        toast.error('Unauthorized: Only admins can access this page');
        navigate('/');
        return;
      }
      // User is admin, fetch users
      fetchUsers();
      
      // Set up real-time auto-refresh interval (every 5 seconds)
      const refreshInterval = setInterval(() => {
        // Only refresh if tab is visible
        if (!document.hidden) {
          fetchUsers(false); // Don't show loading spinner on auto-refresh
        }
      }, 5000); // 5 seconds for real-time updates
      
      // Cleanup interval on unmount
      return () => {
        clearInterval(refreshInterval);
      };
    }
  }, [user, navigate, fetchUsers]);

  // Listen for visibility changes to refresh when tab becomes visible
  useEffect(() => {
    if (user && user.role === 'ADMIN') {
      const handleVisibilityChange = () => {
        if (!document.hidden) {
          fetchUsers(false); // Don't show loading spinner on visibility refresh
        }
      };
      
      document.addEventListener('visibilitychange', handleVisibilityChange);
      return () => {
        document.removeEventListener('visibilitychange', handleVisibilityChange);
      };
    }
  }, [user, fetchUsers]);

  const handleDeleteUser = useCallback(async (userId) => {
    if (window.confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      try {
        await api.delete(`/users/${userId}`);
        toast.success('User deleted successfully');
        // Immediately refresh after delete
        setTimeout(() => {
          fetchUsers();
        }, 300);
      } catch (error) {
        const msg = error.response?.data?.message || 'Failed to delete user';
        toast.error(msg);
      }
    }
  }, [fetchUsers]);

  const handleCloseViewModal = useCallback(() => {
    setViewModalOpen(false);
    setViewingUser(null);
  }, []);

  const handleOpenModal = useCallback((userToEdit = null) => {
    setEditingUser(userToEdit);
    setModalOpen(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setModalOpen(false);
    setEditingUser(null);
  }, []);

  const handleSuccess = useCallback(() => {
    // Immediately refresh after create/update - with a small delay to ensure backend has processed
    setTimeout(() => {
      // Reset to first page to show the newly created user
      setPage(0);
      setSearch(''); // Clear search to show all users
      // Then fetch users
      fetchUsers();
    }, 300);
  }, [fetchUsers]);


  // Helper function to get role color
  const getRoleColor = (role) => {
    switch (role) {
      case 'ADMIN':
        return { bg: '#ff6b6b', color: '#fff' };
      case 'INSTRUCTOR':
        return { bg: '#ffd93d', color: '#000' };
      case 'STUDENT':
        return { bg: '#6bcf7f', color: '#fff' };
      default:
        return { bg: '#7c5dff', color: '#fff' };
    }
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const columns = useMemo(
    () => [
      { 
        header: 'ID', 
        accessor: 'id',
        key: 'id'
      },
      { 
        header: 'Username', 
        accessor: 'username',
        key: 'username'
      },
      { 
        header: 'Email', 
        accessor: 'email',
        key: 'email'
      },
      { 
        header: 'Role', 
        key: 'role',
        accessor: (row) => {
          const role = row.role || 'UNKNOWN';
          const colors = getRoleColor(role);
          return (
            <span
              style={{
                display: 'inline-block',
                background: colors.bg,
                color: colors.color,
                padding: '4px 12px',
                borderRadius: '20px',
                fontSize: '12px',
                fontWeight: '600',
                textTransform: 'uppercase',
              }}
            >
              {role}
            </span>
          );
        },
      },
      {
        header: 'Actions',
        key: 'actions',
        // eslint-disable-next-line react/display-name
        accessor: (row) => (
          <div style={{ 
            display: 'flex', 
            gap: '8px', 
            alignItems: 'center', 
            flexWrap: 'wrap',
            justifyContent: 'center',
            minWidth: '150px'
          }}>
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleOpenModal(row);
              }}
              style={{
                padding: '6px 14px',
                fontSize: '0.85rem',
                background: '#6366f1',
                color: 'white',
                border: 'none',
                borderRadius: '6px',
                cursor: 'pointer',
                transition: 'all 0.2s',
                fontWeight: '600',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                minWidth: '70px',
              }}
              onMouseEnter={(e) => {
                e.target.style.background = '#4f46e5';
                e.target.style.transform = 'translateY(-2px)';
                e.target.style.boxShadow = '0 4px 8px rgba(0,0,0,0.2)';
              }}
              onMouseLeave={(e) => {
                e.target.style.background = '#6366f1';
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = '0 2px 4px rgba(0,0,0,0.1)';
              }}
              title="Edit user"
            >
              ✏️ Edit
            </button>
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleDeleteUser(row.id);
              }}
              style={{
                padding: '6px 14px',
                fontSize: '0.85rem',
                background: '#ef4444',
                color: 'white',
                border: 'none',
                borderRadius: '6px',
                cursor: 'pointer',
                transition: 'all 0.2s',
                fontWeight: '600',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                minWidth: '70px',
              }}
              onMouseEnter={(e) => {
                e.target.style.background = '#dc2626';
                e.target.style.transform = 'translateY(-2px)';
                e.target.style.boxShadow = '0 4px 8px rgba(0,0,0,0.2)';
              }}
              onMouseLeave={(e) => {
                e.target.style.background = '#ef4444';
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = '0 2px 4px rgba(0,0,0,0.1)';
              }}
              title="Delete user"
            >
              🗑️ Delete
            </button>
          </div>
        ),
      },
    ],
    [handleDeleteUser, handleOpenModal],
  );

  if (loading && data.length === 0) {
    return (
      <div>
        <h2>Users</h2>
        <div style={{ padding: '20px', textAlign: 'center' }}>Loading...</div>
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ margin: 0, marginBottom: '8px' }}>User Management</h2>
          <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '14px' }}>
            Manage all users in the system. View, edit, and delete user accounts.
            {lastUpdated && (
              <span style={{ marginLeft: '12px', fontSize: '12px' }}>
                Last updated: {lastUpdated.toLocaleTimeString()}
              </span>
            )}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <Button onClick={() => handleOpenModal()}>+ Create User</Button>
        </div>
      </div>

      <UserFormModal
        isOpen={modalOpen}
        user={editingUser}
        onClose={handleCloseModal}
        onSuccess={handleSuccess}
      />

      {/* View User Details Modal */}
      {viewModalOpen && viewingUser && (
        <div className="modal-overlay" onClick={handleCloseViewModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <div className="modal-header">
              <h3>User Details</h3>
              <button className="modal-close" onClick={handleCloseViewModal}>×</button>
            </div>
            <div style={{ padding: '20px' }}>
              <div style={{ marginBottom: '16px' }}>
                <strong style={{ display: 'block', marginBottom: '4px', color: 'var(--text-secondary)' }}>ID:</strong>
                <span>{viewingUser.id}</span>
              </div>
              <div style={{ marginBottom: '16px' }}>
                <strong style={{ display: 'block', marginBottom: '4px', color: 'var(--text-secondary)' }}>Username:</strong>
                <span>{viewingUser.username || 'N/A'}</span>
              </div>
              <div style={{ marginBottom: '16px' }}>
                <strong style={{ display: 'block', marginBottom: '4px', color: 'var(--text-secondary)' }}>Email:</strong>
                <span>{viewingUser.email || 'N/A'}</span>
              </div>
              <div style={{ marginBottom: '16px' }}>
                <strong style={{ display: 'block', marginBottom: '4px', color: 'var(--text-secondary)' }}>Role:</strong>
                <span
                  style={{
                    display: 'inline-block',
                    background: getRoleColor(viewingUser.role).bg,
                    color: getRoleColor(viewingUser.role).color,
                    padding: '4px 12px',
                    borderRadius: '20px',
                    fontSize: '12px',
                    fontWeight: '600',
                    textTransform: 'uppercase',
                  }}
                >
                  {viewingUser.role || 'UNKNOWN'}
                </span>
              </div>
              {viewingUser.location && (
                <div style={{ marginBottom: '16px' }}>
                  <strong style={{ display: 'block', marginBottom: '4px', color: 'var(--text-secondary)' }}>Location:</strong>
                  <span>
                    {viewingUser.location.provinceName || ''} 
                    {viewingUser.location.districtName ? `, ${viewingUser.location.districtName}` : ''}
                    {viewingUser.location.sectorName ? `, ${viewingUser.location.sectorName}` : ''}
                  </span>
                </div>
              )}
              <div style={{ marginTop: '24px', display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                <Button onClick={handleCloseViewModal} style={{ background: '#888' }}>
                  Close
                </Button>
                <Button onClick={() => {
                  handleCloseViewModal();
                  handleOpenModal(viewingUser);
                }}>
                  Edit User
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}

      <DataTable
        columns={columns}
        data={data}
        page={page}
        pageSize={pageSize}
        total={total}
        loading={loading}
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

export default Users;

