package com.legal.analysis.application.dto.response;

import java.time.LocalDate;
import java.util.Set;

public record PrecedentMatchResponse(
        Long precedentId,
        String caseNumber,
        String title,
        String summary,
        String courtName,
        LocalDate decisionDate,
        String decision,
        Float similarityScore,
        CategoryResponse category,
        Set<TagResponse> tags,
        boolean isFavorite
) {}
