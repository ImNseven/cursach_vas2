package com.legal.analysis.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record PrecedentResponse(
        Long id,
        String caseNumber,
        String title,
        String content,
        String summary,
        LocalDate decisionDate,
        String courtName,
        String decision,
        CategoryResponse category,
        Set<TagResponse> tags,
        LocalDateTime createdAt
) {}
