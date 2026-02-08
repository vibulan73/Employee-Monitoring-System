import React, { useState, useEffect } from 'react';
import { getEmployeeStats } from '../services/api';

const EmployeeDetailsModal = ({ employee, onClose }) => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dateRange, setDateRange] = useState({
        from: new Date(new Date().setDate(new Date().getDate() - 30)).toISOString().split('T')[0], // Last 30 days
        to: new Date().toISOString().split('T')[0]
    });

    useEffect(() => {
        if (employee) {
            fetchStats();
        }
    }, [employee, dateRange]);

    const fetchStats = async () => {
        setLoading(true);
        try {
            const response = await getEmployeeStats(employee.id, dateRange.from, dateRange.to);
            setStats(response.data);
        } catch (error) {
            console.error('Error fetching employee stats:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDateChange = (e) => {
        setDateRange({
            ...dateRange,
            [e.target.name]: e.target.value
        });
    };

    if (!employee) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content modal-large" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <div className="header-info">
                        <h3>👤 {employee.firstName} {employee.lastName}</h3>
                        <span className="role-badge">{employee.jobRole}</span>
                    </div>
                    <button className="close-btn" onClick={onClose}>✕</button>
                </div>

                <div className="modal-body">
                    {/* Employee Basic Info */}
                    <div className="info-grid">
                        <div className="info-item">
                            <label>User ID</label>
                            <span>{employee.userId}</span>
                        </div>
                        <div className="info-item">
                            <label>Phone</label>
                            <span>{employee.phoneNumber}</span>
                        </div>
                        <div className="info-item">
                            <label>Status</label>
                            <span className={`status-badge ${employee.status === 'ACTIVE' ? 'active' : 'inactive'}`}>
                                {employee.status || 'ACTIVE'}
                            </span>
                        </div>
                        <div className="info-item">
                            <label>Joined</label>
                            <span>{new Date(employee.createdAt).toLocaleDateString()}</span>
                        </div>
                    </div>

                    <div className="divider"></div>

                    {/* Stats Filter */}
                    {/* Stats Filter and Login Info */}
                    <div className="filter-controls">
                        <div>
                            <h4>📊 Activity Statistics</h4>
                            {stats && (
                                <div style={{ fontSize: '0.85rem', color: '#666', marginTop: '4px' }}>
                                    Type of Login: <strong>{stats.loginRuleName || 'N/A'}</strong>
                                </div>
                            )}
                        </div>
                        <div className="date-inputs">
                            <input
                                type="date"
                                name="from"
                                value={dateRange.from}
                                onChange={handleDateChange}
                            />
                            <span>to</span>
                            <input
                                type="date"
                                name="to"
                                value={dateRange.to}
                                onChange={handleDateChange}
                            />
                        </div>
                    </div>

                    {/* Stats Grid */}
                    {loading ? (
                        <div className="loading-stats">Loading statistics...</div>
                    ) : stats ? (
                        <>
                            <div className="stats-grid">
                                <div className="stat-card">
                                    <span className="stat-value">{stats.totalWorkingDays}</span>
                                    <span className="stat-label">Total Working Days</span>
                                </div>
                                <div className="stat-card">
                                    <span className="stat-value">{stats.totalWorkingHours} hr</span>
                                    <span className="stat-label">Total Working Hours</span>
                                </div>
                                <div className="stat-card active-hours">
                                    <span className="stat-value">{stats.totalActiveHours} hr</span>
                                    <span className="stat-label">Total Active Hours</span>
                                </div>
                                <div className="stat-card idle-hours">
                                    <span className="stat-value">{stats.totalIdleHours} hr</span>
                                    <span className="stat-label">Total Idle Hours</span>
                                </div>
                            </div>

                            <div className="divider"></div>

                            {/* Session History Table */}
                            <h4>🕒 Session History</h4>
                            <div className="session-table-container" style={{ maxHeight: '200px', overflowY: 'auto' }}>
                                <table className="employee-table" style={{ fontSize: '0.85rem' }}>
                                    <thead style={{ position: 'sticky', top: 0, background: 'white' }}>
                                        <tr>
                                            <th>S.No</th>
                                            <th>Date</th>
                                            <th>Start Time</th>
                                            <th>End Time</th>
                                            <th>Task Name</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {stats.recentSessions && stats.recentSessions.length > 0 ? (
                                            stats.recentSessions.map((session, index) => (
                                                <tr key={session.sessionId}>
                                                    <td>{index + 1}</td>
                                                    <td>{new Date(session.startTime).toLocaleDateString()}</td>
                                                    <td>{new Date(session.startTime).toLocaleTimeString()}</td>
                                                    <td>{session.endTime ? new Date(session.endTime).toLocaleTimeString() : '-'}</td>
                                                    <td style={{ maxWidth: '150px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                                        {session.taskName || '-'}
                                                    </td>
                                                    <td>
                                                        <span className={`status-badge ${session.status === 'ACTIVE' ? 'active' : 'inactive'}`}>
                                                            {session.status}
                                                        </span>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="6" style={{ textAlign: 'center', color: '#999' }}>No sessions found for this period</td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </>
                    ) : (
                        <div className="error-msg">Failed to load statistics</div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default EmployeeDetailsModal;
