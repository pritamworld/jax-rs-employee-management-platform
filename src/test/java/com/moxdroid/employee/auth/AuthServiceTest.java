package com.moxdroid.employee.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.moxdroid.employee.exception.AuthenticationFailedException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String PASSWORD = "Example-Long-Password-123!";

    @Mock AuthRepository repo;
    @Mock JwtService jwt;
    @InjectMocks AuthService service;

    private AuthRepository.User user(boolean enabled) {
        return new AuthRepository.User(1L, "jane.doe", "jane@example.com", BCrypt.hashpw(PASSWORD, BCrypt.gensalt(4)),
                "USER", enabled);
    }

    @Test void registerCreatesUserRoleWithHashedPasswordAndReturnsToken() {
        when(repo.create(eq("jane.doe"), eq("jane@example.com"), anyString(), eq("USER")))
                .thenAnswer(i -> new AuthRepository.User(5L, i.getArgument(0), i.getArgument(1), i.getArgument(2),
                        i.getArgument(3), true));
        when(jwt.issue(any())).thenReturn("token");

        Map<String, Object> out = service.register("jane.doe", "jane@example.com", PASSWORD);

        assertEquals("token", out.get("accessToken"));
        assertEquals("Bearer", out.get("tokenType"));
        assertEquals("USER", out.get("role"));
        assertEquals("jane.doe", out.get("username"));
        var hash = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(repo).create(eq("jane.doe"), eq("jane@example.com"), hash.capture(), eq("USER"));
        assertNotEquals(PASSWORD, hash.getValue());
        assertTrue(BCrypt.checkpw(PASSWORD, hash.getValue()));
    }

    @Test void registerRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> service.register("a", "jane@example.com", PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "jane@example.com", PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> service.register("jane.doe", "not-an-email", PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> service.register("jane.doe", "jane@example.com", "short"));
        assertThrows(IllegalArgumentException.class, () -> service.register("jane.doe", "jane@example.com", null));
        verifyNoInteractions(repo);
    }

    @Test void registerPropagatesDuplicateAccountConflict() {
        when(repo.create(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("Username or email already exists"));
        assertThrows(IllegalStateException.class, () -> service.register("jane.doe", "jane@example.com", PASSWORD));
    }

    @Test void loginReturnsTokenForValidCredentials() {
        when(repo.findByUsername("jane.doe")).thenReturn(Optional.of(user(true)));
        when(jwt.issue(any())).thenReturn("token");
        Map<String, Object> out = service.login("jane.doe", PASSWORD);
        assertEquals("token", out.get("accessToken"));
        assertEquals("USER", out.get("role"));
    }

    @Test void loginFailsWithUnauthorizedForWrongPassword() {
        when(repo.findByUsername("jane.doe")).thenReturn(Optional.of(user(true)));
        assertThrows(AuthenticationFailedException.class, () -> service.login("jane.doe", "wrong-password-123"));
        verifyNoInteractions(jwt);
    }

    @Test void loginFailsForUnknownUser() {
        when(repo.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(AuthenticationFailedException.class, () -> service.login("nobody", PASSWORD));
    }

    @Test void loginFailsForDisabledUser() {
        when(repo.findByUsername("jane.doe")).thenReturn(Optional.of(user(false)));
        assertThrows(AuthenticationFailedException.class, () -> service.login("jane.doe", PASSWORD));
    }

    @Test void loginRejectsMissingFields() {
        assertThrows(IllegalArgumentException.class, () -> service.login(null, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> service.login("jane.doe", null));
        verifyNoInteractions(repo);
    }
}
