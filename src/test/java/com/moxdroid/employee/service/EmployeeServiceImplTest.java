package com.moxdroid.employee.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moxdroid.employee.exception.EmployeeNotFoundException;
import com.moxdroid.employee.model.Employee;
import com.moxdroid.employee.repository.EmployeeRepository;
import com.moxdroid.employee.service.EmployeeServiceImpl;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    @Mock EmployeeRepository repo;
    @InjectMocks EmployeeServiceImpl service;

    @Test void getsEmployee() {
        Employee e=new Employee(); e.setEmployeeId(7L); e.setFirstName("Ava");
        when(repo.findById(7L)).thenReturn(Optional.of(e));
        assertEquals("Ava",service.get(7L).getFirstName());
    }
    @Test void missingEmployeeThrows() {
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class,()->service.get(999L));
    }
    @Test void rejectsInvalidPagination() {
        assertThrows(IllegalArgumentException.class,()->service.list(0,101));
    }
}
