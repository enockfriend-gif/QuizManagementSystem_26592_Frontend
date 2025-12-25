import React, { useState } from 'react';
import api from '../api';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

import Button from '../components/ui/Button';
import '../styles/login.css';
import { toast } from 'react-toastify';

const Login = () => {
  const [form, setForm] = useState({ usernameOrEmail: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  // Test if React is working
  console.log('Login component loaded');

  const submit = (e) => {
    e.preventDefault();
    e.stopPropagation();

    console.log('=== FORM SUBMITTED ===');
    console.log('Event prevented:', e.defaultPrevented);

    handleLogin();
  };

  const decodeToken = (token) => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1] || ''));
      return payload;
    } catch (err) {
      console.error('Failed to decode token', err);
      return null;
    }
  };

  const handleLogin = () => {
    console.log('=== LOGIN ATTEMPT ===');
    setError('');
    setLoading(true);

    api.post('/auth/login', form)
      .then((res) => {
        console.log('Response data:', res.data);
        const data = res.data || {};
        
        // Two-factor authentication is now enforced
        // Backend returns email address after successful credential verification
        // User must verify OTP to complete login
        const email = data?.email || form.usernameOrEmail;
        
        if (!email) {
          throw new Error('Email address not received from server');
        }
        
        // Navigate to OTP verification page
        navigate('/verify-otp', { state: { email } });
        toast.success(`OTP sent to ${email}. Please check your email.`);
      })
      .catch((err) => {
        console.error('Login error:', err);
        const errorMessage = err.response?.data?.message || err.response?.data || 'Login failed. Please check your credentials.';
        setError(errorMessage);
        toast.error(errorMessage);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Sign in</h2>
        <p className="muted">Welcome back! Enter your credentials</p>
        <p style={{ fontSize: '12px', color: 'var(--muted)', marginTop: '8px', marginBottom: '16px', textAlign: 'center' }}>
          🔐 Two-factor authentication enabled. You'll receive an OTP code via email.
        </p>

        <form onSubmit={submit} noValidate>
          <div className="form-group">
            <label>Email or username</label>
            <input
              value={form.usernameOrEmail}
              onChange={(e) => setForm({ ...form, usernameOrEmail: e.target.value })}
              placeholder="admin, teacher, or student"
              required
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <div className="password-input-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                placeholder="Enter your password"
                required
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword(!showPassword)}
                title={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                ) : (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                )}
              </button>
            </div>
          </div>

          {error && <div className="error">{error}</div>}
          <Button type="button" fullWidth disabled={loading} onClick={handleLogin}>
            {loading ? 'Signing in...' : 'Sign in'}
          </Button>
        </form>

        <button type="button" className="link" onClick={() => navigate('/reset-password')}>
          Forgot password?
        </button>
      </div>
    </div>
  );
};

export default Login;

