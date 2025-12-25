import { useState } from 'react';
import api from '../api';

export const useGlobalSearch = () => {
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const search = async (query) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/search/global', { params: { q: query } });
      setResults(res.data);
    } catch (err) {
      setError('Search failed');
    } finally {
      setLoading(false);
    }
  };

  return { results, loading, error, search };
};

