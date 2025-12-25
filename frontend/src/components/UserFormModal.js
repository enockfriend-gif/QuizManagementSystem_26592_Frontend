import React, { useState, useEffect } from 'react';
import Button from './ui/Button';
import { toast } from 'react-toastify';
import api from '../api';
import '../styles/modal.css';

const UserFormModal = ({ isOpen, user, onClose, onSuccess }) => {
  const [form, setForm] = useState({ 
    username: '', 
    email: '', 
    password: '', 
    role: 'STUDENT',
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
  const [loading, setLoading] = useState(false);
  const [provinces, setProvinces] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [sectors, setSectors] = useState([]);
  const [cells, setCells] = useState([]);
  const [villages, setVillages] = useState([]);
  const [loadingLocations, setLoadingLocations] = useState(false);
  const isEditing = !!user?.id;

  // Load provinces on mount
  useEffect(() => {
    if (isOpen) {
      fetchProvinces();
    }
  }, [isOpen]);

  // Load user data when editing
  useEffect(() => {
    if (user && isOpen) {
      setForm({
        username: user.username || '',
        email: user.email || '',
        password: '',
        role: user.role || 'STUDENT',
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

      // Load cascading dropdowns if location data exists
      if (user.location) {
        if (user.location.provinceId) {
          fetchDistricts(user.location.provinceId).then(() => {
            if (user.location.districtId) {
              fetchSectors(user.location.provinceId, user.location.districtId).then(() => {
                if (user.location.sectorId) {
                  fetchCells(user.location.provinceId, user.location.districtId, user.location.sectorId).then(() => {
                    if (user.location.cellId) {
                      fetchVillages(user.location.provinceId, user.location.districtId, user.location.sectorId, user.location.cellId);
                    }
                  });
                }
              });
            }
          });
        }
      }
    } else if (isOpen) {
      setForm({ 
        username: '', 
        email: '', 
        password: '', 
        role: 'STUDENT',
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
      setDistricts([]);
      setSectors([]);
      setCells([]);
      setVillages([]);
    }
  }, [user, isOpen]);

  const fetchProvinces = async () => {
    try {
      setLoadingLocations(true);
      const response = await api.get('/locations/provinces');
      const provincesData = response.data || [];
      console.log('Fetched provinces:', provincesData);
      console.log('Number of provinces:', provincesData.length);
      setProvinces(provincesData);
      if (provincesData.length === 0) {
        console.warn('No provinces found! The seeder may need to run.');
        toast.warning('No provinces found. Please ensure the location seeder has run.');
      }
    } catch (error) {
      console.error('Error fetching provinces:', error);
      toast.error('Failed to load provinces');
    } finally {
      setLoadingLocations(false);
    }
  };

  const fetchDistricts = async (provinceId) => {
    if (!provinceId) {
      setDistricts([]);
      return;
    }
    try {
      setLoadingLocations(true);
      const response = await api.get('/locations/districts', { params: { provinceId } });
      const districtsData = response.data || [];
      console.log(`Fetched districts for province ${provinceId}:`, districtsData);
      console.log('Number of districts:', districtsData.length);
      setDistricts(districtsData);
    } catch (error) {
      console.error('Error fetching districts:', error);
      toast.error('Failed to load districts');
    } finally {
      setLoadingLocations(false);
    }
  };

  const fetchSectors = async (provinceId, districtId) => {
    if (!provinceId || !districtId) {
      setSectors([]);
      return;
    }
    try {
      setLoadingLocations(true);
      const response = await api.get('/locations/sectors', { 
        params: { provinceId, districtId } 
      });
      const sectorsData = response.data || [];
      console.log(`Fetched sectors for province ${provinceId}, district ${districtId}:`, sectorsData);
      console.log('Number of sectors:', sectorsData.length);
      setSectors(sectorsData);
    } catch (error) {
      console.error('Error fetching sectors:', error);
      toast.error('Failed to load sectors');
    } finally {
      setLoadingLocations(false);
    }
  };

  const fetchCells = async (provinceId, districtId, sectorId) => {
    if (!provinceId || !districtId || !sectorId) {
      setCells([]);
      return;
    }
    try {
      setLoadingLocations(true);
      const response = await api.get('/locations/cells', { 
        params: { provinceId, districtId, sectorId } 
      });
      const cellsData = response.data || [];
      console.log(`Fetched cells for province ${provinceId}, district ${districtId}, sector ${sectorId}:`, cellsData);
      console.log('Number of cells:', cellsData.length);
      setCells(cellsData);
    } catch (error) {
      console.error('Error fetching cells:', error);
      toast.error('Failed to load cells');
    } finally {
      setLoadingLocations(false);
    }
  };

  const fetchVillages = async (provinceId, districtId, sectorId, cellId) => {
    if (!provinceId || !districtId || !sectorId || !cellId) {
      setVillages([]);
      return;
    }
    try {
      setLoadingLocations(true);
      const response = await api.get('/locations/villages', { 
        params: { provinceId, districtId, sectorId, cellId } 
      });
      const villagesData = response.data || [];
      console.log(`Fetched villages for province ${provinceId}, district ${districtId}, sector ${sectorId}, cell ${cellId}:`, villagesData);
      console.log('Number of villages:', villagesData.length);
      setVillages(villagesData);
    } catch (error) {
      console.error('Error fetching villages:', error);
      toast.error('Failed to load villages');
    } finally {
      setLoadingLocations(false);
    }
  };

  // RFC 5322 compliant email validation regex
  const isValidEmail = (email) => {
    if (!email) return false;
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailPattern.test(email.trim());
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleLocationChange = (field, value, displayName = null) => {
    setForm((prev) => {
      const newLocation = { ...prev.location };
      
      if (field === 'provinceId') {
        newLocation.provinceId = value;
        // Convert value to number for comparison, handle both string and number IDs
        const provinceIdNum = value && value !== '' ? (typeof value === 'string' ? Number(value) : value) : null;
        
        // Find province using multiple comparison methods
        const selectedProvince = provinceIdNum ? provinces.find(p => {
          const pId = typeof p.id === 'string' ? Number(p.id) : p.id;
          return pId === provinceIdNum || 
                 p.id === provinceIdNum || 
                 p.id === value || 
                 String(p.id) === String(value) ||
                 String(p.id) === String(provinceIdNum);
        }) : null;
        
        // Set provinceName - prioritize displayName, then selectedProvince, otherwise keep existing or empty
        if (displayName && displayName.trim() !== '') {
          newLocation.provinceName = displayName;
        } else if (selectedProvince && selectedProvince.name) {
          newLocation.provinceName = selectedProvince.name;
        } else if (!value || value === '' || value === '0') {
          newLocation.provinceName = '';
        }
        // If value exists but name not found, log warning but keep existing name if any
        
        // Reset dependent fields
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
        if (value && value !== '' && value !== '0' && provinceIdNum) {
          fetchDistricts(provinceIdNum);
        }
      } else if (field === 'districtId') {
        newLocation.districtId = value;
        newLocation.districtName = displayName || districts.find(d => d.id === value)?.name || '';
        // Reset dependent fields
        newLocation.sectorId = '';
        newLocation.sectorName = '';
        newLocation.cellId = '';
        newLocation.cellName = '';
        newLocation.villageId = '';
        newLocation.villageName = '';
        setSectors([]);
        setCells([]);
        setVillages([]);
        if (value && prev.location.provinceId) {
          fetchSectors(prev.location.provinceId, value);
        }
      } else if (field === 'sectorId') {
        newLocation.sectorId = value;
        newLocation.sectorName = displayName || sectors.find(s => s.id === value)?.name || '';
        // Reset dependent fields
        newLocation.cellId = '';
        newLocation.cellName = '';
        newLocation.villageId = '';
        newLocation.villageName = '';
        setCells([]);
        setVillages([]);
        if (value && prev.location.provinceId && prev.location.districtId) {
          fetchCells(prev.location.provinceId, prev.location.districtId, value);
        }
      } else if (field === 'cellId') {
        newLocation.cellId = value;
        newLocation.cellName = displayName || cells.find(c => c.id === value)?.name || '';
        // Reset dependent fields
        newLocation.villageId = '';
        newLocation.villageName = '';
        setVillages([]);
        if (value && prev.location.provinceId && prev.location.districtId && prev.location.sectorId) {
          fetchVillages(prev.location.provinceId, prev.location.districtId, prev.location.sectorId, value);
        }
      } else if (field === 'villageId') {
        newLocation.villageId = value;
        newLocation.villageName = displayName || villages.find(v => v.id === value)?.name || '';
      }
      
      return { ...prev, location: newLocation };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!form.username || !form.email) {
      toast.error('Username and email are required');
      return;
    }

    // Validate email format (any valid domain)
    if (!isValidEmail(form.email)) {
      toast.error('Please enter a valid email address');
      return;
    }

    if (!isEditing && !form.password) {
      toast.error('Password is required for new users');
      return;
    }

    // Validate that location is provided (required)
    // Check if provinceId is empty, null, undefined, or '0'
    const provinceId = form.location.provinceId;
    const provinceName = form.location.provinceName;
    if (!provinceId || provinceId === '' || provinceId === '0' || !provinceName || provinceName.trim() === '') {
      toast.error('Province is required. Please select a province for the user location.');
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      toast.error('Please sign in via OTP to perform this action.');
      return;
    }

    setLoading(true);
    try {
      // Prepare location data - province is required
      // Ensure provinceId is a valid number
      const provinceIdNum = Number(form.location.provinceId);
      if (isNaN(provinceIdNum) || provinceIdNum === 0) {
        toast.error('Invalid province selected. Please select a valid province.');
        setLoading(false);
        return;
      }
      
      // Ensure provinceName is set
      let provinceNameToUse = form.location.provinceName;
      if (!provinceNameToUse || provinceNameToUse.trim() === '') {
        // Try to find province name from the provinces list
        const foundProvince = provinces.find(p => p.id === provinceIdNum || String(p.id) === String(form.location.provinceId));
        provinceNameToUse = foundProvince?.name || '';
        if (!provinceNameToUse) {
          toast.error('Province name not found. Please reselect the province.');
          setLoading(false);
          return;
        }
      }
      
      const locationData = {
        provinceId: provinceIdNum,
        provinceName: provinceNameToUse,
        districtId: form.location.districtId ? Number(form.location.districtId) : null,
        districtName: form.location.districtName || null,
        sectorId: form.location.sectorId ? Number(form.location.sectorId) : null,
        sectorName: form.location.sectorName || null,
        cellId: form.location.cellId ? Number(form.location.cellId) : null,
        cellName: form.location.cellName || null,
        villageId: form.location.villageId ? Number(form.location.villageId) : null,
        villageName: form.location.villageName || null
      };

      const payload = {
        username: form.username,
        email: form.email,
        role: form.role,
        ...(isEditing && form.password ? { password: form.password } : {}),
        ...(!isEditing ? { password: form.password } : {}),
        location: locationData
      };

      if (isEditing) {
        await api.put(`/users/${user.id}`, payload);
        toast.success('User updated successfully');
      } else {
        const response = await api.post('/users/createUser', payload);
        toast.success(`User "${form.username}" created successfully!`);
        console.log('[UserFormModal] Created user:', response.data);
      }
      onSuccess();
      onClose();
    } catch (error) {
      console.error('User creation/update error:', error);
      console.error('Error response:', error.response?.data);
      const status = error.response?.status;
      const serverMsg = error.response?.data?.message || error.response?.data?.error || error.response?.data;
      
      if (status === 401 || status === 403) {
        toast.error('Session expired. Please sign in again via OTP.');
      } else if (status === 409) {
        toast.error('User already exists with this email or username.');
      } else if (status === 400) {
        toast.error(typeof serverMsg === 'string' ? serverMsg : 'Invalid data provided.');
      } else if (status === 500) {
        // Show more detailed error for 500 errors
        const errorMessage = typeof serverMsg === 'string' ? serverMsg : 
                           (error.response?.data?.message || 'Internal server error. Please check the console for details.');
        toast.error(`Server error: ${errorMessage}`);
        console.error('Full error details:', {
          status,
          data: error.response?.data,
          message: error.message
        });
      } else if (serverMsg && typeof serverMsg === 'string') {
        toast.error(serverMsg);
      } else {
        toast.error(`Operation failed (${status || 'Unknown error'}). Please check your input and try again.`);
      }
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '600px', maxHeight: '90vh', overflowY: 'auto' }}>
        <div className="modal-header">
          <h3>{isEditing ? 'Edit User' : 'Create New User'}</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>Full Address: User Name</label>
            <input
              type="text"
              name="username"
              value={form.username}
              onChange={handleChange}
              placeholder="Enter username"
              required
            />
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="example@domain.com"
              required
              pattern="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"
              title="Please enter a valid email address"
            />
            {form.email && !isValidEmail(form.email) && (
              <small style={{ color: '#ff4444', display: 'block', marginTop: '4px' }}>
                Please enter a valid email address
              </small>
            )}
          </div>

          {!isEditing && (
            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="Enter password"
                required={!isEditing}
              />
            </div>
          )}

          {isEditing && (
            <div className="form-group">
              <label>Password (leave empty to keep current)</label>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="Enter new password (optional)"
              />
            </div>
          )}

          <div className="form-group">
            <label>Role</label>
            <select name="role" value={form.role} onChange={handleChange}>
              <option value="STUDENT">Student</option>
              <option value="INSTRUCTOR">Instructor</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          <div className="form-group">
            <label>
              Province <span style={{ color: '#ff4444' }}>*</span> {provinces.length > 0 && <span style={{ color: 'var(--muted)', fontSize: '12px', fontWeight: '400' }}>({provinces.length} available)</span>}
            </label>
            <select
              value={form.location.provinceId}
              onChange={(e) => {
                const value = e.target.value;
                console.log('Province selected:', value, 'Available provinces:', provinces);
                // Convert to number for comparison
                const provinceIdNum = value ? Number(value) : null;
                const selected = provinces.find(p => {
                  // Try multiple comparison methods
                  return p.id === provinceIdNum || 
                         p.id === value || 
                         String(p.id) === String(value) ||
                         Number(p.id) === provinceIdNum;
                });
                console.log('Selected province:', selected);
                if (!selected && value) {
                  console.error('Province not found in list!', { value, provinceIdNum, provinces });
                }
                handleLocationChange('provinceId', value, selected?.name);
              }}
              disabled={loadingLocations}
              required
            >
              <option value="">Select Province (Required)</option>
              {provinces.length === 0 && !loadingLocations && (
                <option value="" disabled>No provinces found. Please reseed locations.</option>
              )}
              {provinces.length === 0 && loadingLocations && (
                <option value="" disabled>Loading provinces...</option>
              )}
              {provinces.map((province) => (
                <option key={province.id} value={province.id}>
                  {province.name}
                </option>
              ))}
            </select>
            {provinces.length < 5 && provinces.length > 0 && (
              <small style={{ color: '#ffd93d', display: 'block', marginTop: '4px' }}>
                Warning: Only {provinces.length} provinces found. Expected 5. Go to Locations tab and click "Reseed Locations".
              </small>
            )}
          </div>

          {form.location.provinceId && (
            <div className="form-group">
              <label>District</label>
              <select
                value={form.location.districtId}
                onChange={(e) => {
                  const selected = districts.find(d => d.id === e.target.value);
                  handleLocationChange('districtId', e.target.value, selected?.name);
                }}
                disabled={loadingLocations || !districts.length}
              >
                <option value="">Select District</option>
                {districts.map((district) => (
                  <option key={district.id} value={district.id}>
                    {district.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          {form.location.districtId && (
            <div className="form-group">
              <label>Sector</label>
              <select
                value={form.location.sectorId}
                onChange={(e) => {
                  const selected = sectors.find(s => s.id === e.target.value);
                  handleLocationChange('sectorId', e.target.value, selected?.name);
                }}
                disabled={loadingLocations || !sectors.length}
              >
                <option value="">Select Sector</option>
                {sectors.map((sector) => (
                  <option key={sector.id} value={sector.id}>
                    {sector.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          {form.location.sectorId && (
            <div className="form-group">
              <label>Cell</label>
              <select
                value={form.location.cellId}
                onChange={(e) => {
                  const selected = cells.find(c => c.id === e.target.value);
                  handleLocationChange('cellId', e.target.value, selected?.name);
                }}
                disabled={loadingLocations || !cells.length}
              >
                <option value="">Select Cell</option>
                {cells.map((cell) => (
                  <option key={cell.id} value={cell.id}>
                    {cell.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          {form.location.cellId && (
            <div className="form-group">
              <label>Village</label>
              <select
                value={form.location.villageId}
                onChange={(e) => {
                  const selected = villages.find(v => v.id === e.target.value);
                  handleLocationChange('villageId', e.target.value, selected?.name);
                }}
                disabled={loadingLocations || !villages.length}
              >
                <option value="">Select Village</option>
                {villages.map((village) => (
                  <option key={village.id} value={village.id}>
                    {village.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="modal-actions">
            <Button type="button" onClick={onClose} style={{ background: '#888' }}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading || loadingLocations}>
              {loading ? 'Saving...' : isEditing ? 'Update' : 'Create'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserFormModal;
