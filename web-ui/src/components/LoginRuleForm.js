import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createLoginRule, updateLoginRule, getLoginRuleById } from '../services/loginRuleService';

const DAYS_OF_WEEK = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

function LoginRuleForm() {
    const navigate = useNavigate();
    const { id } = useParams();
    const isEditMode = Boolean(id);

    const [formData, setFormData] = useState({
        ruleName: '',
        ruleType: 'ALL_DAYS',
        description: '',
        schedules: []
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        if (isEditMode) {
            fetchLoginRule();
        } else {
            initializeSchedules('ALL_DAYS');
        }
    }, [id]);

    const fetchLoginRule = async () => {
        try {
            const response = await getLoginRuleById(id);
            const rule = response.data;
            setFormData({
                ruleName: rule.ruleName,
                ruleType: rule.ruleType,
                description: rule.description || '',
                schedules: rule.schedules || []
            });
        } catch (error) {
            console.error('Error fetching login rule:', error);
            setError('Failed to load login rule');
        }
    };

    const initializeSchedules = (ruleType) => {
        switch (ruleType) {
            case 'ALL_DAYS':
                setFormData(prev => ({ ...prev, schedules: [] }));
                break;
            case 'ALL_DAYS_WITH_TIME':
                setFormData(prev => ({
                    ...prev,
                    schedules: [{
                        dayOfWeek: 'ALL',
                        startTime: '09:00',
                        endTime: '17:00',
                        isActive: true
                    }]
                }));
                break;
            case 'DAY_ANY_TIME':
                setFormData(prev => ({
                    ...prev,
                    schedules: [{
                        dayOfWeek: 'MONDAY',
                        startTime: null,
                        endTime: null,
                        isActive: true
                    }]
                }));
                break;
            case 'CUSTOM':
                setFormData(prev => ({
                    ...prev,
                    schedules: [{
                        dayOfWeek: 'MONDAY',
                        startTime: '09:00',
                        endTime: '17:00',
                        isActive: true
                    }]
                }));
                break;
            default:
                setFormData(prev => ({ ...prev, schedules: [] }));
        }
    };

    const handleRuleTypeChange = (newType) => {
        setFormData(prev => ({ ...prev, ruleType: newType }));
        initializeSchedules(newType);
    };

    const handleInputChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleScheduleChange = (index, field, value) => {
        const newSchedules = [...formData.schedules];
        newSchedules[index] = {
            ...newSchedules[index],
            [field]: value
        };
        setFormData({ ...formData, schedules: newSchedules });
    };

    const addSchedule = () => {
        const newSchedule = formData.ruleType === 'DAY_ANY_TIME'
            ? { dayOfWeek: 'MONDAY', startTime: null, endTime: null, isActive: true }
            : { dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00', isActive: true };

        setFormData({
            ...formData,
            schedules: [...formData.schedules, newSchedule]
        });
    };

    const removeSchedule = (index) => {
        setFormData({
            ...formData,
            schedules: formData.schedules.filter((_, i) => i !== index)
        });
    };

    const validateForm = () => {
        if (!formData.ruleName.trim()) {
            setError('Rule name is required');
            return false;
        }

        if (formData.ruleType === 'ALL_DAYS_WITH_TIME' && formData.schedules.length !== 1) {
            setError('ALL_DAYS_WITH_TIME must have exactly one schedule');
            return false;
        }

        if (['DAY_ANY_TIME', 'CUSTOM', 'ALL_DAYS_WITH_TIME'].includes(formData.ruleType) && formData.schedules.length === 0) {
            setError('At least one schedule is required for this rule type');
            return false;
        }

        for (const schedule of formData.schedules) {
            if (formData.ruleType !== 'DAY_ANY_TIME') {
                if (!schedule.startTime || !schedule.endTime) {
                    setError('All schedules must have start and end times');
                    return false;
                }
                if (schedule.startTime >= schedule.endTime) {
                    setError('Start time must be before end time');
                    return false;
                }
            }
        }

        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (!validateForm()) {
            return;
        }

        setLoading(true);
        try {
            const payload = {
                ruleName: formData.ruleName,
                ruleType: formData.ruleType,
                description: formData.description,
                schedules: formData.ruleType === 'ALL_DAYS' ? [] : formData.schedules
            };

            if (isEditMode) {
                await updateLoginRule(id, payload);
                setSuccess('Login rule updated successfully!');
            } else {
                await createLoginRule(payload);
                setSuccess('Login rule created successfully!');
            }

            setTimeout(() => {
                navigate('/admin/login-rules');
            }, 1500);
        } catch (error) {
            console.error('Error saving login rule:', error);
            setError(error.response?.data?.message || 'Failed to save login rule');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-rule-form-container">
            <div className="form-header">
                <h2>{isEditMode ? '✏️ Edit Login Rule' : '➕ Create Login Rule'}</h2>
                <button onClick={() => navigate('/admin/login-rules')} className="btn-secondary">
                    ← Back to Rules
                </button>
            </div>

            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <form onSubmit={handleSubmit} className="rule-form">
                <div className="form-section">
                    <h3>Basic Information</h3>

                    <div className="form-group">
                        <label>Rule Name *</label>
                        <input
                            type="text"
                            name="ruleName"
                            value={formData.ruleName}
                            onChange={handleInputChange}
                            placeholder="e.g., Business Hours, Night Shift, Weekends Only"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Rule Type *</label>
                        <select
                            name="ruleType"
                            value={formData.ruleType}
                            onChange={(e) => handleRuleTypeChange(e.target.value)}
                            required
                        >
                            <option value="ALL_DAYS">24/7 Access - No restrictions</option>
                            <option value="ALL_DAYS_WITH_TIME">Daily Time Range - Same time every day</option>
                            <option value="DAY_ANY_TIME">Specific Days - Any time on those days</option>
                            <option value="CUSTOM">Custom Schedule - Specific days and times</option>
                        </select>
                        <small className="help-text">
                            {formData.ruleType === 'ALL_DAYS' && 'Tracking allowed 24/7 without any restrictions'}
                            {formData.ruleType === 'ALL_DAYS_WITH_TIME' && 'Same time window applies to every day of the week'}
                            {formData.ruleType === 'DAY_ANY_TIME' && 'Tracking allowed any time on specified days'}
                            {formData.ruleType === 'CUSTOM' && 'Different time windows for different days'}
                        </small>
                    </div>

                    <div className="form-group">
                        <label>Description</label>
                        <textarea
                            name="description"
                            value={formData.description}
                            onChange={handleInputChange}
                            placeholder="Optional notes about this rule..."
                            rows="3"
                        />
                    </div>
                </div>

                {formData.ruleType !== 'ALL_DAYS' && (
                    <div className="form-section">
                        <div className="section-header">
                            <h3>Schedule Configuration</h3>
                            {formData.ruleType !== 'ALL_DAYS_WITH_TIME' && (
                                <button type="button" onClick={addSchedule} className="btn-add-schedule">
                                    + Add Schedule
                                </button>
                            )}
                        </div>

                        {formData.schedules.map((schedule, index) => (
                            <div key={index} className="schedule-item">
                                <div className="schedule-row">
                                    <div className="form-group">
                                        <label>Day of Week</label>
                                        <select
                                            value={schedule.dayOfWeek}
                                            onChange={(e) => handleScheduleChange(index, 'dayOfWeek', e.target.value)}
                                            disabled={formData.ruleType === 'ALL_DAYS_WITH_TIME'}
                                        >
                                            {formData.ruleType === 'ALL_DAYS_WITH_TIME' && (
                                                <option value="ALL">All Days</option>
                                            )}
                                            {formData.ruleType !== 'ALL_DAYS_WITH_TIME' && DAYS_OF_WEEK.map(day => (
                                                <option key={day} value={day}>{day}</option>
                                            ))}
                                        </select>
                                    </div>

                                    {formData.ruleType !== 'DAY_ANY_TIME' && (
                                        <>
                                            <div className="form-group">
                                                <label>Start Time</label>
                                                <input
                                                    type="time"
                                                    value={schedule.startTime || ''}
                                                    onChange={(e) => handleScheduleChange(index, 'startTime', e.target.value)}
                                                    required
                                                />
                                            </div>

                                            <div className="form-group">
                                                <label>End Time</label>
                                                <input
                                                    type="time"
                                                    value={schedule.endTime || ''}
                                                    onChange={(e) => handleScheduleChange(index, 'endTime', e.target.value)}
                                                    required
                                                />
                                            </div>
                                        </>
                                    )}

                                    {formData.ruleType === 'DAY_ANY_TIME' && (
                                        <div className="all-day-badge">
                                            ⏰ All Day
                                        </div>
                                    )}

                                    {formData.ruleType !== 'ALL_DAYS_WITH_TIME' && (
                                        <button
                                            type="button"
                                            onClick={() => removeSchedule(index)}
                                            className="btn-remove"
                                            title="Remove this schedule"
                                        >
                                            🗑️
                                        </button>
                                    )}
                                </div>

                                {formData.ruleType === 'CUSTOM' && schedule.startTime >= schedule.endTime && (
                                    <div className="warning-message">
                                        ⚠️ For overnight shifts (e.g., 22:00-06:00), create TWO schedules:
                                        Day 1 (22:00-23:59) and Day 2 (00:00-06:00)
                                    </div>
                                )}
                            </div>
                        ))}

                        {formData.schedules.length === 0 && formData.ruleType !== 'ALL_DAYS' && (
                            <div className="empty-schedules">
                                <p>No schedules configured. Click "+ Add Schedule" to begin.</p>
                            </div>
                        )}
                    </div>
                )}

                <div className="form-actions">
                    <button
                        type="button"
                        onClick={() => navigate('/admin/login-rules')}
                        className="btn-secondary"
                        disabled={loading}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="btn-primary"
                        disabled={loading}
                    >
                        {loading ? 'Saving...' : (isEditMode ? 'Update Rule' : 'Create Rule')}
                    </button>
                </div>
            </form>

            <style jsx>{`
                .login-rule-form-container {
                    padding: 20px;
                    max-width: 900px;
                    margin: 0 auto;
                }

                .form-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 30px;
                }

                .form-header h2 {
                    margin: 0;
                    color: #2c3e50;
                }

                .rule-form {
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    padding: 30px;
                }

                .form-section {
                    margin-bottom: 30px;
                    padding-bottom: 30px;
                    border-bottom: 1px solid #ecf0f1;
                }

                .form-section:last-of-type {
                    border-bottom: none;
                }

                .form-section h3 {
                    color: #34495e;
                    margin-bottom: 20px;
                }

                .section-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 20px;
                }

                .form-group {
                    margin-bottom: 20px;
                }

                .form-group label {
                    display: block;
                    margin-bottom: 8px;
                    color: #2c3e50;
                    font-weight: 500;
                }

                .form-group input,
                .form-group select,
                .form-group textarea {
                    width: 100%;
                    padding: 10px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    font-size: 14px;
                }

                .form-group input:focus,
                .form-group select:focus,
                .form-group textarea:focus {
                    outline: none;
                    border-color: #3498db;
                }

                .help-text {
                    display: block;
                    margin-top: 5px;
                    color: #7f8c8d;
                    font-size: 13px;
                }

                .schedule-item {
                    background: #f8f9fa;
                    padding: 15px;
                    border-radius: 6px;
                    margin-bottom: 15px;
                }

                .schedule-row {
                    display: grid;
                    grid-template-columns: 2fr 1.5fr 1.5fr auto;
                    gap: 15px;
                    align-items: end;
                }

                .all-day-badge {
                    background: #27ae60;
                    color: white;
                    padding: 10px 20px;
                    border-radius: 4px;
                    text-align: center;
                    font-weight: 600;
                }

                .btn-remove {
                    padding: 10px 15px;
                    background: #e74c3c;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-size: 16px;
                }

                .btn-remove:hover {
                    background: #c0392b;
                }

                .btn-add-schedule {
                    padding: 8px 16px;
                    background: #27ae60;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-weight: 500;
                }

                .btn-add-schedule:hover {
                    background: #229954;
                }

                .empty-schedules {
                    text-align: center;
                    padding: 40px;
                    color: #95a5a6;
                }

                .warning-message {
                    margin-top: 10px;
                    padding: 10px;
                    background: #fff3cd;
                    border-left: 4px solid #f39c12;
                    color: #856404;
                    font-size: 13px;
                }

                .form-actions {
                    display: flex;
                    gap: 10px;
                    justify-content: flex-end;
                    margin-top: 30px;
                }

                .btn-primary, .btn-secondary {
                    padding: 12px 24px;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-weight: 500;
                    font-size: 14px;
                }

                .btn-primary {
                    background: #3498db;
                    color: white;
                }

                .btn-primary:hover:not(:disabled) {
                    background: #2980b9;
                }

                .btn-primary:disabled {
                    opacity: 0.6;
                    cursor: not-allowed;
                }

                .btn-secondary {
                    background: #95a5a6;
                    color: white;
                }

                .btn-secondary:hover:not(:disabled) {
                    background: #7f8c8d;
                }

                .alert {
                    padding: 15px;
                    border-radius: 4px;
                    margin-bottom: 20px;
                }

                .alert-success {
                    background: #d4edda;
                    color: #155724;
                    border-left: 4px solid #28a745;
                }

                .alert-error {
                    background: #f8d7da;
                    color: #721c24;
                    border-left: 4px solid #dc3545;
                }
            `}</style>
        </div>
    );
}

export default LoginRuleForm;
