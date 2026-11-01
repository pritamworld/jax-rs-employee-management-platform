package com.moxdroid.employee.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RoleSecurityFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String path = ctx.getUriInfo().getPath();
        if (!path.startsWith("employees"))
            return;
        String method = ctx.getMethod();
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")) {
            Object role = ctx.getProperty("auth.role");
            if (!"ADMIN".equals(role)) {
                ctx.abortWith(Response.status(403)
                        .entity(java.util.Map.of("status", 403, "error", "ADMIN role required")).build());
            }
        }
    }
}
