package com.company.employee.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public final class DatabaseConfig {
    private static HikariDataSource ds;

    private DatabaseConfig() {
    }

    public static synchronized DataSource dataSource() {
        if (ds == null) {
            HikariConfig c = new HikariConfig();
            c.setDriverClassName(EnvironmentConfig.get("db.driver"));
            c.setJdbcUrl(EnvironmentConfig.get("db.url"));
            c.setUsername(EnvironmentConfig.get("db.username"));
            c.setPassword(EnvironmentConfig.get("db.password"));
            c.setMinimumIdle(EnvironmentConfig.getInt("db.pool.minimumIdle"));
            c.setMaximumPoolSize(EnvironmentConfig.getInt("db.pool.maximumPoolSize"));
            c.setConnectionTimeout(EnvironmentConfig.getInt("db.pool.connectionTimeoutMs"));
            c.setPoolName("EmployeeOraclePool");
            c.setAutoCommit(true);
            ds = new HikariDataSource(c);
        }
        return ds;
    }

    public static synchronized void close() {
        if (ds != null) {
            ds.close();
            ds = null;
        }
    }
}
