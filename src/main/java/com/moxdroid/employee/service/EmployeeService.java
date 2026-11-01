package com.moxdroid.employee.service;

import java.util.List;

import com.moxdroid.employee.model.Employee;

public interface EmployeeService {
    Employee create(Employee e);

    Employee get(long id);

    List<Employee> list(int page, int size);

    Employee update(long id, Employee e);

    void delete(long id);
}
