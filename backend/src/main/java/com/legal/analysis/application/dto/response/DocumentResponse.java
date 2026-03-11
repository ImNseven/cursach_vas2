package com.legal.analysis.application.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record DocumentResponse(
        Long id,
        String title,
        String fileName,
        String fileType,
        Long fileSize,
        CategoryResponse category,
        Set<TagResponse> tags,
        LocalDateTime uploadedAt,
        Boolean isAnalyzed,
        Integer matchedPrecedentsCount
) {}
