import React from 'react';
import './button.css';

const Button = ({ children, variant = 'primary', size = 'md', fullWidth = false, ...rest }) => {
  const sizeClass = size === 'sm' ? 'btn--sm' : '';
  return (
    <button className={`btn btn--${variant} ${sizeClass} ${fullWidth ? 'btn--block' : ''}`} {...rest}>
      {children}
    </button>
  );
};

export default Button;

