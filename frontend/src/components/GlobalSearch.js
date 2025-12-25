import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api';
import './GlobalSearch.css';

const GlobalSearch = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const inputRef = useRef(null);
  const resultsRef = useRef(null);
  const debounceTimer = useRef(null);

  // Debounced search
  const performSearch = useCallback(async (searchQuery) => {
    if (!searchQuery || searchQuery.trim().length < 2) {
      setResults(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const response = await api.get('/search/global', { params: { q: searchQuery.trim() } });
      setResults(response.data);
      setSelectedIndex(-1);
    } catch (error) {
      console.error('Search error:', error);
      setResults(null);
    } finally {
      setLoading(false);
    }
  }, []);

  // Handle input change with debouncing
  useEffect(() => {
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    if (query.trim().length >= 2) {
      debounceTimer.current = setTimeout(() => {
        performSearch(query);
      }, 300); // 300ms debounce
    } else {
      setResults(null);
      setLoading(false);
    }

    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
    };
  }, [query, performSearch]);

  // Keyboard shortcut (Cmd/Ctrl + K)
  useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setIsOpen(true);
        setTimeout(() => inputRef.current?.focus(), 0);
      }
      
      if (e.key === 'Escape' && isOpen) {
        setIsOpen(false);
        setQuery('');
        setResults(null);
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen]);

  const handleResultClick = useCallback((item) => {
    if (!item) return;
    
    setIsOpen(false);
    setQuery('');
    setResults(null);
    
    switch (item.type) {
      case 'user':
        if (user?.role === 'ADMIN') {
          navigate('/users');
        }
        break;
      case 'quiz':
        navigate(`/quizzes`);
        break;
      case 'question':
        if (user?.role === 'INSTRUCTOR' && item.data.quiz?.id) {
          navigate(`/quiz-builder/${item.data.quiz.id}`);
        }
        break;
      case 'attempt':
        navigate(`/quiz-results/${item.data.id}`);
        break;
      default:
        break;
    }
  }, [navigate, user]);

  const getTotalItems = useCallback(() => {
    if (!results) return 0;
    return (results.users?.length || 0) +
           (results.quizzes?.length || 0) +
           (results.questions?.length || 0) +
           (results.attempts?.length || 0);
  }, [results]);

  const getItemByIndex = useCallback((index) => {
    if (!results) return null;
    let current = 0;
    
    if (results.users) {
      if (index < results.users.length) {
        return { type: 'user', data: results.users[index] };
      }
      current += results.users.length;
    }
    
    if (results.quizzes) {
      if (index < current + results.quizzes.length) {
        return { type: 'quiz', data: results.quizzes[index - current] };
      }
      current += results.quizzes.length;
    }
    
    if (results.questions) {
      if (index < current + results.questions.length) {
        return { type: 'question', data: results.questions[index - current] };
      }
      current += results.questions.length;
    }
    
    if (results.attempts) {
      if (index < current + results.attempts.length) {
        return { type: 'attempt', data: results.attempts[index - current] };
      }
    }
    
    return null;
  }, [results]);

  // Keyboard navigation in results
  useEffect(() => {
    if (!isOpen || !results) return;

    const handleKeyDown = (e) => {
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        const totalItems = getTotalItems();
        setSelectedIndex((prev) => (prev < totalItems - 1 ? prev + 1 : prev));
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev > 0 ? prev - 1 : -1));
      } else if (e.key === 'Enter' && selectedIndex >= 0) {
        e.preventDefault();
        const item = getItemByIndex(selectedIndex);
        if (item) {
          handleResultClick(item);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, results, selectedIndex, handleResultClick, getTotalItems, getItemByIndex]);

  const highlightText = (text, query) => {
    if (!text || !query) return text;
    const parts = text.split(new RegExp(`(${query})`, 'gi'));
    return parts.map((part, i) => 
      part.toLowerCase() === query.toLowerCase() ? (
        <mark key={i} style={{ background: '#ffd700', padding: '0 2px' }}>{part}</mark>
      ) : part
    );
  };

  const renderResults = () => {
    if (!isOpen) return null;
    
    if (query.trim().length < 2) {
      return (
        <div className="global-search-results">
          <div className="global-search-empty">
            <p>Type at least 2 characters to search</p>
            <div className="global-search-hint">
              <kbd>⌘</kbd> <kbd>K</kbd> to open search
            </div>
          </div>
        </div>
      );
    }

    if (loading) {
      return (
        <div className="global-search-results">
          <div className="global-search-loading">Searching...</div>
        </div>
      );
    }

    if (!results || getTotalItems() === 0) {
      return (
        <div className="global-search-results">
          <div className="global-search-empty">
            <p>No results found for "{query}"</p>
          </div>
        </div>
      );
    }

    let itemIndex = 0;

    return (
      <div className="global-search-results" ref={resultsRef}>
        {results.users && results.users.length > 0 && (
          <div className="global-search-section">
            <div className="global-search-section-header">
              <span className="global-search-icon">👤</span>
              <span>Users ({results.users.length})</span>
            </div>
            {results.users.map((user) => {
              const currentIndex = itemIndex++;
              const isSelected = selectedIndex === currentIndex;
              return (
                <div
                  key={user.id}
                  className={`global-search-item ${isSelected ? 'selected' : ''}`}
                  onClick={() => handleResultClick({ type: 'user', data: user })}
                >
                  <div className="global-search-item-content">
                    <div className="global-search-item-title">
                      {highlightText(user.username, query)}
                    </div>
                    <div className="global-search-item-meta">
                      {user.email} • {user.role}
                    </div>
                  </div>
                  <div className="global-search-item-arrow">→</div>
                </div>
              );
            })}
          </div>
        )}

        {results.quizzes && results.quizzes.length > 0 && (
          <div className="global-search-section">
            <div className="global-search-section-header">
              <span className="global-search-icon">📝</span>
              <span>Quizzes ({results.quizzes.length})</span>
            </div>
            {results.quizzes.map((quiz) => {
              const currentIndex = itemIndex++;
              const isSelected = selectedIndex === currentIndex;
              return (
                <div
                  key={quiz.id}
                  className={`global-search-item ${isSelected ? 'selected' : ''}`}
                  onClick={() => handleResultClick({ type: 'quiz', data: quiz })}
                >
                  <div className="global-search-item-content">
                    <div className="global-search-item-title">
                      {highlightText(quiz.title, query)}
                    </div>
                    <div className="global-search-item-meta">
                      {quiz.status} • {quiz.durationMinutes} min
                    </div>
                  </div>
                  <div className="global-search-item-arrow">→</div>
                </div>
              );
            })}
          </div>
        )}

        {results.questions && results.questions.length > 0 && (
          <div className="global-search-section">
            <div className="global-search-section-header">
              <span className="global-search-icon">❓</span>
              <span>Questions ({results.questions.length})</span>
            </div>
            {results.questions.map((question) => {
              const currentIndex = itemIndex++;
              const isSelected = selectedIndex === currentIndex;
              return (
                <div
                  key={question.id}
                  className={`global-search-item ${isSelected ? 'selected' : ''}`}
                  onClick={() => handleResultClick({ type: 'question', data: question })}
                >
                  <div className="global-search-item-content">
                    <div className="global-search-item-title">
                      {highlightText(question.text?.substring(0, 60) || 'Question', query)}
                      {question.text?.length > 60 ? '...' : ''}
                    </div>
                    <div className="global-search-item-meta">
                      {question.type} • {question.points} pts • {question.quiz?.title || 'No quiz'}
                    </div>
                  </div>
                  <div className="global-search-item-arrow">→</div>
                </div>
              );
            })}
          </div>
        )}

        {results.attempts && results.attempts.length > 0 && (
          <div className="global-search-section">
            <div className="global-search-section-header">
              <span className="global-search-icon">📊</span>
              <span>Attempts ({results.attempts.length})</span>
            </div>
            {results.attempts.map((attempt) => {
              const currentIndex = itemIndex++;
              const isSelected = selectedIndex === currentIndex;
              return (
                <div
                  key={attempt.id}
                  className={`global-search-item ${isSelected ? 'selected' : ''}`}
                  onClick={() => handleResultClick({ type: 'attempt', data: attempt })}
                >
                  <div className="global-search-item-content">
                    <div className="global-search-item-title">
                      {attempt.quiz?.title || 'Quiz'} by {attempt.user?.username || 'User'}
                    </div>
                    <div className="global-search-item-meta">
                      Score: {attempt.score || 'N/A'}% • {attempt.status}
                    </div>
                  </div>
                  <div className="global-search-item-arrow">→</div>
                </div>
              );
            })}
          </div>
        )}

        <div className="global-search-footer">
          <div className="global-search-hint">
            <kbd>↑</kbd> <kbd>↓</kbd> to navigate • <kbd>Enter</kbd> to select • <kbd>Esc</kbd> to close
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="global-search-container">
      <div className="global-search-input-wrapper">
        <input
          ref={inputRef}
          type="text"
          className="global-search-input"
          placeholder="Search users, quizzes, questions..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setIsOpen(true)}
          onBlur={(e) => {
            // Delay closing to allow clicks on results
            setTimeout(() => {
              if (!resultsRef.current?.contains(document.activeElement)) {
                setIsOpen(false);
              }
            }, 200);
          }}
        />
        <div className="global-search-shortcut">
          <kbd>⌘</kbd> <kbd>K</kbd>
        </div>
      </div>
      {renderResults()}
    </div>
  );
};

export default GlobalSearch;

