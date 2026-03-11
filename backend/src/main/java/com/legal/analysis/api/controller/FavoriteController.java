package com.legal.analysis.api.controller;

import com.legal.analysis.application.dto.response.FavoriteResponse;
import com.legal.analysis.application.dto.response.PageResponse;
import com.legal.analysis.application.service.FavoriteService;
import com.legal.analysis.infrastructure.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{precedentId}")
    public ResponseEntity<FavoriteResponse> addFavorite(
            @PathVariable Long precedentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteService.addFavorite(userDetails.getId(), precedentId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<FavoriteResponse>> getUserFavorites(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                PageResponse.from(favoriteService.getUserFavorites(userDetails.getId(), pageable))
        );
    }

    @DeleteMapping("/{precedentId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long precedentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        favoriteService.removeFavorite(userDetails.getId(), precedentId);
        return ResponseEntity.noContent().build();
    }
}
