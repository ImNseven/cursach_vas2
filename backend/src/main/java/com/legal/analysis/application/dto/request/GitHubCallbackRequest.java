package com.legal.analysis.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GitHubCallbackRequest(
        @NotBlank(message = "Authorization code is required")
        String code
) {}
