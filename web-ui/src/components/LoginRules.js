import React, { useState, useEffect } from 'react';
import { getAllLoginRules, deleteLoginRule } from '../services/loginRuleService';
import { useNavigate } from 'react-router-dom';

function LoginRules() {
    const [loginRules, setLoginRules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchLoginRules();
    }, []);

    const fetchLoginRules = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await getAllLoginRules();
            setLoginRules(response.data);
        } catch (error) {
            console.error('Error fetching login rules:', error);
            setError('Failed to load login rules. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (ruleId, ruleName) => {
        if (!window.confirm(`Are you sure you want to delete the login rule "${ruleName}"?`)) {
            return;
        }

        try {
            await deleteLoginRule(ruleId);
            setLoginRules(loginRules.filter(rule => rule.id !== ruleId));
            alert(`Login rule "${ruleName}" deleted successfully!`);
        } catch (error) {
            console.error('Error deleting login rule:', error);
            const errorMessage = error.response?.data?.message || 'Failed to delete login rule';
            alert(`Error: ${errorMessage}`);
        }
    };

    const getRuleTypeDisplay = (ruleType) => {
        const typeMap = {
            'ALL_DAYS': '24/7 Access',
            'ALL_DAYS_WITH_TIME': 'Daily Time Range',
            'DAY_ANY_TIME': 'Specific Days',
            'CUSTOM': 'Custom Schedule'
        };
        return typeMap[ruleType] || ruleType;
    };

    const getRuleTypeBadgeClass = (ruleType) => {
        const classMap = {
            'ALL_DAYS': 'badge-success',
            'ALL_DAYS_WITH_TIME': 'badge-info',
            'DAY_ANY_TIME': 'badge-warning',
            'CUSTOM': 'badge-primary'
        };
        return classMap[ruleType] || 'badge-secondary';
    };

    return (
        <div className="login-rules">
            <div className="page-header">
                <h2>🔐 Login Time Restrictions</h2>
                <button
                    className="btn btn-primary"
                    onClick={() => navigate('/admin/login-rules/new')}
                >
                    + Create New Rule
                </button>
            </div>

            {loading ? (
                <div className="loading">
                    <div className="loading-spinner"></div>
                    <p>Loading login rules...</p>
                </div>
            ) : error ? (
                <div className="error-state">
                    <div className="error-icon">❌</div>
                    <h3>Error Loading Rules</h3>
                    <p>{error}</p>
                    <button onClick={fetchLoginRules} className="btn btn-secondary">
                        Retry
                    </button>
                </div>
            ) : loginRules.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon">📋</div>
                    <h3>No Login Rules Found</h3>
                    <p>Create your first login rule to control employee tracking time windows.</p>
                    <button
                        className="btn btn-primary"
                        onClick={() => navigate('/admin/login-rules/new')}
                    >
                        Create First Rule
                    </button>
                </div>
            ) : (
                <div className="rules-table-container">
                    <table className="rules-table">
                        <thead>
                            <tr>
                                <th>Rule Name</th>
                                <th>Type</th>
                                <th>Description</th>
                                <th>Employees</th>
                                <th>Created</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loginRules.map((rule) => (
                                <tr key={rule.id} className={rule.isDefault ? 'default-rule' : ''}>
                                    <td>
                                        <strong>{rule.ruleName}</strong>
                                        {rule.isDefault && (
                                            <span className="badge badge-default" style={{ marginLeft: '8px' }}>
                                                DEFAULT
                                            </span>
                                        )}
                                    </td>
                                    <td>
                                        <span className={`badge ${getRuleTypeBadgeClass(rule.ruleType)}`}>
                                            {getRuleTypeDisplay(rule.ruleType)}
                                        </span>
                                    </td>
                                    <td className="description-cell">
                                        {rule.description || <span style={{ color: '#95a5a6' }}>No description</span>}
                                    </td>
                                    <td className="text-center">
                                        <span className="employee-count-badge">
                                            {rule.assignedEmployeeCount || 0}
                                        </span>
                                    </td>
                                    <td>
                                        {new Date(rule.createdAt).toLocaleDateString()}
                                    </td>
                                    <td className="actions-cell">
                                        <button
                                            className="btn btn-sm btn-primary"
                                            onClick={() => navigate(`/admin/login-rules/${rule.id}/edit`)}
                                            title="Edit Rule"
                                            disabled={rule.isDefault}
                                        >
                                            ✏️ Edit
                                        </button>
                                        <button
                                            className="btn btn-sm btn-danger"
                                            onClick={() => handleDelete(rule.id, rule.ruleName)}
                                            title={rule.isDefault ? "Cannot delete default rule" : "Delete Rule"}
                                            disabled={rule.isDefault || rule.assignedEmployeeCount > 0}
                                        >
                                            🗑️ Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>

                    <div className="rules-summary">
                        <p>
                            <strong>{loginRules.length}</strong> rule{loginRules.length !== 1 ? 's' : ''} total
                            {' | '}
                            <strong>{loginRules.filter(r => r.isDefault).length}</strong> default
                            {' | '}
                            <strong>{loginRules.reduce((sum, r) => sum + (r.assignedEmployeeCount || 0), 0)}</strong> total assignments
                        </p>
                    </div>
                </div>
            )}

            <style jsx>{`
                .login-rules {
                    padding: 20px;
                    max-width: 1400px;
                    margin: 0 auto;
                }

                .page-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 30px;
                    padding-bottom: 15px;
                    border-bottom: 2px solid #ecf0f1;
                }

                .page-header h2 {
                    margin: 0;
                    color: #2c3e50;
                }

                .rules-table-container {
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    overflow: hidden;
                }

                .rules-table {
                    width: 100%;
                    border-collapse: collapse;
                }

                .rules-table thead {
                    background: #34495e;
                    color: white;
                }

                .rules-table th {
                    padding: 12px 15px;
                    text-align: left;
                    font-weight: 600;
                }

                .rules-table td {
                    padding: 12px 15px;
                    border-bottom: 1px solid #ecf0f1;
                }

                .rules-table tbody tr:hover {
                    background: #f8f9fa;
                }

                .rules-table tbody tr.default-rule {
                    background: #fff9e6;
                }

                .rules-table tbody tr.default-rule:hover {
                    background: #fff5cc;
                }

                .description-cell {
                    max-width: 300px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }

                .text-center {
                    text-align: center;
                }

                .actions-cell {
                    display: flex;
                    gap: 5px;
                }

                .employee-count-badge {
                    display: inline-block;
                    background: #3498db;
                    color: white;
                    padding: 4px 12px;
                    border-radius: 12px;
                    font-weight: 600;
                    font-size: 14px;
                }

                .badge {
                    display: inline-block;
                    padding: 4px 10px;
                    border-radius: 4px;
                    font-size: 12px;
                    font-weight: 600;
                    text-transform: uppercase;
                }

                .badge-default {
                    background: #f39c12;
                    color: white;
                }

                .badge-success {
                    background: #27ae60;
                    color: white;
                }

                .badge-info {
                    background: #3498db;
                    color: white;
                }

                .badge-warning {
                    background: #e67e22;
                    color: white;
                }

                .badge-primary {
                    background: #9b59b6;
                    color: white;
                }

                .btn {
                    padding: 8px 16px;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-weight: 500;
                    transition: all 0.2s;
                }

                .btn:disabled {
                    opacity: 0.5;
                    cursor: not-allowed;
                }

                .btn-primary {
                    background: #3498db;
                    color: white;
                }

                .btn-primary:hover:not(:disabled) {
                    background: #2980b9;
                }

                .btn-sm {
                    padding: 6px 12px;
                    font-size: 13px;
                }

                .btn-info {
                    background: #17a2b8;
                    color: white;
                }

                .btn-danger {
                    background: #e74c3c;
                    color: white;
                }

                .btn-danger:hover:not(:disabled) {
                    background: #c0392b;
                }

                .btn-secondary {
                    background: #95a5a6;
                    color: white;
                }

                .rules-summary {
                    padding: 15px;
                    background: #f8f9fa;
                    border-top: 1px solid #ecf0f1;
                    text-align: center;
                    color: #7f8c8d;
                }

                .empty-state, .error-state, .loading {
                    text-align: center;
                    padding: 60px 20px;
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                }

                .empty-state-icon, .error-icon {
                    font-size: 64px;
                    margin-bottom: 20px;
                }

                .loading-spinner {
                    border: 4px solid #f3f3f3;
                    border-top: 4px solid #3498db;
                    border-radius: 50%;
                    width: 50px;
                    height: 50px;
                    animation: spin 1s linear infinite;
                    margin: 0 auto 20px;
                }

                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            `}</style>
        </div>
    );
}

export default LoginRules;
