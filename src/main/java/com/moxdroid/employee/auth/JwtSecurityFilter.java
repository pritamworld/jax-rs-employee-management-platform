package com.moxdroid.employee.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ContainerRequest;
import java.io.IOException;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtSecurityFilter implements ContainerRequestFilter {
    private final JwtService jwt;

    @Inject
    public JwtSecurityFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String path = ctx.getUriInfo().getPath();
        if (path.equals("auth/login") || path.equals("auth/register") || path.equals("auth/health")
                || path.equals("health"))
            return;
        String h = ctx.getHeaderString("Authorization");
        if (h == null || !h.startsWith("Bearer ")) {
            abort(ctx, 401, "Bearer token required");
            return;
        }
        try {
            var decoded = jwt.verify(h.substring(7));
            String role = decoded.getClaim("role").asString();
            String user = decoded.getSubject();
            ctx.setProperty("auth.user", user);
            ctx.setProperty("auth.role", role);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            abort(ctx, 401, "Invalid or expired token");
        }
    }

    private void abort(ContainerRequestContext ctx, int status, String message) {
        ctx.abortWith(Response.status(status).type("application/json")
                .entity(java.util.Map.of("status", status, "error", message)).build());
    }
}
