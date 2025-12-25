import React, { useState, useEffect, useCallback } from 'react';
import DataTable from '../components/ui/DataTable';
import api from '../api';
import { toast } from 'react-toastify';

const Locations = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [search, setSearch] = useState('');
  const pageSize = 10;

  const fetchLocations = useCallback(async (showLoading = true) => {
    try {
      if (showLoading) {
        setLoading(true);
      }
      // Fetch only locations that are associated with users (userLocationsOnly=true)
      const response = await api.get('/locations/page', {
        params: { page, size: pageSize, q: search, userLocationsOnly: true }
      });
      setData(response.data.content || response.data || []);
      setTotal(response.data.totalElements || response.data.length || 0);
    } catch (error) {
      console.error('Error fetching locations:', error);
      if (showLoading) {
        toast.error('Failed to load locations');
      }
      setData([]);
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  }, [page, search]);

  useEffect(() => {
    fetchLocations();
    
    // Set up real-time auto-refresh interval (every 5 seconds)
    const refreshInterval = setInterval(() => {
      // Only refresh if tab is visible
      if (!document.hidden) {
        fetchLocations(false); // Don't show loading spinner on auto-refresh
      }
    }, 5000); // 5 seconds for real-time updates
    
    // Cleanup interval on unmount
    return () => {
      clearInterval(refreshInterval);
    };
  }, [fetchLocations]);

  // Listen for visibility changes to refresh when tab becomes visible
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        fetchLocations(false); // Don't show loading spinner on visibility refresh
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [fetchLocations]);

  const columns = [
    { 
      header: 'Username', 
      accessor: (row) => row.username || 'N/A'
    },
    { 
      header: 'Role', 
      accessor: (row) => row.role || 'N/A'
    },
    { header: 'Province', accessor: 'provinceName' },
    { header: 'District', accessor: 'districtName' },
    { header: 'Sector', accessor: 'sectorName' },
    { header: 'Cell', accessor: 'cellName' },
    { header: 'Village', accessor: 'villageName' }
  ];

  if (loading && data.length === 0) {
    return (
      <div>
        <h2>Locations</h2>
        <div style={{ padding: '20px', textAlign: 'center' }}>Loading...</div>
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <div>
          <h2>User Locations</h2>
          <p style={{ color: 'var(--muted)', fontSize: '14px', marginTop: '4px' }}>
            Locations are automatically saved and displayed when users are created or updated. Updates refresh every 5 seconds.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
          <div className="location-stats">
            <span>Total User Locations: {total}</span>
          </div>
        </div>
      </div>
      
      <DataTable
        columns={columns}
        data={data}
        page={page}
        pageSize={pageSize}
        total={total}
        loading={loading}
        onPageChange={setPage}
        onSearch={(q) => {
          setSearch(q);
          setPage(0);
        }}
      />
    </div>
  );
};

export default Locations;

