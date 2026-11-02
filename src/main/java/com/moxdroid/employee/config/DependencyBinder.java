package com.moxdroid.employee.config;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.moxdroid.employee.auth.*;
import com.moxdroid.employee.repository.*;
import com.moxdroid.employee.service.*;

import javax.sql.DataSource;

public class DependencyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(DatabaseConfig.dataSource()).to(DataSource.class);
        bind(EmployeeRepositoryJdbcImpl.class).to(EmployeeRepository.class);
        bind(EmployeeServiceImpl.class).to(EmployeeService.class);
        bind(AuthRepositoryJdbc.class).to(AuthRepository.class);
        bind(AuthService.class).to(AuthService.class);
        bind(JwtService.class).to(JwtService.class);
    }
}
