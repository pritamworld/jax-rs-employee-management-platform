package com.moxdroid.employee.config;

import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        packages("com.moxdroid.employee");
        register(new DependencyBinder());
    }
}
