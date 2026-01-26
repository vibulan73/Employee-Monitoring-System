import React, { useState, useEffect } from 'react';
import { getAllSessions } from '../services/api';
import { useNavigate } from 'react-router-dom';
import websocketService from '../services/websocket';

function Dashboard() {
    const [sessions, setSessions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filterUserId, setFilterUserId] = useState('');
    const [filterStatus, setFilterStatus] = useState('');

    const navigate = useNavigate();

    useEffect(() => {
        fetchSessions();

        // Initialize WebSocket connection
        websocketService.connect(() => {
            console.log('Dashboard: WebSocket connected');

            // Subscribe to session updates
            websocketService.subscribe('/topic/sessions', (event) => {
                console.log('Received session event:', event);
                handleSessionEvent(event);
            });
        });

        // Cleanup on unmount
        return () => {
            websocketService.unsubscribe('/topic/sessions');
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Refetch when filters change
    useEffect(() => {
        if (filterUserId || filterStatus) {
            fetchSessions();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [filterUserId, filterStatus]);

    const fetchSessions = async () => {
        setLoading(true);
        try {
            const params = {};
            if (filterUserId) params.userId = filterUserId;
            if (filterStatus) params.status = filterStatus;

            const response = await getAllSessions(params);
            // Sort sessions by startTime descending (newest first)
            const sortedSessions = response.data.sort((a, b) =>
                new Date(b.startTime) - new Date(a.startTime)
            );
            setSessions(sortedSessions);
        } catch (error) {
            console.error('Error fetching sessions:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSessionEvent = (event) => {
        const { eventType, payload } = event;

        // Update sessions list based on event type
        setSessions(prevSessions => {
            const sessionIndex = prevSessions.findIndex(s => s.sessionId === payload.sessionId);

            if (eventType === 'SESSION_CREATED') {
                // Add new session only if it doesn't exist and matches filters
                if (sessionIndex === -1) {
                    if (shouldIncludeSession(payload)) {
                        return [payload, ...prevSessions];
                    }
                }
            } else if (eventType === 'SESSION_STOPPED' || eventType === 'SESSION_UPDATED') {
                // Update existing session
                if (sessionIndex !== -1) {
                    const updatedSessions = [...prevSessions];
                    updatedSessions[sessionIndex] = payload;

                    // Remove if no longer matches filters
                    if (!shouldIncludeSession(payload)) {
                        updatedSessions.splice(sessionIndex, 1);
                    }
                    return updatedSessions;
                }
            }

            return prevSessions;
        });
    };

    const shouldIncludeSession = (session) => {
        if (filterUserId && session.userId !== filterUserId) return false;
        if (filterStatus && session.status !== filterStatus) return false;
        return true;
    };

    const handleSessionClick = (sessionId) => {
        navigate(`/session/${sessionId}`);
    };

    const formatDateTime = (dateTime) => {
        return new Date(dateTime).toLocaleString();
    };

    const calculateDuration = (startTime, endTime) => {
        const start = new Date(startTime);
        const end = endTime ? new Date(endTime) : new Date();
        const diffMs = end - start;
        const diffMins = Math.floor(diffMs / 60000);
        const hours = Math.floor(diffMins / 60);
        const minutes = diffMins % 60;
        return `${hours}h ${minutes}m`;
    };

    // Group sessions by userId
    const groupedSessions = sessions.reduce((groups, session) => {
        const userId = session.userId;
        if (!groups[userId]) {
            groups[userId] = [];
        }
        groups[userId].push(session);
        return groups;
    }, {});

    return (
        <div className="dashboard">
            <div className="dashboard-header">
                <h2>Work Sessions</h2>
                <div className="filter-section">
                    <div>
                        <label>Filter by User: </label>
                        <input
                            type="text"
                            placeholder="User ID"
                            value={filterUserId}
                            onChange={(e) => setFilterUserId(e.target.value)}
                        />
                    </div>
                    <div>
                        <label>Status: </label>
                        <select
                            value={filterStatus}
                            onChange={(e) => setFilterStatus(e.target.value)}
                        >
                            <option value="">All</option>
                            <option value="ACTIVE">Active</option>
                            <option value="STOPPED">Stopped</option>
                        </select>
                    </div>
                </div>
            </div>

            {loading ? (
                <div className="loading">
                    <div className="loading-spinner"></div>
                    <p>Loading sessions...</p>
                </div>
            ) : sessions.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon">📊</div>
                    <h3>No Sessions Found</h3>
                    <p>Start the desktop agent to begin monitoring work sessions.</p>
                </div>
            ) : (
                <div className="sessions-by-user">
                    {Object.entries(groupedSessions).map(([userId, userSessions]) => {
                        // Get user details from first session
                        const firstSession = userSessions[0];
                        const hasUserDetails = firstSession.firstName && firstSession.lastName;
                        const userDisplayName = hasUserDetails
                            ? `${firstSession.firstName} ${firstSession.lastName}`
                            : userId;

                        return (
                            <div key={userId} className="user-group">
                                <div className="user-group-header">
                                    <div>
                                        <h3>👤 {userDisplayName}</h3>
                                        {firstSession.jobRole && (
                                            <span className="job-role-badge">{firstSession.jobRole}</span>
                                        )}
                                        {!hasUserDetails && (
                                            <span style={{ color: '#95a5a6', fontSize: '14px', marginLeft: '10px' }}>
                                                ({userId})
                                            </span>
                                        )}
                                    </div>
                                    <span className="session-count">
                                        {userSessions.length} session{userSessions.length > 1 ? 's' : ''}
                                    </span>
                                </div>
                                <div className="sessions-grid">
                                    {userSessions.map((session) => (
                                        <div
                                            key={session.sessionId}
                                            className="session-card"
                                            onClick={() => handleSessionClick(session.sessionId)}
                                        >
                                            <div className="session-card-header">
                                                <span className="session-id">
                                                    {session.sessionId.substring(0, 8)}...
                                                </span>
                                                <span className={`session-status ${session.status.toLowerCase()}`}>
                                                    {session.status}
                                                </span>
                                            </div>

                                            <div className="session-info">
                                                <div className="session-info-item">
                                                    <strong>Started:</strong>
                                                    <span>{formatDateTime(session.startTime)}</span>
                                                </div>
                                                {session.endTime && (
                                                    <div className="session-info-item">
                                                        <strong>Ended:</strong>
                                                        <span>{formatDateTime(session.endTime)}</span>
                                                    </div>
                                                )}
                                                <div className="session-info-item">
                                                    <strong>Duration:</strong>
                                                    <span>{calculateDuration(session.startTime, session.endTime)}</span>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default Dashboard;
