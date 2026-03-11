package com.legal.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legal.analysis.api.controller.AuthController;
import com.legal.analysis.application.dto.request.LoginRequest;
import com.legal.analysis.application.dto.request.RegisterRequest;
import com.legal.analysis.application.dto.response.AuthResponse;
import com.legal.analysis.application.dto.response.UserResponse;
import com.legal.analysis.application.service.AuthService;
import com.legal.analysis.application.service.UserService;
import com.legal.analysis.infrastructure.exception.DuplicateResourceException;
import com.legal.analysis.infrastructure.security.JwtService;
import com.legal.analysis.infrastructure.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private final UserResponse mockUser = new UserResponse(
            1L, "test@example.com", "Test User", null, "USER", LocalDateTime.now()
    );

    private final AuthResponse mockAuthResponse = new AuthResponse(
            "access_token", "refresh_token", mockUser
    );

    @Test
    void register_shouldReturn201_whenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User");
        when(authService.register(any())).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void register_shouldReturn400_whenInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "password123", "Test User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User");
        when(authService.register(any())).thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturn200_whenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(authService.login(any())).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"));
    }

    @Test
    void login_shouldReturn400_whenEmptyPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
