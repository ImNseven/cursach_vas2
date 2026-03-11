package com.legal.analysis.api.controller;

import com.legal.analysis.application.dto.request.UpdateProfileRequest;
import com.legal.analysis.application.dto.response.UserResponse;
import com.legal.analysis.application.service.UserService;
import com.legal.analysis.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getId(), request));
    }
}
