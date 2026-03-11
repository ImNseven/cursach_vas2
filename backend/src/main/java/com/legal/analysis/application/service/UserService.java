package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.request.UpdateProfileRequest;
import com.legal.analysis.application.dto.response.UserResponse;
import com.legal.analysis.domain.model.User;
import com.legal.analysis.domain.repository.UserRepository;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Optional.ofNullable(request.fullName()).ifPresent(user::setFullName);

        return mapToResponse(userRepository.save(user));
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    private UserResponse mapToResponse(User user) {
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
