import axios from 'axios';
import { toast } from 'react-toastify';

const api = axios.create({
  // Use explicit backend URL to avoid proxy issues (403/Forbidden)
  baseURL: 'http://localhost:9000/api',
  timeout: 30000, // 30 second timeout
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      console.log(`[API] Request ${config.url} with token (first 10 chars: ${token.substring(0, 10)}...)`);
      config.headers.Authorization = `Bearer ${token.trim()}`;
    } else {
      console.warn(`[API] Request ${config.url} WITHOUT token`);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      console.error(`[API ERROR] ${error.response.status} from ${error.config.url}`, error.response.data);

      const url = error.config.url || '';
      const isAuthRequest = url.includes('/auth/login') || url.includes('/auth/verify-otp') || url.includes('/auth/reset');
      
      // Unauthorized
      if (error.response.status === 401) {
        if (!isAuthRequest) {
          console.error('[API] 401 Unauthorized detected. Logging out.', { url: error.config.url });
          toast.error('Session expired. Please log in again.');
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          sessionStorage.clear();
          setTimeout(() => {
            window.location.href = '/login';
          }, 1000);
        }
      }
      // Forbidden
      else if (error.response.status === 403) {
        console.warn('[API] 403 Forbidden detected. You do not have permission for this resource.', {
          url: error.config.url,
          roles: JSON.parse(localStorage.getItem('user') || '{}')?.role
        });
        if (!isAuthRequest) {
          toast.error('You do not have permission to perform this action.');
        }
      }
      // Not Found
      else if (error.response.status === 404) {
        if (!isAuthRequest && !url.includes('/page')) {
          const errorMessage = error.response?.data?.message || 'Resource not found';
          toast.error(errorMessage);
        }
      }
      // Server Error
      else if (error.response.status >= 500) {
        if (!isAuthRequest) {
          const errorMessage = error.response?.data?.message || 'Server error. Please try again later.';
          toast.error(`Server error: ${errorMessage}`);
        }
      }
      // Client Error (400, 409, etc.) - Only show if not already handled by component
      else if (error.response.status >= 400 && !isAuthRequest) {
        // Don't show toast for validation errors that are handled in forms
        // Components will handle their own error messages
      }
    } else if (error.request) {
      // Request was made but no response received
      console.error('Network error:', error.request);
      toast.error('Network error. Please check your connection and try again.');
    } else {
      // Something else happened
      console.error('Error:', error.message);
      toast.error(`Error: ${error.message || 'An unexpected error occurred'}`);
    }
    return Promise.reject(error);
  }
);

export default api;

export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem('token', token);
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    localStorage.removeItem('token');
    delete api.defaults.headers.common['Authorization'];
  }
};

export const clearAuth = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  delete api.defaults.headers.common['Authorization'];
};

export const get = (url, config) => api.get(url, config).then((r) => r.data);
export const post = (url, data, config) => api.post(url, data, config).then((r) => r.data);
export const put = (url, data, config) => api.put(url, data, config).then((r) => r.data);
export const del = (url, config) => api.delete(url, config).then((r) => r.data);

