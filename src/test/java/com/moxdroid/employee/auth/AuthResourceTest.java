package com.moxdroid.employee.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.moxdroid.employee.exception.AuthenticationFailedException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthResourceTest {
    private final AuthService service = mock(AuthService.class);
    private final AuthResource resource = new AuthResource(service);

    @Test void registerReturns201() {
        when(service.register("jane.doe", "jane@example.com", "pw")).thenReturn(Map.of("accessToken", "t"));
        var req = new AuthResource.RegisterRequest();
        req.username = "jane.doe";
        req.email = "jane@example.com";
        req.password = "pw";
        Response r = resource.register(req);
        assertEquals(201, r.getStatus());
    }

    @Test void loginDelegatesToService() {
        when(service.login("jane.doe", "pw")).thenReturn(Map.of("accessToken", "t"));
        var req = new AuthResource.LoginRequest();
        req.username = "jane.doe";
        req.password = "pw";
        assertEquals("t", resource.login(req).get("accessToken"));
    }

    @Test void nullBodiesRejected() {
        assertThrows(IllegalArgumentException.class, () -> resource.register(null));
        assertThrows(IllegalArgumentException.class, () -> resource.login(null));
    }

    @Test void badCredentialsPropagate() {
        when(service.login(any(), any())).thenThrow(new AuthenticationFailedException("Invalid username or password"));
        var req = new AuthResource.LoginRequest();
        req.username = "x";
        req.password = "y";
        assertThrows(AuthenticationFailedException.class, () -> resource.login(req));
    }
}
