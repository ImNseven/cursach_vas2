package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.response.*;
import com.legal.analysis.domain.model.*;
import com.legal.analysis.domain.repository.*;
import com.legal.analysis.infrastructure.analyzer.TextAnalysisStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final DocumentRepository documentRepository;
    private final PrecedentRepository precedentRepository;
    private final DocumentPrecedentRepository documentPrecedentRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TextAnalysisStrategy textAnalysisStrategy;

    private static final float SIMILARITY_THRESHOLD = 0.05f;
    private static final int MAX_RESULTS = 10;

    @Transactional
    public SearchResultResponse analyzeDocument(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new com.legal.analysis.infrastructure.exception.ResourceNotFoundException("Document", documentId));

        log.info("Starting analysis of document {} for user {}", documentId, userId);

        documentPrecedentRepository.deleteByDocumentId(documentId);

        List<Precedent> allPrecedents = precedentRepository.findAll();

        Map<String, Double> docVector = textAnalysisStrategy.computeVector(document.getContent());

        List<DocumentPrecedent> matches = new ArrayList<>();

        for (Precedent precedent : allPrecedents) {
            Map<String, Double> precVector = textAnalysisStrategy.computeVector(precedent.getContent());
            double similarity = textAnalysisStrategy.computeSimilarity(docVector, precVector);

            if (similarity >= SIMILARITY_THRESHOLD) {
                DocumentPrecedent dp = DocumentPrecedent.builder()
                        .document(document)
                        .precedent(precedent)
                        .similarityScore((float) similarity)
                        .build();
                matches.add(dp);
            }
        }

        matches.sort(Comparator.comparing(DocumentPrecedent::getSimilarityScore).reversed());

        List<DocumentPrecedent> topMatches = matches.stream()
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());

        documentPrecedentRepository.saveAll(topMatches);

        document.setIsAnalyzed(true);
        documentRepository.save(document);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.legal.analysis.infrastructure.exception.ResourceNotFoundException("User", userId));

        SearchHistory history = SearchHistory.builder()
                .user(user)
                .document(document)
                .resultsCount(topMatches.size())
                .build();
        searchHistoryRepository.save(history);

        log.info("Analysis complete for document {}. Found {} matches.", documentId, topMatches.size());

        Set<Long> userFavorites = favoriteRepository.findByUserId(userId, Pageable.unpaged())
                .stream()
                .map(f -> f.getPrecedent().getId())
                .collect(Collectors.toSet());

        List<PrecedentMatchResponse> matchResponses = topMatches.stream()
                .map(dp -> mapToMatchResponse(dp, userFavorites))
                .collect(Collectors.toList());

        return new SearchResultResponse(
                documentId,
                document.getTitle(),
                topMatches.size(),
                matchResponses,
                LocalDateTime.now()
        );
    }

    public SearchResultResponse getDocumentResults(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new com.legal.analysis.infrastructure.exception.ResourceNotFoundException("Document", documentId));

        List<DocumentPrecedent> matches = documentPrecedentRepository
                .findByDocumentIdOrderBySimilarityScoreDesc(documentId);

        Set<Long> userFavorites = favoriteRepository.findByUserId(userId, Pageable.unpaged())
                .stream()
                .map(f -> f.getPrecedent().getId())
                .collect(Collectors.toSet());

        List<PrecedentMatchResponse> matchResponses = matches.stream()
                .map(dp -> mapToMatchResponse(dp, userFavorites))
                .collect(Collectors.toList());

        return new SearchResultResponse(
                documentId,
                document.getTitle(),
                matches.size(),
                matchResponses,
                LocalDateTime.now()
        );
    }

    public Page<SearchHistoryResponse> getSearchHistory(Long userId, Pageable pageable) {
        return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId, pageable)
                .map(this::mapToHistoryResponse);
    }

    private PrecedentMatchResponse mapToMatchResponse(DocumentPrecedent dp, Set<Long> userFavoriteIds) {
        Precedent p = dp.getPrecedent();

        CategoryResponse categoryResponse = Optional.ofNullable(p.getCategory())
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
                        c.getParent() != null ? c.getParent().getId() : null))
                .orElse(null);

        Set<TagResponse> tagResponses = p.getTags().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getColor()))
                .collect(Collectors.toSet());

        return new PrecedentMatchResponse(
                p.getId(),
                p.getCaseNumber(),
                p.getTitle(),
                p.getSummary(),
                p.getCourtName(),
                p.getDecisionDate(),
                p.getDecision(),
                dp.getSimilarityScore(),
                categoryResponse,
                tagResponses,
                userFavoriteIds.contains(p.getId())
        );
    }

    private SearchHistoryResponse mapToHistoryResponse(SearchHistory history) {
        String documentTitle = Optional.ofNullable(history.getDocument())
                .map(Document::getTitle)
                .orElse("Unknown");
        Long documentId = Optional.ofNullable(history.getDocument())
                .map(Document::getId)
                .orElse(null);

        return new SearchHistoryResponse(
                history.getId(),
                documentId,
                documentTitle,
                history.getResultsCount(),
                history.getSearchedAt()
        );
    }
}
