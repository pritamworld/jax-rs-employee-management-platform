package com.company.employee.auth;

import java.util.Optional;

public interface AuthRepository {
    record User(long id, String username, String email, String passwordHash, String role, boolean enabled) {
    }

    Optional<User> findByUsername(String username);

    User create(String username, String email, String passwordHash, String role);
}
