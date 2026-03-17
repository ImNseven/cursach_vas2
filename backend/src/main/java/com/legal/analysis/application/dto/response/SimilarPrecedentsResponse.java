package com.legal.analysis.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SimilarPrecedentsResponse(
        Long precedentId,
        String precedentTitle,
        Integer totalMatches,
        List<PrecedentMatchResponse> matches,
        LocalDateTime analyzedAt
) {}
