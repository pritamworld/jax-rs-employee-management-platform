package com.company.employee.auth;

import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Map;

public class AuthService {
    private final AuthRepository repo;
    private final JwtService jwt;

    @Inject
    public AuthService(AuthRepository repo, JwtService jwt) {
        this.repo = repo;
        this.jwt = jwt;
    }

    public Map<String, Object> register(String username, String email, String password) {
        if (username == null || !username.matches("^[A-Za-z0-9._-]{3,50}$"))
            throw new IllegalArgumentException("username must be 3-50 letters, digits, dot, underscore or hyphen");
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("valid email is required");
        if (password == null || password.length() < 12)
            throw new IllegalArgumentException("password must be at least 12 characters");
        var u = repo.create(username, email, BCrypt.hashpw(password, BCrypt.gensalt(12)), "USER");
        return tokenResponse(u);
    }

    public Map<String, Object> login(String username, String password) {
        var u = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!u.enabled() || password == null || !BCrypt.checkpw(password, u.passwordHash()))
            throw new IllegalArgumentException("Invalid username or password");
        return tokenResponse(u);
    }

    private Map<String, Object> tokenResponse(AuthRepository.User u) {
        return Map.of("accessToken", jwt.issue(u), "tokenType", "Bearer", "expiresIn",
                Integer.parseInt(com.company.employee.config.EnvironmentConfig.getOrDefault("jwt.ttlMinutes", "30"))
                        * 60,
                "role", u.role(), "username", u.username());
    }
}
