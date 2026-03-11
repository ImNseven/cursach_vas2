package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.request.LoginRequest;
import com.legal.analysis.application.dto.request.RefreshTokenRequest;
import com.legal.analysis.application.dto.request.RegisterRequest;
import com.legal.analysis.application.dto.response.AuthResponse;
import com.legal.analysis.application.dto.response.UserResponse;
import com.legal.analysis.domain.model.Role;
import com.legal.analysis.domain.model.User;
import com.legal.analysis.domain.repository.RoleRepository;
import com.legal.analysis.domain.repository.UserRepository;
import com.legal.analysis.infrastructure.exception.AuthException;
import com.legal.analysis.infrastructure.exception.DuplicateResourceException;
import com.legal.analysis.infrastructure.security.JwtService;
import com.legal.analysis.infrastructure.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User with email " + request.email() + " already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(userRole)
                .build();

        User saved = userRepository.save(user);

        UserDetailsImpl userDetails = new UserDetailsImpl(saved);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        saved.setRefreshToken(refreshToken);
        userRepository.save(saved);

        log.info("New user registered: {}", saved.getEmail());

        return new AuthResponse(accessToken, refreshToken, mapToUserResponse(saved));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException("User not found"));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken, mapToUserResponse(user));
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        User user = userRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new AuthResponse(newAccessToken, newRefreshToken, mapToUserResponse(user));
    }

    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getRole().getName(),
                user.getCreatedAt()
        );
    }
}
