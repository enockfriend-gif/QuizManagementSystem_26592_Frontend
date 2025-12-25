import React, { useState } from 'react';
import api from '../api';
import { useLocation, useNavigate } from 'react-router-dom';
import Button from '../components/ui/Button';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';

const VerifyOtp = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const email = location.state?.email || '';
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  // Check if we have required data
  if (!email) {
    return (
      <div className="auth-page">
        <div className="auth-card" style={{ padding: '40px', textAlign: 'center' }}>
          <h2>Error</h2>
          <p style={{ color: '#666', marginBottom: '20px' }}>
            Email address not found. Please log in again.
          </p>
          <Button onClick={() => navigate('/login')} fullWidth>
            Return to Login
          </Button>
        </div>
      </div>
    );
  }

  const decodeToken = (token) => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1] || ''));
      return payload;
    } catch (err) {
      console.error('Failed to decode token', err);
      return null;
    }
  };

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    
    // Validate OTP format (6 digits)
    const otpCode = code.trim();
    if (!/^\d{6}$/.test(otpCode)) {
      setError('Please enter a valid 6-digit OTP code');
      toast.error('OTP must be exactly 6 digits');
      return;
    }
    
    setLoading(true);
    try {
      const res = await api.post('/auth/verify-otp', { email, code: otpCode });
      const token = res.data.token;
      
      if (!token) {
        throw new Error('Token not received from server');
      }

      // Decode token to get user details
      const payload = decodeToken(token);
      if (!payload) {
        throw new Error('Failed to decode authentication token');
      }

      // Prepare user data from JWT payload
      const userData = {
        username: payload.sub,
        email,
        role: payload.role,
      };

      // Login with token and user data
      login(token, userData);

      toast.success('Signed in successfully! Two-factor authentication completed.');
      
      // Redirect user based on role
      const role = payload.role;
      const getDefaultRedirect = (r) => {
        switch (r) {
          case 'ADMIN': return '/';
          case 'INSTRUCTOR': return '/quizzes';
          case 'STUDENT': return '/quizzes';
          default: return '/';
        }
      };
      navigate(getDefaultRedirect(role || ''));
    } catch (err) {
      const errMsg = err.response?.data?.message || err.response?.data || 'OTP invalid or expired.';
      setError(errMsg);
      toast.error(errMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Enter OTP</h2>
        <p className="muted">Code sent to {email || 'your email'}</p>
        <form onSubmit={submit}>
          <div className="form-group">
            <label>6-digit verification code</label>
            <input
              type="text"
              value={code}
              onChange={(e) => {
                // Only allow digits, max 6 characters
                const value = e.target.value.replace(/\D/g, '').slice(0, 6);
                setCode(value);
              }}
              placeholder="000000"
              maxLength={6}
              required
              style={{
                textAlign: 'center',
                fontSize: '24px',
                letterSpacing: '8px',
                fontFamily: 'monospace'
              }}
            />
            <small style={{ color: '#666', display: 'block', marginTop: '8px' }}>
              Enter the 6-digit code sent to your email. Code expires in 5 minutes.
            </small>
          </div>
          {error && <div className="error">{error}</div>}
          <Button type="submit" fullWidth disabled={loading || code.length !== 6}>
            {loading ? 'Verifying...' : 'Verify & Sign in'}
          </Button>
          <button 
            type="button" 
            className="link" 
            onClick={() => navigate('/login')}
            style={{ marginTop: '12px', fontSize: '14px', display: 'block', width: '100%', textAlign: 'center' }}
          >
            Back to Login
          </button>
        </form>
      </div>
    </div>
  );
};

export default VerifyOtp;

