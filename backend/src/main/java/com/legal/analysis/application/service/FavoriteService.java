package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.response.*;
import com.legal.analysis.domain.model.Favorite;
import com.legal.analysis.domain.model.Precedent;
import com.legal.analysis.domain.model.User;
import com.legal.analysis.domain.repository.FavoriteRepository;
import com.legal.analysis.domain.repository.PrecedentRepository;
import com.legal.analysis.domain.repository.UserRepository;
import com.legal.analysis.infrastructure.exception.DuplicateResourceException;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PrecedentRepository precedentRepository;
    private final UserRepository userRepository;

    @Transactional
    public FavoriteResponse addFavorite(Long userId, Long precedentId) {
        if (favoriteRepository.existsByUserIdAndPrecedentId(userId, precedentId)) {
            throw new DuplicateResourceException("Precedent is already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Precedent precedent = precedentRepository.findById(precedentId)
                .orElseThrow(() -> new ResourceNotFoundException("Precedent", precedentId));

        Favorite favorite = Favorite.builder()
                .user(user)
                .precedent(precedent)
                .build();

        return mapToResponse(favoriteRepository.save(favorite));
    }

    public Page<FavoriteResponse> getUserFavorites(Long userId, Pageable pageable) {
        return favoriteRepository.findByUserId(userId, pageable).map(this::mapToResponse);
    }

    @Transactional
    public void removeFavorite(Long userId, Long precedentId) {
        if (!favoriteRepository.existsByUserIdAndPrecedentId(userId, precedentId)) {
            throw new ResourceNotFoundException("Favorite not found");
        }
        favoriteRepository.deleteByUserIdAndPrecedentId(userId, precedentId);
    }

    private FavoriteResponse mapToResponse(Favorite favorite) {
        Precedent p = favorite.getPrecedent();

        CategoryResponse categoryResponse = Optional.ofNullable(p.getCategory())
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
                        c.getParent() != null ? c.getParent().getId() : null))
                .orElse(null);

        Set<TagResponse> tagResponses = p.getTags().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getColor()))
                .collect(Collectors.toSet());

        PrecedentResponse precedentResponse = new PrecedentResponse(
                p.getId(), p.getCaseNumber(), p.getTitle(), p.getContent(),
                p.getSummary(), p.getDecisionDate(), p.getCourtName(),
                p.getDecision(), categoryResponse, tagResponses, p.getCreatedAt()
        );

        return new FavoriteResponse(favorite.getId(), precedentResponse, favorite.getAddedAt());
    }
}
