package com.legal.analysis.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SearchResultResponse(
        Long documentId,
        String documentTitle,
        Integer totalMatches,
        List<PrecedentMatchResponse> matches,
        LocalDateTime analyzedAt
) {}
