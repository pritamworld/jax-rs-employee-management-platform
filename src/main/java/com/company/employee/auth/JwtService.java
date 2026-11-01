package com.company.employee.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.company.employee.config.EnvironmentConfig;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Date;

public class JwtService {
    private final Algorithm algorithm;
    private final String issuer;
    private final int ttl;

    @Inject
    public JwtService() {
        String secret = EnvironmentConfig.get("jwt.secret");
        if (secret.length() < 32)
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        algorithm = Algorithm.HMAC256(secret);
        issuer = EnvironmentConfig.getOrDefault("jwt.issuer", "employee-management");
        ttl = Integer.parseInt(EnvironmentConfig.getOrDefault("jwt.ttlMinutes", "30"));
    }

    public String issue(AuthRepository.User user) {
        Instant now = Instant.now();
        return JWT.create().withIssuer(issuer).withSubject(user.username()).withClaim("uid", user.id())
                .withClaim("role", user.role()).withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttl * 60L))).sign(algorithm);
    }

    public DecodedJWT verify(String token) {
        return JWT.require(algorithm).withIssuer(issuer).build().verify(token);
    }
}
