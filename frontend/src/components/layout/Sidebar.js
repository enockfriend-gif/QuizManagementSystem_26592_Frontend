import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import Button from '../ui/Button';
import { useAuth } from '../../context/AuthContext';
import { navigationConfig, ROLES } from '../../config/navigationConfig';
import { toast } from 'react-toastify';

const Sidebar = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const links = navigationConfig.filter(link =>
    link.inSidebar && (!link.roles || link.roles.includes(user?.role))
  );

  const getLabel = (link) => {
    // Customize label for specific roles if needed
    if (link.path === '/attempts' && user?.role === ROLES.STUDENT) return 'My Results';
    return link.label;
  };

  const handleLogout = () => {
    logout();
    toast.info('Signed out');
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <button
        className="sidebar__brand"
        onClick={() => navigate('/')}
        aria-label="Go to home"
        style={{ background: 'none', border: 'none', padding: 0, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
      >
        <img 
          src={`${process.env.PUBLIC_URL}/logo.svg`} 
          alt="Quiz Manager" 
          style={{ height: '40px', width: 'auto', maxWidth: '100%' }}
        />
      </button>
      <nav className="sidebar__nav">
        {links.map((link) => (
          <NavLink key={link.path} to={link.path} className="sidebar__link">
            {getLabel(link)}
          </NavLink>
        ))}
      </nav>
      <div className="sidebar__cta" style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {user?.role === 'INSTRUCTOR' && (
          <Button fullWidth onClick={() => navigate('/quizzes')}>New Quiz</Button>
        )}
        {user && (
          <Button 
            fullWidth 
            onClick={handleLogout}
            style={{
              background: 'rgba(255, 107, 107, 0.2)',
              border: '1px solid #ff6b6b',
              color: '#ff6b6b',
            }}
            onMouseEnter={(e) => {
              e.target.style.background = 'rgba(255, 107, 107, 0.3)';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = 'rgba(255, 107, 107, 0.2)';
            }}
          >
            Log Out
          </Button>
        )}
      </div>
    </aside>
  );
};

export default Sidebar;

