package com.company.employee.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class EmployeeRequest {
    @NotBlank @Size(max=30) public String employeeNumber;
    @NotBlank @Size(max=100) public String firstName;
    @NotBlank @Size(max=100) public String lastName;
    @NotBlank @Email @Size(max=150) public String email;
    @Size(max=100) public String department;
    @NotNull @DecimalMin("0.00") public BigDecimal salary;
    public String status = "ACTIVE";
}
