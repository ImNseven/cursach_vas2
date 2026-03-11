package com.legal.analysis.application.dto.response;

import java.time.LocalDateTime;

public record SearchHistoryResponse(
        Long id,
        Long documentId,
        String documentTitle,
        Integer resultsCount,
        LocalDateTime searchedAt
) {}
