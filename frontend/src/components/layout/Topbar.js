import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import GlobalSearch from '../GlobalSearch';

const Topbar = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <header className="topbar">
      <div className="topbar__search-wrapper">
        <GlobalSearch />
      </div>
      <div className="topbar__profile">
        {user ? (
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '14px', fontWeight: '600' }}>{user.username}</div>
            <div style={{ fontSize: '12px', color: 'var(--muted)' }}>{user.role}</div>
          </div>
        ) : (
          <button
            onClick={() => navigate('/login')}
            style={{
              background: 'var(--accent)',
              border: 'none',
              color: 'white',
              padding: '8px 16px',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: '600',
              fontSize: '14px',
            }}
          >
            Log in
          </button>
        )}
      </div>
    </header>
  );
};

export default Topbar;

