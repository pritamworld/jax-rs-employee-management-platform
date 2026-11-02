package com.moxdroid.employee.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GlobalExceptionMapperTest {
    private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

    @Test void authenticationFailureMapsTo401() {
        assertEquals(401, mapper.toResponse(new AuthenticationFailedException("Invalid username or password")).getStatus());
    }

    @Test void validationMapsTo400() {
        assertEquals(400, mapper.toResponse(new IllegalArgumentException("bad")).getStatus());
    }

    @Test void duplicateMapsTo409() {
        assertEquals(409, mapper.toResponse(new IllegalStateException("exists")).getStatus());
    }
}
