package com.legal.analysis.api.controller;

import com.legal.analysis.application.dto.response.DocumentResponse;
import com.legal.analysis.application.dto.response.PageResponse;
import com.legal.analysis.application.service.DocumentService;
import com.legal.analysis.infrastructure.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(file, userDetails.getId()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<DocumentResponse>> getUserDocuments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                PageResponse.from(documentService.getUserDocuments(userDetails.getId(), pageable))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(documentService.getDocumentById(id, userDetails.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        documentService.deleteDocument(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
