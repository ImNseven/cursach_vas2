package com.legal.analysis.api.controller;

import com.legal.analysis.application.dto.request.PrecedentRequest;
import com.legal.analysis.application.dto.response.PageResponse;
import com.legal.analysis.application.dto.response.PrecedentResponse;
import com.legal.analysis.application.service.PrecedentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/precedents")
@RequiredArgsConstructor
public class PrecedentController {

    private final PrecedentService precedentService;

    @GetMapping
    public ResponseEntity<PageResponse<PrecedentResponse>> getAllPrecedents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (categoryId != null) {
            return ResponseEntity.ok(
                    PageResponse.from(precedentService.getPrecedentsByCategory(categoryId, pageable))
            );
        }

        return ResponseEntity.ok(PageResponse.from(precedentService.getAllPrecedents(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrecedentResponse> getPrecedentById(@PathVariable Long id) {
        return ResponseEntity.ok(precedentService.getPrecedentById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PrecedentResponse>> searchPrecedents(@RequestParam String query) {
        return ResponseEntity.ok(precedentService.searchPrecedents(query));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrecedentResponse> createPrecedent(@Valid @RequestBody PrecedentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(precedentService.createPrecedent(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrecedentResponse> updatePrecedent(
            @PathVariable Long id,
            @Valid @RequestBody PrecedentRequest request
    ) {
        return ResponseEntity.ok(precedentService.updatePrecedent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrecedent(@PathVariable Long id) {
        precedentService.deletePrecedent(id);
        return ResponseEntity.noContent().build();
    }
}
