import React, { useMemo, useRef } from 'react';
import './table.css';

const DataTable = ({ columns, data, page = 0, pageSize = 10, total = 0, onPageChange, onSearch, loading = false, enableClientSideSearch = true }) => {
  const totalPages = Math.ceil(total / pageSize) || 1;
  const [searchValue, setSearchValue] = React.useState('');
  const prevSearchRef = useRef('');

  // Extract cell value from a row and column definition (for display)
  const getCellValue = (row, col) => {
    // Check if accessor is a function first (for custom rendering)
    if (typeof col.accessor === 'function') {
      try {
        return col.accessor(row);
      } catch (e) {
        return null;
      }
    } else if (typeof col.key === 'function') {
      try {
        return col.key(row);
      } catch (e) {
        return null;
      }
    } else {
      const key = col.key || col.accessor;
      if (typeof key === 'string' && key.includes('.')) {
        return key.split('.').reduce((obj, prop) => obj?.[prop], row);
      } else {
        return row[key];
      }
    }
  };

  // Extract searchable text from a cell value (recursively handles React elements)
  const extractSearchableText = (value) => {
    if (value === null || value === undefined) return '';
    
    // Handle React elements (extract text from children recursively)
    if (typeof value === 'object' && value !== null) {
      // Check if it's a React element
      if (value.props) {
        const children = value.props.children;
        if (children !== undefined && children !== null) {
          if (Array.isArray(children)) {
            return children.map(child => extractSearchableText(child)).join(' ').toLowerCase();
          } else if (typeof children === 'object') {
            return extractSearchableText(children).toLowerCase();
          } else {
            return String(children).toLowerCase();
          }
        }
        // Also check for other props that might contain text (like title, label, etc.)
        const textProps = ['title', 'label', 'alt', 'aria-label'];
        for (const prop of textProps) {
          if (value.props[prop]) {
            return String(value.props[prop]).toLowerCase();
          }
        }
      }
      
      // Handle arrays
      if (Array.isArray(value)) {
        return value.map(v => extractSearchableText(v)).join(' ').toLowerCase();
      }
      
      // Handle plain objects - extract all string values
      if (value.constructor === Object) {
        const objValues = Object.values(value)
          .filter(v => v !== null && v !== undefined)
          .map(v => extractSearchableText(v))
          .join(' ');
        return objValues.toLowerCase();
      }
    }
    
    // Handle primitives
    return String(value).toLowerCase();
  };

  // Client-side filtering: search across all visible columns
  const filteredData = useMemo(() => {
    if (!enableClientSideSearch || !searchValue || !searchValue.trim()) {
      return data || [];
    }

    const searchTerm = searchValue.toLowerCase().trim();
    return (data || []).filter(row => {
      // Search across all columns dynamically
      return columns.some(col => {
        const cellValue = getCellValue(row, col);
        const searchableText = extractSearchableText(cellValue);
        return searchableText.includes(searchTerm);
      });
    });
  }, [data, searchValue, columns, enableClientSideSearch]);

  const handleSearch = (value) => {
    const prevValue = prevSearchRef.current;
    setSearchValue(value);
    prevSearchRef.current = value;
    
    // Reset to first page when search changes
    if (onPageChange && value !== prevValue) {
      onPageChange(0);
    }
    
    // If onSearch callback is provided, also trigger backend search
    if (onSearch) {
      onSearch(value);
    }
  };

  // Apply pagination to filtered data
  const paginatedData = useMemo(() => {
    const dataToPaginate = enableClientSideSearch ? filteredData : (data || []);
    
    // If client-side search is active, apply client-side pagination
    if (enableClientSideSearch && searchValue) {
      const startIndex = page * pageSize;
      const endIndex = startIndex + pageSize;
      return dataToPaginate.slice(startIndex, endIndex);
    }
    
    // Otherwise, use data as-is (backend pagination)
    return dataToPaginate;
  }, [filteredData, data, page, pageSize, enableClientSideSearch, searchValue]);

  const displayData = paginatedData;
  const displayTotal = enableClientSideSearch && searchValue ? filteredData.length : total;
  const displayTotalPages = enableClientSideSearch && searchValue 
    ? Math.ceil(filteredData.length / pageSize) || 1 
    : totalPages;

  return (
    <div className="table-card">
      <div className="table-toolbar">
        <input
          placeholder="Search across all columns..."
          value={searchValue}
          onChange={(e) => handleSearch(e.target.value)}
          title="Search dynamically across all visible table columns"
        />
        <span className="table-info">
          Showing {displayData.length} of {displayTotal} entries
          {enableClientSideSearch && searchValue && filteredData.length !== (data?.length || 0) && (
            <span style={{ marginLeft: '8px', color: 'var(--accent)', fontSize: '12px' }}>
              (filtered from {data?.length || 0} total)
            </span>
          )}
        </span>
      </div>
      {loading ? (
        <div className="table-loading">Loading...</div>
      ) : (
        <table>
          <thead>
            <tr>
              {columns.map((col) => (
                <th key={col.key || col.accessor}>{col.header || col.label}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {displayData.length === 0 ? (
              <tr>
                <td colSpan={columns.length} style={{textAlign: 'center', padding: '20px'}}>No data available</td>
              </tr>
            ) : (
              displayData.map((row, index) => (
                <tr key={row.id || index}>
                  {columns.map((col, colIndex) => {
                    const cellKey = col.key || col.accessor || colIndex;
                    const value = getCellValue(row, col);
                    
                    return (
                      <td key={cellKey}>{value || (value === 0 ? 0 : '-')}</td>
                    );
                  })}
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
      <div className="table-pagination">
        <button 
          disabled={page === 0 || loading} 
          onClick={() => onPageChange && onPageChange(page - 1)}
        >
          Previous
        </button>
        <span className="pagination-info">
          Page {page + 1} of {displayTotalPages} ({displayTotal} total)
        </span>
        <button 
          disabled={page + 1 >= displayTotalPages || loading} 
          onClick={() => onPageChange && onPageChange(page + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default DataTable;

