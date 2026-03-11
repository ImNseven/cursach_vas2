package com.legal.analysis.api.controller;

import com.legal.analysis.application.dto.response.PageResponse;
import com.legal.analysis.application.dto.response.SearchHistoryResponse;
import com.legal.analysis.application.dto.response.SearchResultResponse;
import com.legal.analysis.application.service.SearchService;
import com.legal.analysis.infrastructure.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/analyze/{documentId}")
    public ResponseEntity<SearchResultResponse> analyzeDocument(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(searchService.analyzeDocument(documentId, userDetails.getId()));
    }

    @GetMapping("/results/{documentId}")
    public ResponseEntity<SearchResultResponse> getDocumentResults(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(searchService.getDocumentResults(documentId, userDetails.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<PageResponse<SearchHistoryResponse>> getSearchHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                PageResponse.from(searchService.getSearchHistory(userDetails.getId(), pageable))
        );
    }
}
