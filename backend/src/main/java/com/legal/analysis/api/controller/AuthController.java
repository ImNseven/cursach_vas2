package com.legal.analysis.api.controller;

import com.legal.analysis.application.dto.request.GitHubCallbackRequest;
import com.legal.analysis.application.dto.request.LoginRequest;
import com.legal.analysis.application.dto.request.RefreshTokenRequest;
import com.legal.analysis.application.dto.request.RegisterRequest;
import com.legal.analysis.application.dto.response.AuthResponse;
import com.legal.analysis.application.dto.response.UserResponse;
import com.legal.analysis.application.service.AuthService;
import com.legal.analysis.application.service.UserService;
import com.legal.analysis.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/github")
    public ResponseEntity<AuthResponse> loginWithGitHub(@Valid @RequestBody GitHubCallbackRequest request) {
        return ResponseEntity.ok(authService.loginWithGitHub(request.code()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        authService.logout(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }
}
