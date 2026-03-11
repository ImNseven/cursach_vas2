package com.legal.analysis.application.dto.response;

import java.time.LocalDateTime;

public record AnnotationResponse(
        Long id,
        Long documentId,
        String content,
        Integer startPosition,
        Integer endPosition,
        LocalDateTime createdAt
) {}
