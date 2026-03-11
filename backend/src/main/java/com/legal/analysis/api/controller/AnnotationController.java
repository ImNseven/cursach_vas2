package com.legal.analysis.api.controller;

import com.legal.analysis.application.dto.request.AnnotationRequest;
import com.legal.analysis.application.dto.response.AnnotationResponse;
import com.legal.analysis.application.service.AnnotationService;
import com.legal.analysis.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/annotations")
@RequiredArgsConstructor
public class AnnotationController {

    private final AnnotationService annotationService;

    @PostMapping
    public ResponseEntity<AnnotationResponse> createAnnotation(
            @Valid @RequestBody AnnotationRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(annotationService.createAnnotation(request, userDetails.getId()));
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<AnnotationResponse>> getDocumentAnnotations(@PathVariable Long documentId) {
        return ResponseEntity.ok(annotationService.getDocumentAnnotations(documentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnotation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        annotationService.deleteAnnotation(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
