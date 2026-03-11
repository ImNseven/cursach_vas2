package com.legal.analysis.application.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now(), null);
    }
}
