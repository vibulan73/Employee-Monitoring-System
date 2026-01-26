import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import SessionDetail from './components/SessionDetail';
import EmployeeManagement from './components/EmployeeManagement';
import LoginRules from './components/LoginRules';
import LoginRuleForm from './components/LoginRuleForm';
import './App.css';

function Navigation() {
  const location = useLocation();

  return (
    <nav className="app-nav">
      <Link
        to="/"
        className={location.pathname === '/' ? 'active' : ''}
      >
        📊 Sessions
      </Link>
      <Link
        to="/employees"
        className={location.pathname === '/employees' ? 'active' : ''}
      >
        👥 Employees
      </Link>
      <Link
        to="/admin/login-rules"
        className={location.pathname.startsWith('/admin/login-rules') ? 'active' : ''}
      >
        🔐 Login Rules
      </Link>
    </nav>
  );
}

function App() {
  return (
    <Router>
      <div className="app">
        <header className="app-header">
          <h1>Employee Activity Monitoring System</h1>
          <p>Track and analyze work sessions, activity, and productivity</p>
          <Navigation />
        </header>

        <div className="container">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/session/:sessionId" element={<SessionDetail />} />
            <Route path="/employees" element={<EmployeeManagement />} />
            <Route path="/admin/login-rules" element={<LoginRules />} />
            <Route path="/admin/login-rules/new" element={<LoginRuleForm />} />
            <Route path="/admin/login-rules/:id/edit" element={<LoginRuleForm />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
