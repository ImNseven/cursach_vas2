package com.legal.analysis.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PrecedentRequest(
        @NotBlank(message = "Case number is required")
        @Size(max = 100)
        String caseNumber,

        @NotBlank(message = "Title is required")
        @Size(max = 500)
        String title,

        @NotBlank(message = "Content is required")
        String content,

        String summary,

        LocalDate decisionDate,

        @Size(max = 300)
        String courtName,

        @Size(max = 100)
        String decision,

        Long categoryId,

        List<Long> tagIds
) {}
