package com.company.employee.config;

import com.company.employee.repository.*;
import com.company.employee.service.*;
import com.company.employee.auth.*;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import javax.sql.DataSource;

public class DependencyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(DatabaseConfig.dataSource()).to(DataSource.class);
        bind(EmployeeRepositoryJdbc.class).to(EmployeeRepository.class);
        bind(EmployeeServiceImpl.class).to(EmployeeService.class);
        bind(AuthRepositoryJdbc.class).to(AuthRepository.class);
        bind(AuthRepositoryJdbc.class).to(AuthRepository.class);
        bind(AuthService.class).to(AuthService.class);
        bind(JwtService.class).to(JwtService.class);
    }
}
