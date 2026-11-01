package com.company.employee.repository;

import com.company.employee.model.Employee;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class EmployeeRepositoryJdbc implements EmployeeRepository {
    private final DataSource ds;
    @Inject public EmployeeRepositoryJdbc(DataSource ds) { this.ds = ds; }

    @Override public Employee create(Employee e) {
        String sql = "INSERT INTO employees(employee_number,first_name,last_name,email,department,salary,status) VALUES(?,?,?,?,?,?,?)";
        try (Connection c=ds.getConnection(); PreparedStatement p=c.prepareStatement(sql, new String[]{"EMPLOYEE_ID"})) {
            bind(p,e); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()) { if(r.next()) e.setEmployeeId(r.getLong(1)); }
            return findById(e.getEmployeeId()).orElseThrow();
        } catch(SQLException x) { throw translate(x); }
    }
    @Override public Optional<Employee> findById(long id) {
        try(Connection c=ds.getConnection(); PreparedStatement p=c.prepareStatement("SELECT * FROM employees WHERE employee_id=?")) {
            p.setLong(1,id); try(ResultSet r=p.executeQuery()) { return r.next()?Optional.of(map(r)):Optional.empty(); }
        } catch(SQLException x) { throw translate(x); }
    }
    @Override public List<Employee> findAll(int offset, int limit) {
        String sql = "SELECT * FROM (SELECT e.*, ROW_NUMBER() OVER (ORDER BY employee_id) rn FROM employees e) WHERE rn > ? AND rn <= ? ORDER BY rn";
        try(Connection c=ds.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,offset); p.setInt(2,offset+limit);
            try(ResultSet r=p.executeQuery()) { List<Employee> out=new ArrayList<>(); while(r.next()) out.add(map(r)); return out; }
        } catch(SQLException x) { throw translate(x); }
    }
    @Override public Employee update(long id, Employee e) {
        String sql="UPDATE employees SET employee_number=?,first_name=?,last_name=?,email=?,department=?,salary=?,status=?,updated_at=CURRENT_TIMESTAMP WHERE employee_id=?";
        try(Connection c=ds.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            bind(p,e); p.setLong(8,id);
            if(p.executeUpdate()==0) return null;
            return findById(id).orElse(null);
        } catch(SQLException x) { throw translate(x); }
    }
    @Override public boolean delete(long id) {
        try(Connection c=ds.getConnection(); PreparedStatement p=c.prepareStatement("DELETE FROM employees WHERE employee_id=?")) {
            p.setLong(1,id); return p.executeUpdate()==1;
        } catch(SQLException x) { throw translate(x); }
    }
    @Override public boolean emailExists(String email, Long excludingId) {
        String sql = excludingId == null ? "SELECT 1 FROM employees WHERE LOWER(email)=LOWER(?)" :
            "SELECT 1 FROM employees WHERE LOWER(email)=LOWER(?) AND employee_id<>?";
        try(Connection c=ds.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,email); if(excludingId!=null) p.setLong(2,excludingId);
            try(ResultSet r=p.executeQuery()) { return r.next(); }
        } catch(SQLException x) { throw translate(x); }
    }
    private void bind(PreparedStatement p, Employee e) throws SQLException {
        p.setString(1,e.getEmployeeNumber()); p.setString(2,e.getFirstName()); p.setString(3,e.getLastName());
        p.setString(4,e.getEmail()); p.setString(5,e.getDepartment()); p.setBigDecimal(6,e.getSalary());
        p.setString(7, e.getStatus()==null || e.getStatus().isBlank() ? "ACTIVE" : e.getStatus());
    }
    private Employee map(ResultSet r) throws SQLException {
        Employee e=new Employee(); e.setEmployeeId(r.getLong("employee_id")); e.setEmployeeNumber(r.getString("employee_number"));
        e.setFirstName(r.getString("first_name")); e.setLastName(r.getString("last_name")); e.setEmail(r.getString("email"));
        e.setDepartment(r.getString("department")); e.setSalary(r.getBigDecimal("salary")); e.setStatus(r.getString("status"));
        Date d=r.getDate("hire_date"); if(d!=null)e.setHireDate(d.toLocalDate()); return e;
    }
    private RuntimeException translate(SQLException x) {
        if (x.getErrorCode()==1 || "23505".equals(x.getSQLState())) return new IllegalStateException("A unique employee field already exists");
        return new RuntimeException("Database operation failed",x);
    }
}
