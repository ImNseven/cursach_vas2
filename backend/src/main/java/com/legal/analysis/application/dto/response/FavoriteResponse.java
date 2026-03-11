package com.legal.analysis.application.dto.response;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long id,
        PrecedentResponse precedent,
        LocalDateTime addedAt
) {}
