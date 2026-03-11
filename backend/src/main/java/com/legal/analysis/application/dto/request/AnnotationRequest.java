package com.legal.analysis.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnnotationRequest(
        @NotNull(message = "Document ID is required")
        Long documentId,

        @NotBlank(message = "Content is required")
        String content,

        Integer startPosition,

        Integer endPosition
) {}
