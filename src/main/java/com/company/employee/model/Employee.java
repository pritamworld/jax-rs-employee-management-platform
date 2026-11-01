package com.company.employee.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    private Long employeeId;
    private String employeeNumber, firstName, lastName, email, department, status;
    private BigDecimal salary;
    private LocalDate hireDate;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long v) {
        employeeId = v;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String v) {
        employeeNumber = v;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String v) {
        firstName = v;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String v) {
        lastName = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String v) {
        department = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal v) {
        salary = v;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate v) {
        hireDate = v;
    }
}
