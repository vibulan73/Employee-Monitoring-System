import React, { useState, useEffect } from 'react';
import { getAllEmployees, createEmployee, updateEmployee, deleteEmployee } from '../services/api';
import { getAllLoginRules } from '../services/loginRuleService';
import websocketService from '../services/websocket';
import EmployeeDetailsModal from './EmployeeDetailsModal';

function EmployeeManagement() {
    const [employees, setEmployees] = useState([]);
    const [loginRules, setLoginRules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterRole, setFilterRole] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [modalMode, setModalMode] = useState('add'); // 'add' or 'edit'
    const [selectedEmployee, setSelectedEmployee] = useState(null);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [employeeToDelete, setEmployeeToDelete] = useState(null);
    const [formData, setFormData] = useState({
        userId: '',
        password: '',
        firstName: '',
        lastName: '',
        jobRole: '',
        phoneNumber: '',
        loginRuleId: '',
        status: 'ACTIVE'
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // New state for details modal
    const [showDetailsModal, setShowDetailsModal] = useState(false);
    const [detailsEmployee, setDetailsEmployee] = useState(null);

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [itemsPerPage] = useState(10);

    useEffect(() => {
        fetchEmployees();
        fetchLoginRules();

        // Initialize WebSocket connection
        websocketService.connect(() => {
            console.log('EmployeeManagement: WebSocket connected');

            // Subscribe to employee updates
            websocketService.subscribe('/topic/employees', (event) => {
                console.log('Received employee event:', event);
                handleEmployeeEvent(event);
            });
        });

        // Cleanup on unmount
        return () => {
            websocketService.unsubscribe('/topic/employees');
        };
    }, []);

    const fetchEmployees = async () => {
        setLoading(true);
        try {
            const response = await getAllEmployees();
            setEmployees(response.data.employees || []);
        } catch (error) {
            console.error('Error fetching employees:', error);
            setError('Failed to load employees');
        } finally {
            setLoading(false);
        }
    };

    const fetchLoginRules = async () => {
        try {
            const response = await getAllLoginRules();
            setLoginRules(response.data || []);
        } catch (error) {
            console.error('Error fetching login rules:', error);
        }
    };

    const handleEmployeeEvent = (event) => {
        const { eventType, payload } = event;

        setEmployees(prevEmployees => {
            if (eventType === 'EMPLOYEE_CREATED') {
                // Prevent duplicate addition
                if (prevEmployees.some(emp => emp.id === payload.id)) {
                    console.log('Ignoring duplicate EMPLOYEE_CREATED event', payload.id);
                    return prevEmployees;
                }
                return [...prevEmployees, payload];
            } else if (eventType === 'EMPLOYEE_UPDATED') {
                return prevEmployees.map(emp =>
                    emp.id === payload.id ? payload : emp
                );
            } else if (eventType === 'EMPLOYEE_DELETED') {
                return prevEmployees.filter(emp => emp.id !== payload);
            }
            return prevEmployees;
        });
    };

    const handleAddEmployee = () => {
        setModalMode('add');
        setSelectedEmployee(null);
        const defaultRule = loginRules.find(r => r.isDefault);
        setFormData({
            userId: '',
            password: '',
            firstName: '',
            lastName: '',
            jobRole: '',
            phoneNumber: '',
            loginRuleId: defaultRule?.id || '',
            status: 'ACTIVE'
        });
        setError('');
        setSuccess('');
        setShowModal(true);
    };

    const handleEditEmployee = (employee) => {
        setModalMode('edit');
        setSelectedEmployee(employee);
        setFormData({
            userId: employee.userId,
            password: '',
            firstName: employee.firstName,
            lastName: employee.lastName,
            jobRole: employee.jobRole,
            phoneNumber: employee.phoneNumber,
            loginRuleId: employee.loginRuleId || '',
            status: employee.status || 'ACTIVE'
        });
        setError('');
        setSuccess('');
        setShowModal(true);
    };

    const handleDeleteClick = (employee) => {
        setEmployeeToDelete(employee);
        setShowDeleteConfirm(true);
    };

    const handleDeleteConfirm = async () => {
        try {
            await deleteEmployee(employeeToDelete.id);
            setSuccess('Employee deleted successfully');
            setShowDeleteConfirm(false);
            setEmployeeToDelete(null);
            setTimeout(() => setSuccess(''), 3000);
        } catch (error) {
            console.error('Error deleting employee:', error);
            setError('Failed to delete employee');
            setTimeout(() => setError(''), 3000);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const dataToSend = {
                ...formData,
                loginRuleId: formData.loginRuleId === '' ? null : formData.loginRuleId
            };

            if (modalMode === 'add') {
                await createEmployee(dataToSend);
                setSuccess('Employee added successfully');
            } else {
                const dataToSend = {
                    ...formData,
                    loginRuleId: formData.loginRuleId === '' ? null : formData.loginRuleId
                };
                await updateEmployee(selectedEmployee.id, dataToSend);
                setSuccess('Employee updated successfully');
            }
            setShowModal(false);
            setTimeout(() => setSuccess(''), 3000);
        } catch (error) {
            console.error('Error saving employee:', error);
            setError(error.response?.data?.message || 'Failed to save employee');
        }
    };

    const handleInputChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleRowClick = (employee) => {
        setDetailsEmployee(employee);
        setShowDetailsModal(true);
    };

    // Reset to first page when search/filter changes
    useEffect(() => {
        setCurrentPage(1);
    }, [searchQuery, filterRole]);

    // Filter employees based on search and role filter
    const filteredEmployees = employees.filter(emp => {
        const matchesSearch =
            emp.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            emp.lastName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            emp.userId.toLowerCase().includes(searchQuery.toLowerCase());
        const matchesRole = !filterRole || emp.jobRole === filterRole;
        return matchesSearch && matchesRole;
    }).sort((a, b) => a.id - b.id);

    // Pagination logic
    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentItems = filteredEmployees.slice(indexOfFirstItem, indexOfLastItem);
    const totalPages = Math.ceil(filteredEmployees.length / itemsPerPage);

    const paginate = (pageNumber) => setCurrentPage(pageNumber);

    // Get unique job roles for filter
    const jobRoles = [...new Set(employees.map(emp => emp.jobRole))];

    return (
        <div className="employee-management">
            <div className="employee-header">
                <div>
                    <h2>👥 Employee Management</h2>
                    <p>Manage your organization's employees</p>
                </div>
                <button className="btn-primary" onClick={handleAddEmployee}>
                    ➕ Add Employee
                </button>
            </div>

            {success && <div className="alert alert-success">{success}</div>}
            {error && <div className="alert alert-error">{error}</div>}

            <div className="employee-filters">
                <div className="search-box">
                    <input
                        type="text"
                        placeholder="🔍 Search by name or user ID..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
                <div className="filter-box">
                    <select
                        value={filterRole}
                        onChange={(e) => setFilterRole(e.target.value)}
                    >
                        <option value="">All Roles</option>
                        {jobRoles.map(role => (
                            <option key={role} value={role}>{role}</option>
                        ))}
                    </select>
                </div>
            </div>

            {loading ? (
                <div className="loading">
                    <div className="loading-spinner"></div>
                    <p>Loading employees...</p>
                </div>
            ) : filteredEmployees.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon">👥</div>
                    <h3>No Employees Found</h3>
                    <p>Add your first employee to get started.</p>
                </div>
            ) : (
                <div className="employee-table-container">
                    <table className="employee-table">
                        <thead>
                            <tr>
                                <th>S.No</th>
                                <th>User ID</th>
                                <th>Name</th>
                                <th>Job Role</th>
                                <th>Phone</th>
                                <th>Status</th>
                                <th>Login Rule</th>
                                <th>Created</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentItems.map((employee, index) => (
                                <tr
                                    key={employee.id}
                                    onClick={() => handleRowClick(employee)}
                                    style={{ cursor: 'pointer', transition: 'background-color 0.2s' }}
                                    className="employee-row"
                                >
                                    <td>{(currentPage - 1) * itemsPerPage + index + 1}</td>
                                    <td className="user-id">{employee.userId}</td>
                                    <td className="employee-name">
                                        {employee.firstName} {employee.lastName}
                                    </td>
                                    <td>
                                        <span className="role-badge">{employee.jobRole}</span>
                                    </td>
                                    <td>{employee.phoneNumber}</td>
                                    <td>
                                        <span
                                            style={{
                                                backgroundColor: employee.status === 'ACTIVE' ? '#d4edda' : '#f8d7da',
                                                color: employee.status === 'ACTIVE' ? '#155724' : '#721c24',
                                                padding: '5px 10px',
                                                borderRadius: '15px',
                                                fontSize: '12px',
                                                fontWeight: 'bold'
                                            }}
                                        >
                                            {employee.status || 'ACTIVE'}
                                        </span>
                                    </td>
                                    <td>
                                        {employee.loginRuleName ? (
                                            <span className="login-rule-badge" title={`Rule ID: ${employee.loginRuleId}`}>
                                                🔐 {employee.loginRuleName}
                                            </span>
                                        ) : (
                                            <span style={{ color: '#95a5a6', fontSize: '12px' }}>Not assigned</span>
                                        )}
                                    </td>
                                    <td>{new Date(employee.createdAt).toLocaleDateString()}</td>
                                    <td onClick={(e) => e.stopPropagation()}>
                                        <div className="action-buttons">
                                            <button
                                                className="btn-edit"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleEditEmployee(employee);
                                                }}
                                                title="Edit Employee"
                                            >
                                                ✏️
                                            </button>
                                            <button
                                                className="btn-delete"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleDeleteClick(employee);
                                                }}
                                                title="Delete Employee"
                                            >
                                                🗑️
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Pagination Controls */}
            {
                filteredEmployees.length > 0 && (
                    <div className="pagination">
                        <button
                            onClick={() => paginate(currentPage - 1)}
                            disabled={currentPage === 1}
                            className="pagination-btn"
                        >
                            Previous
                        </button>

                        <span className="pagination-info">
                            Page {currentPage} of {totalPages}
                        </span>

                        <button
                            onClick={() => paginate(currentPage + 1)}
                            disabled={currentPage === totalPages}
                            className="pagination-btn"
                        >
                            Next
                        </button>
                    </div>
                )
            }

            {/* Add/Edit Modal */}
            {
                showModal && (
                    <div className="modal-overlay" onClick={() => setShowModal(false)}>
                        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3>{modalMode === 'add' ? '➕ Add Employee' : '✏️ Edit Employee'}</h3>
                                <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
                            </div>
                            {error && <div className="alert alert-error">{error}</div>}
                            <form onSubmit={handleSubmit}>
                                <div className="form-group">
                                    <label>User ID *</label>
                                    <input
                                        type="text"
                                        name="userId"
                                        value={formData.userId}
                                        onChange={handleInputChange}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Password {modalMode === 'add' ? '*' : '(leave blank to keep current)'}</label>
                                    <input
                                        type="password"
                                        name="password"
                                        value={formData.password}
                                        onChange={handleInputChange}
                                        required={modalMode === 'add'}
                                    />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label>First Name *</label>
                                        <input
                                            type="text"
                                            name="firstName"
                                            value={formData.firstName}
                                            onChange={handleInputChange}
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label>Last Name *</label>
                                        <input
                                            type="text"
                                            name="lastName"
                                            value={formData.lastName}
                                            onChange={handleInputChange}
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label>Job Role *</label>
                                    <input
                                        type="text"
                                        name="jobRole"
                                        value={formData.jobRole}
                                        onChange={handleInputChange}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Phone Number *</label>
                                    <input
                                        type="tel"
                                        name="phoneNumber"
                                        value={formData.phoneNumber}
                                        onChange={handleInputChange}
                                        required
                                    />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label>Status</label>
                                        <select
                                            name="status"
                                            value={formData.status}
                                            onChange={handleInputChange}
                                        >
                                            <option value="ACTIVE">Active</option>
                                            <option value="INACTIVE">Inactive</option>
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label>Login Rule</label>
                                        <select
                                            name="loginRuleId"
                                            value={formData.loginRuleId}
                                            onChange={handleInputChange}
                                        >
                                            <option value="">Default (Unrestricted Access)</option>
                                            {loginRules.map(rule => (
                                                <option key={rule.id} value={rule.id}>
                                                    {rule.ruleName} ({rule.ruleType})
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                                <div className="modal-actions">
                                    <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>
                                        Cancel
                                    </button>
                                    <button type="submit" className="btn-primary">
                                        {modalMode === 'add' ? 'Add Employee' : 'Update Employee'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )
            }

            {/* Delete Confirmation Modal */}
            {
                showDeleteConfirm && (
                    <div className="modal-overlay" onClick={() => setShowDeleteConfirm(false)}>
                        <div className="modal-content modal-small" onClick={(e) => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3>⚠️ Confirm Delete</h3>
                                <button className="close-btn" onClick={() => setShowDeleteConfirm(false)}>✕</button>
                            </div>
                            <p>Are you sure you want to delete employee <strong>{employeeToDelete?.firstName} {employeeToDelete?.lastName}</strong>?</p>
                            <p className="warning-text">This action cannot be undone.</p>
                            <div className="modal-actions">
                                <button className="btn-secondary" onClick={() => setShowDeleteConfirm(false)}>
                                    Cancel
                                </button>
                                <button className="btn-danger" onClick={handleDeleteConfirm}>
                                    Delete
                                </button>
                            </div>
                        </div>
                    </div>
                )
            }

            {/* Employee Details Modal */}
            {showDetailsModal && (
                <EmployeeDetailsModal
                    employee={detailsEmployee}
                    onClose={() => setShowDetailsModal(false)}
                />
            )}
        </div >
    );
}

export default EmployeeManagement;
