package com.moxdroid.employee.repository;

import java.util.List;
import java.util.Optional;

import com.moxdroid.employee.model.Employee;

public interface EmployeeRepository {
    Employee create(Employee e);

    Optional<Employee> findById(long id);

    List<Employee> findAll(int offset, int limit);

    Employee update(long id, Employee e);

    boolean delete(long id);

    boolean emailExists(String email, Long excludingId);
}
