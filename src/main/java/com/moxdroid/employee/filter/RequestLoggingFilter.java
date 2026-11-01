package com.moxdroid.employee.filter;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.UUID;

@Provider
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String KEY = "correlationId";

    @Override
    public void filter(ContainerRequestContext req) {
        String incoming = req.getHeaderString("X-Correlation-ID");
        String id = (incoming != null && incoming.matches("[A-Za-z0-9._-]{1,80}")) ? incoming
                : UUID.randomUUID().toString();
        req.setProperty(KEY, id);
        LOG.info("request_id={} method={} path={}", id, req.getMethod(), req.getUriInfo().getPath());
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        Object id = req.getProperty(KEY);
        if (id != null)
            res.getHeaders().putSingle("X-Correlation-ID", id);
        LOG.info("request_id={} status={}", id, res.getStatus());
    }
}
