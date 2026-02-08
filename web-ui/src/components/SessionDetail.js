import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getSession, getActivityLogs, getScreenshots } from '../services/api';
import ScreenshotGallery from './ScreenshotGallery';
import websocketService from '../services/websocket';

function SessionDetail() {
    const { sessionId } = useParams();
    const navigate = useNavigate();

    const [session, setSession] = useState(null);
    const [activityLogs, setActivityLogs] = useState([]);
    const [screenshots, setScreenshots] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchSessionData();

        // Initialize WebSocket connection
        websocketService.connect(() => {
            console.log('SessionDetail: WebSocket connected');

            // Subscribe to activity updates for this session
            websocketService.subscribe(`/topic/activity/${sessionId}`, (event) => {
                console.log('Received activity event:', event);
                if (event.eventType === 'ACTIVITY_LOGGED') {
                    handleActivityEvent(event.payload);
                }
            });

            // Subscribe to screenshot updates for this session
            websocketService.subscribe(`/topic/screenshots/${sessionId}`, (event) => {
                console.log('Received screenshot event:', event);
                if (event.eventType === 'SCREENSHOT_UPLOADED') {
                    handleScreenshotEvent(event.payload);
                }
            });

            // Subscribe to session updates
            websocketService.subscribe('/topic/sessions', (event) => {
                console.log('Received session event:', event);
                if (event.payload.sessionId === sessionId) {
                    handleSessionUpdate(event.payload);
                }
            });
        });

        // Cleanup on unmount
        return () => {
            websocketService.unsubscribe(`/topic/activity/${sessionId}`);
            websocketService.unsubscribe(`/topic/screenshots/${sessionId}`);
            websocketService.unsubscribe('/topic/sessions');
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [sessionId]);

    const fetchSessionData = async () => {
        setLoading(true);
        try {
            const [sessionRes, activityRes, screenshotsRes] = await Promise.all([
                getSession(sessionId),
                getActivityLogs(sessionId),
                getScreenshots(sessionId)
            ]);

            setSession(sessionRes.data);
            setActivityLogs(activityRes.data);
            setScreenshots(screenshotsRes.data);
        } catch (error) {
            console.error('Error fetching session data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSessionUpdate = (updatedSession) => {
        setSession(updatedSession);
    };

    const handleActivityEvent = (activityLog) => {
        setActivityLogs(prevLogs => {
            // Check if activity log already exists (avoid duplicates)
            const exists = prevLogs.some(log => log.id === activityLog.id);
            if (!exists) {
                return [...prevLogs, activityLog];
            }
            return prevLogs;
        });
    };

    const handleScreenshotEvent = (screenshot) => {
        setScreenshots(prevScreenshots => {
            // Check if screenshot already exists (avoid duplicates)
            const exists = prevScreenshots.some(s => s.id === screenshot.id);
            if (!exists) {
                return [...prevScreenshots, screenshot];
            }
            return prevScreenshots;
        });
    };

    const formatDateTime = (dateTime) => {
        return new Date(dateTime).toLocaleString();
    };

    const formatTime = (dateTime) => {
        return new Date(dateTime).toLocaleTimeString();
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

    const calculateActivityStats = () => {
        if (activityLogs.length === 0) return { active: 0, idle: 0 };

        const activeCount = activityLogs.filter(log => log.activityStatus === 'ACTIVE').length;
        const idleCount = activityLogs.filter(log => log.activityStatus === 'IDLE').length;
        const total = activityLogs.length;

        return {
            active: ((activeCount / total) * 100).toFixed(1),
            idle: ((idleCount / total) * 100).toFixed(1)
        };
    };

    if (loading) {
        return (
            <div className="session-detail">
                <div className="loading">
                    <div className="loading-spinner"></div>
                    <p>Loading session details...</p>
                </div>
            </div>
        );
    }

    if (!session) {
        return (
            <div className="session-detail">
                <div className="empty-state">
                    <h3>Session Not Found</h3>
                    <button className="back-button" onClick={() => navigate('/')}>
                        Back to Dashboard
                    </button>
                </div>
            </div>
        );
    }

    const stats = calculateActivityStats();

    return (
        <div className="session-detail">
            <button className="back-button" onClick={() => navigate('/')}>
                ← Back to Dashboard
            </button>

            <div className="session-detail-header">
                <h2>Session Details</h2>
                <div className="session-metadata">
                    <div className="metadata-item">
                        <label>Session ID</label>
                        <div style={{ fontFamily: 'monospace', fontSize: '14px' }}>
                            {session.sessionId}
                        </div>
                    </div>
                    <div className="metadata-item">
                        <label>User</label>
                        <div>
                            <div style={{ fontSize: '16px', fontWeight: '600' }}>
                                {session.firstName && session.lastName
                                    ? `${session.firstName} ${session.lastName}`
                                    : session.userId}
                            </div>
                            {session.jobRole && (
                                <span className="job-role-badge" style={{ marginTop: '8px', display: 'inline-block' }}>
                                    {session.jobRole}
                                </span>
                            )}
                            {!session.firstName && (
                                <div style={{ fontSize: '12px', color: '#7f8c8d', marginTop: '4px' }}>
                                    ID: {session.userId}
                                </div>
                            )}
                        </div>
                    </div>
                    <div className="metadata-item">
                        <label>Status</label>
                        <div>
                            <span className={`session-status ${session.status.toLowerCase()}`}>
                                {session.status}
                            </span>
                        </div>
                    </div>
                    <div className="metadata-item">
                        <label>Start Time</label>
                        <div>{formatDateTime(session.startTime)}</div>
                    </div>
                    {session.endTime && (
                        <div className="metadata-item">
                            <label>End Time</label>
                            <div>{formatDateTime(session.endTime)}</div>
                        </div>
                    )}
                    <div className="metadata-item">
                        <label>Duration</label>
                        <div>{calculateDuration(session.startTime, session.endTime)}</div>
                    </div>
                    {session.taskName && (
                        <div className="metadata-item">
                            <label>Task Name</label>
                            <div style={{ fontSize: '16px', fontWeight: '600', color: '#667eea' }}>
                                {session.taskName}
                            </div>
                        </div>
                    )}
                    {session.estimatedDurationMinutes && (
                        <div className="metadata-item">
                            <label>Estimated Duration</label>
                            <div>{session.estimatedDurationMinutes} minutes</div>
                        </div>
                    )}
                </div>
            </div>

            {/* Activity Statistics */}
            <div className="section">
                <h3 className="section-title">Activity Statistics</h3>
                <div className="session-metadata">
                    <div className="metadata-item">
                        <label>Total Activity Logs</label>
                        <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#667eea' }}>
                            {activityLogs.length}
                        </div>
                    </div>
                    <div className="metadata-item">
                        <label>Active Time</label>
                        <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#27ae60' }}>
                            {stats.active}%
                        </div>
                    </div>
                    <div className="metadata-item">
                        <label>Idle Time</label>
                        <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#e67e22' }}>
                            {stats.idle}%
                        </div>
                    </div>
                    <div className="metadata-item">
                        <label>Screenshots Captured</label>
                        <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#667eea' }}>
                            {screenshots.length}
                        </div>
                    </div>
                </div>
            </div>

            {/* Activity Timeline */}
            <div className="section">
                <h3 className="section-title">Activity Timeline</h3>
                {activityLogs.length === 0 ? (
                    <div className="empty-state">
                        <p>No activity logs recorded yet.</p>
                    </div>
                ) : (
                    <div className="activity-timeline">
                        {activityLogs.map((log) => (
                            <div key={log.id} className="activity-item">
                                <span className={`activity-status-badge ${log.activityStatus.toLowerCase()}`}>
                                    {log.activityStatus}
                                </span>
                                <span className="activity-time">{formatTime(log.loggedAt)}</span>
                                {log.metadata && <span style={{ color: '#7f8c8d' }}>{log.metadata}</span>}
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Screenshots */}
            <div className="section">
                <h3 className="section-title">Screenshots</h3>
                {screenshots.length === 0 ? (
                    <div className="empty-state">
                        <p>No screenshots captured yet.</p>
                    </div>
                ) : (
                    <ScreenshotGallery screenshots={screenshots} />
                )}
            </div>
        </div>
    );
}

export default SessionDetail;
