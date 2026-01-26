package com.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListResponse {

    private List<EmployeeResponse> employees;
    private int totalCount;

    public static EmployeeListResponse from(List<EmployeeResponse> employees) {
        return new EmployeeListResponse(employees, employees.size());
    }
}
