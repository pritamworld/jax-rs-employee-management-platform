package com.company.employee.service;
import com.company.employee.model.Employee;
import java.util.List;
public interface EmployeeService {
    Employee create(Employee e); Employee get(long id); List<Employee> list(int page,int size);
    Employee update(long id, Employee e); void delete(long id);
}
