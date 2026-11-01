package com.moxdroid.employee.exception;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.moxdroid.employee.dto.ErrorResponse;

import java.time.Instant;
import java.sql.SQLException;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionMapper.class);
    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        int status = 500;
        String error = "INTERNAL_SERVER_ERROR";
        String message = "An unexpected error occurred";
        if (ex instanceof EmployeeNotFoundException) {
            status = 404;
            error = "NOT_FOUND";
            message = ex.getMessage();
        } else if (ex instanceof IllegalArgumentException || ex instanceof ConstraintViolationException) {
            status = 400;
            error = "BAD_REQUEST";
            message = ex instanceof IllegalArgumentException ? ex.getMessage() : "Request validation failed";
        } else if (ex instanceof IllegalStateException) {
            status = 409;
            error = "CONFLICT";
            message = ex.getMessage();
        } else {
            LOG.error("Unhandled request failure", ex);
        }
        
        if (ex instanceof SQLException) {
            status = 500;
            error = "DATABASE_ERROR";
            message = "Database operation failed";
        }
        var body = new ErrorResponse(Instant.now(), status, error, message, uriInfo == null ? "" : uriInfo.getPath());
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}
