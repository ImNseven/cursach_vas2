package com.legal.analysis.application.dto.response;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Long parentId
) {}
