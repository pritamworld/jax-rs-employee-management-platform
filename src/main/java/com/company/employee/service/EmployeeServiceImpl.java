package com.company.employee.service;

import com.company.employee.exception.EmployeeNotFoundException;
import com.company.employee.model.Employee;
import com.company.employee.repository.EmployeeRepository;
import jakarta.inject.Inject;
import java.util.List;

public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository repo;

    @Inject
    public EmployeeServiceImpl(EmployeeRepository repo) {
        this.repo = repo;
    }

    @Override
    public Employee create(Employee e) {
        validate(e);
        if (repo.emailExists(e.getEmail(), null))
            throw new IllegalStateException("Employee email already exists");
        if (e.getStatus() == null || e.getStatus().isBlank())
            e.setStatus("ACTIVE");
        return repo.create(e);
    }

    @Override
    public Employee get(long id) {
        return repo.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + id));
    }

    @Override
    public List<Employee> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("page must be >= 0 and size must be 1..100");
        return repo.findAll(Math.multiplyExact(page, size), size);
    }

    @Override
    public Employee update(long id, Employee e) {
        get(id);
        validate(e);
        if (repo.emailExists(e.getEmail(), id))
            throw new IllegalStateException("Employee email already exists");
        Employee updated = repo.update(id, e);
        if (updated == null)
            throw new EmployeeNotFoundException("Employee not found: " + id);
        return updated;
    }

    @Override
    public void delete(long id) {
        if (!repo.delete(id))
            throw new EmployeeNotFoundException("Employee not found: " + id);
    }

    private void validate(Employee e) {
        if (e == null)
            throw new IllegalArgumentException("Request body is required");
        if (blank(e.getEmployeeNumber()) || blank(e.getFirstName()) || blank(e.getLastName()) || blank(e.getEmail()))
            throw new IllegalArgumentException("employeeNumber, firstName, lastName and email are required");
        if (!e.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("email format is invalid");
        if (e.getSalary() == null || e.getSalary().signum() < 0)
            throw new IllegalArgumentException("salary must be zero or greater");
        if (e.getStatus() != null && !List.of("ACTIVE", "INACTIVE").contains(e.getStatus().toUpperCase()))
            throw new IllegalArgumentException("status must be ACTIVE or INACTIVE");
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
