package com.legal.analysis.service;

import com.legal.analysis.application.dto.request.LoginRequest;
import com.legal.analysis.application.dto.request.RegisterRequest;
import com.legal.analysis.application.dto.response.AuthResponse;
import com.legal.analysis.application.service.AuthService;
import com.legal.analysis.domain.model.Role;
import com.legal.analysis.domain.model.User;
import com.legal.analysis.domain.repository.RoleRepository;
import com.legal.analysis.domain.repository.UserRepository;
import com.legal.analysis.infrastructure.exception.DuplicateResourceException;
import com.legal.analysis.infrastructure.security.JwtService;
import com.legal.analysis.infrastructure.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Role userRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").description("Regular user").build();
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashed_password")
                .fullName("Test User")
                .role(userRole)
                .build();
    }

    @Test
    void register_shouldReturnAuthResponse_whenValidRequest() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh_token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access_token");
        assertThat(response.refreshToken()).isEqualTo("refresh_token");
        assertThat(response.user().email()).isEqualTo("test@example.com");

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "Test User");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void login_shouldReturnAuthResponse_whenValidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh_token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access_token");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void logout_shouldClearRefreshToken() {
        testUser.setRefreshToken("some_refresh_token");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        authService.logout(1L);

        verify(userRepository).save(argThat(user -> user.getRefreshToken() == null));
    }
}
