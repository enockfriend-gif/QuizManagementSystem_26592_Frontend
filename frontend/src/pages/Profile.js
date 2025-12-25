import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api';
import { toast } from 'react-toastify';
import Button from '../components/ui/Button';

const Profile = () => {
  const { user, login } = useAuth();
  const [profile, setProfile] = useState({
    username: '',
    email: '',
    role: '',
    location: {
      provinceId: '',
      provinceName: '',
      districtId: '',
      districtName: '',
      sectorId: '',
      sectorName: '',
      cellId: '',
      cellName: '',
      villageId: '',
      villageName: ''
    }
  });
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [provinces, setProvinces] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [sectors, setSectors] = useState([]);
  const [cells, setCells] = useState([]);
  const [villages, setVillages] = useState([]);


  const isAdmin = user?.role === 'ADMIN';

  useEffect(() => {
    if (user && isAdmin) {
      setProfile({
        username: user.username || '',
        email: user.email || '',
        role: user.role || '',
        location: user.location ? {
          provinceId: user.location.provinceId || '',
          provinceName: user.location.provinceName || '',
          districtId: user.location.districtId || '',
          districtName: user.location.districtName || '',
          sectorId: user.location.sectorId || '',
          sectorName: user.location.sectorName || '',
          cellId: user.location.cellId || '',
          cellName: user.location.cellName || '',
          villageId: user.location.villageId || '',
          villageName: user.location.villageName || ''
        } : {
          provinceId: '',
          provinceName: '',
          districtId: '',
          districtName: '',
          sectorId: '',
          sectorName: '',
          cellId: '',
          cellName: '',
          villageId: '',
          villageName: ''
        }
      });

      // Load location cascading dropdowns if location exists
      if (user.location && user.location.provinceId) {
        fetchProvinces().then(() => {
          fetchDistricts(user.location.provinceId).then(() => {
            if (user.location.districtId) {
              fetchSectors(user.location.provinceId, user.location.districtId).then(() => {
                if (user.location.sectorId) {
                  // Fetch cells from API
                  fetchCells(user.location.provinceId, user.location.districtId, user.location.sectorId).then(() => {
                    if (user.location.cellId) {
                      // Fetch villages from API
                      fetchVillages(user.location.provinceId, user.location.districtId, user.location.sectorId, user.location.cellId);
                    }
                  });
                }
              });
            }
          });
        });
      } else {
        fetchProvinces();
      }
    }
  }, [user, isAdmin]);

  const fetchProvinces = async () => {
    try {
      const response = await api.get('/locations/provinces');
      const provincesData = response.data || [];
      setProvinces(provincesData);
    } catch (error) {
      console.error('Error fetching provinces:', error);
    }
  };

  const fetchDistricts = async (provinceId) => {
    if (!provinceId) {
      setDistricts([]);
      return;
    }
    try {
      const response = await api.get('/locations/districts', { params: { provinceId } });
      setDistricts(response.data || []);
    } catch (error) {
      console.error('Error fetching districts:', error);
    }
  };

  const fetchSectors = async (provinceId, districtId) => {
    if (!provinceId || !districtId) {
      setSectors([]);
      return;
    }
    try {
      const response = await api.get('/locations/sectors', { 
        params: { provinceId, districtId } 
      });
      setSectors(response.data || []);
    } catch (error) {
      console.error('Error fetching sectors:', error);
    }
  };

  const fetchCells = async (provinceId, districtId, sectorId) => {
    if (!provinceId || !districtId || !sectorId) {
      setCells([]);
      return;
    }
    try {
      const response = await api.get('/locations/cells', { 
        params: { provinceId, districtId, sectorId } 
      });
      setCells(response.data || []);
    } catch (error) {
      console.error('Error fetching cells:', error);
      toast.error('Failed to load cells');
      setCells([]);
    }
  };

  const fetchVillages = async (provinceId, districtId, sectorId, cellId) => {
    if (!provinceId || !districtId || !sectorId || !cellId) {
      setVillages([]);
      return;
    }
    try {
      const response = await api.get('/locations/villages', { 
        params: { provinceId, districtId, sectorId, cellId } 
      });
      setVillages(response.data || []);
    } catch (error) {
      console.error('Error fetching villages:', error);
      toast.error('Failed to load villages');
      setVillages([]);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const handleLocationChange = (field, value, displayName = null) => {
    setProfile((prev) => {
      const newLocation = { ...prev.location };
      
      if (field === 'provinceId') {
        newLocation.provinceId = value;
        newLocation.provinceName = displayName || provinces.find(p => p.id === value)?.name || '';
        newLocation.districtId = '';
        newLocation.districtName = '';
        newLocation.sectorId = '';
        newLocation.sectorName = '';
        newLocation.cellId = '';
        newLocation.cellName = '';
        newLocation.villageId = '';
        newLocation.villageName = '';
        setDistricts([]);
        setSectors([]);
        setCells([]);
        setVillages([]);
        if (value) {
          fetchDistricts(value);
        }
      } else if (field === 'districtId') {
        newLocation.districtId = value;
        newLocation.districtName = displayName || districts.find(d => d.id === value)?.name || '';
        newLocation.sectorId = '';
        newLocation.sectorName = '';
        newLocation.cellId = '';
        newLocation.cellName = '';
        newLocation.villageId = '';
        newLocation.villageName = '';
        setSectors([]);
        setCells([]);
        setVillages([]);
        if (value) {
          fetchSectors(profile.location.provinceId, value);
        }
      } else if (field === 'sectorId') {
        newLocation.sectorId = value;
        newLocation.sectorName = displayName || sectors.find(s => s.id === value)?.name || '';
        newLocation.cellId = '';
        newLocation.cellName = '';
        newLocation.villageId = '';
        newLocation.villageName = '';
        setCells([]);
        setVillages([]);
        if (value) {
          fetchCells(profile.location.provinceId, profile.location.districtId, value);
        }
      } else if (field === 'cellId') {
        newLocation.cellId = value;
        newLocation.cellName = displayName || cells.find(c => c.id.toString() === value)?.name || '';
        newLocation.villageId = '';
        newLocation.villageName = '';
        setVillages([]);
        if (value) {
          fetchVillages(profile.location.provinceId, profile.location.districtId, profile.location.sectorId, value);
        }
      } else if (field === 'villageId') {
        newLocation.villageId = value;
        newLocation.villageName = displayName || villages.find(v => v.id.toString() === value)?.name || '';
      }
      
      return { ...prev, location: newLocation };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isAdmin) {
      toast.error('Only administrators can update profile information');
      return;
    }
    setLoading(true);
    try {
      const response = await api.put('/users/profile', profile);
      // Update auth context with new user data
      login(null, response.data);
      toast.success('Profile updated successfully');
    } catch (error) {
      console.error('Error updating profile:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || 'Error updating profile';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };


  const handlePasswordChange = async (e) => {
    e.preventDefault();
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error('New passwords do not match');
      return;
    }

    if (passwordData.newPassword.length < 6) {
      toast.error('Password must be at least 6 characters long');
      return;
    }

    setPasswordLoading(true);
    try {
      await api.put('/users/change-password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      });
      toast.success('Password changed successfully');
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      });
    } catch (error) {
      console.error('Error changing password:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || 'Error changing password';
      toast.error(errorMessage);
    } finally {
      setPasswordLoading(false);
    }
  };

  return (
    <div className="profile-page" style={{ padding: '32px', maxWidth: '1200px', margin: '0 auto' }}>
      <h1 className="page-title" style={{ marginBottom: '32px' }}>My Profile</h1>
      
      <div style={{ display: 'grid', gridTemplateColumns: isAdmin ? '1fr 1fr' : '1fr', gap: '32px' }}>
        {/* Profile Information Form - Only for Admin */}
        {isAdmin && (
          <div className="card">
          <h2 style={{ marginBottom: '24px', fontSize: '20px', fontWeight: '600' }}>Profile Information</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group" style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                Username:
              </label>
              <input
                type="text"
                name="username"
                value={profile.username}
                onChange={handleChange}
                required
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  borderRadius: '8px',
                  border: '1px solid var(--border)',
                  background: 'rgba(255, 255, 255, 0.04)',
                  color: 'var(--text)',
                  fontSize: '14px'
                }}
              />
            </div>

            <div className="form-group" style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                Email:
              </label>
              <input
                type="email"
                name="email"
                value={profile.email}
                onChange={handleChange}
                required
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  borderRadius: '8px',
                  border: '1px solid var(--border)',
                  background: 'rgba(255, 255, 255, 0.04)',
                  color: 'var(--text)',
                  fontSize: '14px'
                }}
              />
            </div>

            <div className="form-group" style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                Role:
              </label>
              <input
                type="text"
                value={profile.role}
                disabled
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  borderRadius: '8px',
                  border: '1px solid var(--border)',
                  background: 'rgba(255, 255, 255, 0.02)',
                  color: 'var(--muted)',
                  fontSize: '14px',
                  cursor: 'not-allowed'
                }}
              />
            </div>

            {/* Location Fields */}
            <div style={{ marginTop: '24px', marginBottom: '20px' }}>
              <h3 style={{ marginBottom: '16px', fontSize: '16px', fontWeight: '600' }}>Location</h3>
              
              <div className="form-group" style={{ marginBottom: '16px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                  Province:
                </label>
                <select
                  value={profile.location.provinceId || ''}
                  onChange={(e) => handleLocationChange('provinceId', e.target.value, e.target.options[e.target.selectedIndex].text)}
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: '8px',
                    border: '1px solid var(--border)',
                    background: 'rgba(255, 255, 255, 0.04)',
                    color: 'var(--text)',
                    fontSize: '14px'
                  }}
                >
                  <option value="">Select Province</option>
                  {provinces.map(province => (
                    <option key={province.id} value={province.id}>{province.name}</option>
                  ))}
                </select>
              </div>

              {profile.location.provinceId && (
                <div className="form-group" style={{ marginBottom: '16px' }}>
                  <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                    District:
                  </label>
                  <select
                    value={profile.location.districtId || ''}
                    onChange={(e) => handleLocationChange('districtId', e.target.value, e.target.options[e.target.selectedIndex].text)}
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      borderRadius: '8px',
                      border: '1px solid var(--border)',
                      background: 'rgba(255, 255, 255, 0.04)',
                      color: 'var(--text)',
                      fontSize: '14px'
                    }}
                  >
                    <option value="">Select District</option>
                    {districts.map(district => (
                      <option key={district.id} value={district.id}>{district.name}</option>
                    ))}
                  </select>
                </div>
              )}

              {profile.location.districtId && (
                <div className="form-group" style={{ marginBottom: '16px' }}>
                  <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                    Sector:
                  </label>
                  <select
                    value={profile.location.sectorId || ''}
                    onChange={(e) => handleLocationChange('sectorId', e.target.value, e.target.options[e.target.selectedIndex].text)}
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      borderRadius: '8px',
                      border: '1px solid var(--border)',
                      background: 'rgba(255, 255, 255, 0.04)',
                      color: 'var(--text)',
                      fontSize: '14px'
                    }}
                  >
                    <option value="">Select Sector</option>
                    {sectors.map(sector => (
                      <option key={sector.id} value={sector.id}>{sector.name}</option>
                    ))}
                  </select>
                </div>
              )}

              {profile.location.sectorId && (
                <div className="form-group" style={{ marginBottom: '16px' }}>
                  <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                    Cell:
                  </label>
                  <select
                    value={profile.location.cellId || ''}
                    onChange={(e) => {
                      const selectedCell = cells.find(c => c.id.toString() === e.target.value);
                      handleLocationChange('cellId', e.target.value, selectedCell?.name || '');
                    }}
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      borderRadius: '8px',
                      border: '1px solid var(--border)',
                      background: 'rgba(255, 255, 255, 0.04)',
                      color: 'var(--text)',
                      fontSize: '14px'
                    }}
                  >
                    <option value="">Select Cell</option>
                    {cells.map(cell => (
                      <option key={cell.id} value={cell.id}>{cell.name}</option>
                    ))}
                  </select>
                </div>
              )}

              {profile.location.cellId && (
                <div className="form-group" style={{ marginBottom: '16px' }}>
                  <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                    Village:
                  </label>
                  <select
                    value={profile.location.villageId || ''}
                    onChange={(e) => {
                      const selectedVillage = villages.find(v => v.id.toString() === e.target.value);
                      handleLocationChange('villageId', e.target.value, selectedVillage?.name || '');
                    }}
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      borderRadius: '8px',
                      border: '1px solid var(--border)',
                      background: 'rgba(255, 255, 255, 0.04)',
                      color: 'var(--text)',
                      fontSize: '14px'
                    }}
                  >
                    <option value="">Select Village</option>
                    {villages.map(village => (
                      <option key={village.id} value={village.id}>{village.name}</option>
                    ))}
                  </select>
                </div>
              )}
            </div>

            <Button type="submit" disabled={loading} fullWidth>
              {loading ? 'Updating...' : 'Update Profile'}
            </Button>
          </form>
          </div>
        )}

        {/* Password Change Form - Available for all users */}
        <div className="card">
        <h2 style={{ marginBottom: '24px', fontSize: '20px', fontWeight: '600' }}>Change Password</h2>
        <form onSubmit={handlePasswordChange}>
          <div className="form-group" style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
              Current Password:
            </label>
            <input
              type="password"
              value={passwordData.currentPassword}
              onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})}
              required
              style={{
                width: '100%',
                padding: '12px 16px',
                borderRadius: '8px',
                border: '1px solid var(--border)',
                background: 'rgba(255, 255, 255, 0.04)',
                color: 'var(--text)',
                fontSize: '14px'
              }}
          />
        </div>

          <div className="form-group" style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
              New Password:
            </label>
          <input
              type="password"
              value={passwordData.newPassword}
              onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})}
              required
              minLength={6}
              style={{
                width: '100%',
                padding: '12px 16px',
                borderRadius: '8px',
                border: '1px solid var(--border)',
                background: 'rgba(255, 255, 255, 0.04)',
                color: 'var(--text)',
                fontSize: '14px'
              }}
          />
        </div>

          <div className="form-group" style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
              Confirm New Password:
            </label>
            <input
              type="password"
              value={passwordData.confirmPassword}
              onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
              required
              minLength={6}
              style={{
                width: '100%',
                padding: '12px 16px',
                borderRadius: '8px',
                border: '1px solid var(--border)',
                background: 'rgba(255, 255, 255, 0.04)',
                color: 'var(--text)',
                fontSize: '14px'
              }}
            />
        </div>

          <Button type="submit" disabled={passwordLoading} fullWidth>
            {passwordLoading ? 'Changing...' : 'Change Password'}
          </Button>
        </form>
        </div>
      </div>
    </div>
  );
};

export default Profile;
