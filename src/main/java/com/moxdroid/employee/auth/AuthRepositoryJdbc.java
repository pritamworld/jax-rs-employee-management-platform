package com.moxdroid.employee.auth;

import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class AuthRepositoryJdbc implements AuthRepository {
    private final DataSource ds;

    @Inject
    public AuthRepositoryJdbc(DataSource ds) {
        this.ds = ds;
    }

    public Optional<User> findByUsername(String username) {
        try (Connection c = ds.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "SELECT user_id,username,email,password_hash,role,enabled FROM app_users WHERE LOWER(username)=LOWER(?)")) {
            p.setString(1, username);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next())
                    return Optional.empty();
                return Optional.of(new User(r.getLong(1), r.getString(2), r.getString(3), r.getString(4),
                        r.getString(5), "Y".equals(r.getString(6))));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Authentication database operation failed", e);
        }
    }

    public User create(String username, String email, String passwordHash, String role) {
        try (Connection c = ds.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "INSERT INTO app_users(username,email,password_hash,role,enabled) VALUES(?,?,?,?, 'Y')",
                        new String[] { "USER_ID" })) {
            p.setString(1, username);
            p.setString(2, email);
            p.setString(3, passwordHash);
            p.setString(4, role);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next())
                    return new User(r.getLong(1), username, email, passwordHash, role, true);
            }
            throw new SQLException("No generated user id");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1)
                throw new IllegalStateException("Username or email already exists");
            throw new RuntimeException("Unable to create account", e);
        }
    }
}
