import React from 'react';
import Sidebar from './Sidebar';
import Topbar from './Topbar';
import Footer from './Footer';
import './layout.css';

const MainLayout = ({ children }) => {
  return (
    <div className="layout">
      <Sidebar />
      <div className="layout-content">
        <Topbar />
        <main className="layout-main">{children}</main>
        <Footer />
      </div>
    </div>
  );
};

export default MainLayout;

