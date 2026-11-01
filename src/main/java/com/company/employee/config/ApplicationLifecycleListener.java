package com.company.employee.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApplicationLifecycleListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent e) {
        DatabaseConfig.dataSource();
    }
    @Override public void contextDestroyed(ServletContextEvent e) {
        DatabaseConfig.close();
    }
}
