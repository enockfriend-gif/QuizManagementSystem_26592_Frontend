import React, { useState } from 'react';
import api from '../api';
import { toast } from 'react-toastify';

const Settings = () => {
  const [settings, setSettings] = useState({
    emailNotifications: true,
    autoSave: true,
    language: 'en'
  });
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);

  const handleSettingsSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.put('/users/settings', settings);
      toast.success('Settings updated successfully');
    } catch (error) {
      console.error('Error updating settings:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Failed to update settings';
      toast.error(`Error updating settings: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }
    if (passwordData.newPassword.length < 6) {
      toast.error('Password must be at least 6 characters long');
      return;
    }
    setLoading(true);
    try {
      await api.put('/users/change-password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      });
      toast.success('Password changed successfully');
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error) {
      console.error('Error changing password:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Failed to change password';
      if (error.response?.status === 401) {
        toast.error('Current password is incorrect');
      } else {
        toast.error(`Error changing password: ${errorMessage}`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="settings-page">
      <h1>Settings</h1>
      
      <div className="settings-section">
        <h2>General Settings</h2>
        <form onSubmit={handleSettingsSubmit}>
          <div className="form-group">
            <label>
              <input
                type="checkbox"
                checked={settings.emailNotifications}
                onChange={(e) => setSettings({...settings, emailNotifications: e.target.checked})}
              />
              Email Notifications
            </label>
          </div>
          <div className="form-group">
            <label>
              <input
                type="checkbox"
                checked={settings.autoSave}
                onChange={(e) => setSettings({...settings, autoSave: e.target.checked})}
              />
              Auto Save
            </label>
          </div>
          <button type="submit" disabled={loading}>Save Settings</button>
        </form>
      </div>

      <div className="settings-section">
        <h2>Change Password</h2>
        <form onSubmit={handlePasswordSubmit}>
          <div className="form-group">
            <label>Current Password:</label>
            <input
              type="password"
              value={passwordData.currentPassword}
              onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})}
              required
            />
          </div>
          <div className="form-group">
            <label>New Password:</label>
            <input
              type="password"
              value={passwordData.newPassword}
              onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})}
              required
            />
          </div>
          <div className="form-group">
            <label>Confirm New Password:</label>
            <input
              type="password"
              value={passwordData.confirmPassword}
              onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
              required
            />
          </div>
          <button type="submit" disabled={loading}>Change Password</button>
        </form>
      </div>
    </div>
  );
};

export default Settings;