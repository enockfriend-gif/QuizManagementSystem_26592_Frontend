import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import Button from '../components/ui/Button';
import { toast } from 'react-toastify';

const ResetPassword = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const requestOtp = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    
    // Basic email validation
    if (!email || !email.includes('@')) {
      setError('Please enter a valid email address.');
      toast.error('Please enter a valid email address.');
      return;
    }
    
    setLoading(true);
    try {
      const response = await api.post('/auth/reset/request', { email });
      // Handle 202 Accepted response
      if (response.status === 202 || !response.data) {
        setStep(2);
        setMessage('OTP sent to your email. Please check your inbox (and spam folder).');
        toast.success('OTP sent to your email. Please check your inbox.');
      } else {
        setStep(2);
        setMessage('OTP sent to your email. Please check your inbox (and spam folder).');
        toast.success('OTP sent to your email. Please check your inbox.');
      }
    } catch (err) {
      console.error('Password reset request error:', err);
      const errorMessage = err.response?.data?.message || 
                          (err.response?.data?.error ? err.response.data.error : null) ||
                          (typeof err.response?.data === 'string' ? err.response.data : null) ||
                          err.message || 
                          'Failed to send reset email.';
      
      if (err.response?.status === 404 || errorMessage.toLowerCase().includes('not found')) {
        setError('Email not found in our system.');
        toast.error('Email not found in our system.');
      } else if (err.response?.status === 400 || errorMessage.toLowerCase().includes('invalid')) {
        setError('Invalid email format. Please check your email address.');
        toast.error('Invalid email format.');
      } else {
        setError(`Failed to send reset email: ${errorMessage}`);
        toast.error(`Failed to send reset email: ${errorMessage}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const confirmReset = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    
    // Validation
    if (!code || code.length !== 6) {
      setError('Please enter a valid 6-digit OTP code.');
      toast.error('Please enter a valid 6-digit OTP code.');
      return;
    }
    
    if (!password || password.length < 6) {
      setError('Password must be at least 6 characters long.');
      toast.error('Password must be at least 6 characters long.');
      return;
    }
    
    setLoading(true);
    try {
      const response = await api.post('/auth/reset/confirm', { email, code, newPassword: password });
      // Handle successful password reset (200 OK or 204 No Content)
      if (response.status === 200 || response.status === 204 || response.data?.status === 'success') {
        setMessage('Password updated successfully! You can now sign in with your new password.');
        toast.success('Password updated successfully! You can now sign in with your new password.');
        // Reset form and redirect to login after successful password change
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else {
        setMessage('Password updated successfully! You can now sign in with your new password.');
        toast.success('Password updated successfully!');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      }
    } catch (err) {
      console.error('Password reset confirmation error:', err);
      const errorMessage = err.response?.data?.message || 
                          (err.response?.data?.error ? err.response.data.error : null) ||
                          (typeof err.response?.data === 'string' ? err.response.data : null) ||
                          err.message || 
                          'Failed to reset password.';
      
      if (err.response?.status === 401 || 
          errorMessage.toLowerCase().includes('invalid') || 
          errorMessage.toLowerCase().includes('expired') ||
          errorMessage.toLowerCase().includes('otp')) {
        setError('Invalid or expired OTP code. Please request a new code.');
        toast.error('Invalid or expired OTP code. Please request a new code.');
      } else {
        setError(`Failed to reset password: ${errorMessage}`);
        toast.error(`Failed to reset password: ${errorMessage}`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Password reset</h2>
        {step === 1 && (
          <form onSubmit={requestOtp}>
            <div className="form-group">
              <label>Email Address</label>
              <input 
                type="email"
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
                placeholder="Enter your email address"
                required 
              />
            </div>
            <Button type="submit" fullWidth disabled={loading}>
              {loading ? 'Sending...' : 'Send Reset Code'}
            </Button>
            <p style={{ fontSize: '12px', color: 'var(--muted)', marginTop: '12px', textAlign: 'center' }}>
              We'll send a 6-digit code to your email address.
            </p>
            <p style={{ fontSize: '12px', color: 'var(--muted)', marginTop: '8px', textAlign: 'center' }}>
              Remember your password?{' '}
              <button
                type="button"
                onClick={() => navigate('/login')}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--accent)',
                  cursor: 'pointer',
                  textDecoration: 'underline',
                  fontSize: '12px'
                }}
              >
                Sign in
              </button>
            </p>
          </form>
        )}
        {step === 2 && (
          <form onSubmit={confirmReset}>
            <div className="form-group">
              <label>OTP Code</label>
              <input 
                type="text"
                value={code} 
                onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))} 
                placeholder="Enter 6-digit code"
                maxLength={6}
                required 
              />
              <p style={{ fontSize: '11px', color: 'var(--muted)', marginTop: '4px' }}>
                Check your email for the code sent to {email}
              </p>
            </div>
            <div className="form-group">
              <label>New Password</label>
              <input 
                type="password"
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                placeholder="Enter new password (min. 6 characters)"
                minLength={6}
                required 
              />
            </div>
            <Button type="submit" fullWidth disabled={loading || code.length !== 6 || password.length < 6}>
              {loading ? 'Updating Password...' : 'Update Password'}
            </Button>
            <button 
              type="button" 
              className="link" 
              onClick={() => {
                setStep(1);
                setCode('');
                setPassword('');
                setError('');
                setMessage('');
              }}
              style={{ marginTop: '12px', fontSize: '14px', display: 'block', width: '100%', textAlign: 'center' }}
            >
              Back to email entry
            </button>
          </form>
        )}
        {message && (
          <div className="success" style={{ marginTop: '16px' }}>
            {message}
            {step === 2 && message.includes('successfully') && (
              <div style={{ marginTop: '12px' }}>
                <Button 
                  onClick={() => navigate('/login')} 
                  fullWidth
                  style={{ marginTop: '8px' }}
                >
                  Go to Login
                </Button>
              </div>
            )}
          </div>
        )}
        {error && <div className="error" style={{ marginTop: '16px' }}>{error}</div>}
      </div>
    </div>
  );
};

export default ResetPassword;

