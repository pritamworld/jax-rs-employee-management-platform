package com.company.employee.repository;
import com.company.employee.model.Employee;
import java.util.List;
import java.util.Optional;
public interface EmployeeRepository {
    Employee create(Employee e);
    Optional<Employee> findById(long id);
    List<Employee> findAll(int offset, int limit);
    Employee update(long id, Employee e);
    boolean delete(long id);
    boolean emailExists(String email, Long excludingId);
}
