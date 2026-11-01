package com.moxdroid.employee.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {
    @GET
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "employee-management");
    }

    @GET
    @Path("/ready")
    public Map<String, String> ready() {
        return Map.of("status", "UP", "service", "employee-management");
    }
}
