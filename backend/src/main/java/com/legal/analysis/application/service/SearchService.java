package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.response.*;
import com.legal.analysis.domain.model.*;
import com.legal.analysis.domain.repository.*;
import com.legal.analysis.infrastructure.analyzer.TextAnalysisStrategy;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
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

    private static final int SUMMARY_PREVIEW_LIMIT = 500;
    private final DocumentRepository documentRepository;
    private final PrecedentRepository precedentRepository;
    private final DocumentPrecedentRepository documentPrecedentRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;
    private final TextAnalysisStrategy textAnalysisStrategy;

    private static final float SIMILARITY_THRESHOLD = 0.05f;
    private static final int MAX_RESULTS = 10;

    @Transactional
    public SearchResultResponse analyzeDocument(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));

        log.info("Starting analysis of document {} for user {}", documentId, userId);

        documentPrecedentRepository.deleteByDocumentId(documentId);

        List<Precedent> allPrecedents = precedentRepository.findAll();

        Map<String, Double> docVector = textAnalysisStrategy.computeVector(document.getContent());

        List<DocumentPrecedent> matches = new ArrayList<>();

        for (Precedent precedent : allPrecedents) {
            if (precedent.getSourceDocument() != null
                    && Objects.equals(precedent.getSourceDocument().getId(), documentId)) {
                continue;
            }

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
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        SearchHistory history = SearchHistory.builder()
                .user(user)
                .document(document)
                .resultsCount(topMatches.size())
                .build();
        searchHistoryRepository.save(history);

        log.info("Analysis complete for document {}. Found {} matches.", documentId, topMatches.size());

        List<PrecedentMatchResponse> matchResponses = topMatches.stream()
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());

        return new SearchResultResponse(
                documentId,
                document.getTitle(),
                document.getContent(),
                topMatches.size(),
                matchResponses,
                LocalDateTime.now()
        );
    }

    public SearchResultResponse getDocumentResults(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));

        List<DocumentPrecedent> matches = documentPrecedentRepository
                .findByDocumentIdOrderBySimilarityScoreDesc(documentId);

        List<PrecedentMatchResponse> matchResponses = matches.stream()
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());

        return new SearchResultResponse(
                documentId,
                document.getTitle(),
                document.getContent(),
                matches.size(),
                matchResponses,
                LocalDateTime.now()
        );
    }

    public SimilarPrecedentsResponse findSimilarPrecedents(Long precedentId) {
        Precedent basePrecedent = precedentRepository.findById(precedentId)
                .orElseThrow(() -> new ResourceNotFoundException("Precedent", precedentId));

        Map<String, Double> baseVector = textAnalysisStrategy.computeVector(basePrecedent.getContent());

        List<PrecedentMatchResponse> matches = precedentRepository.findAll().stream()
                .filter(candidate -> !Objects.equals(candidate.getId(), precedentId))
                .map(candidate -> Map.entry(candidate,
                        textAnalysisStrategy.computeSimilarity(
                                baseVector,
                                textAnalysisStrategy.computeVector(candidate.getContent())
                        )))
                .filter(entry -> entry.getValue() >= SIMILARITY_THRESHOLD)
                .sorted(Map.Entry.<Precedent, Double>comparingByValue().reversed())
                .limit(MAX_RESULTS)
                .map(entry -> mapToMatchResponse(entry.getKey(), entry.getValue().floatValue()))
                .collect(Collectors.toList());

        return new SimilarPrecedentsResponse(
                basePrecedent.getId(),
                basePrecedent.getTitle(),
                matches.size(),
                matches,
                LocalDateTime.now()
        );
    }

    public Page<SearchHistoryResponse> getSearchHistory(Long userId, Pageable pageable) {
        return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId, pageable)
                .map(this::mapToHistoryResponse);
    }

    private PrecedentMatchResponse mapToMatchResponse(DocumentPrecedent dp) {
        return mapToMatchResponse(dp.getPrecedent(), dp.getSimilarityScore());
    }

    private PrecedentMatchResponse mapToMatchResponse(Precedent p, Float similarityScore) {
        Optional<Document> sourceDocumentOptional = resolveSourceDocument(p);

        String sourceDocumentTitle = sourceDocumentOptional
                .map(doc -> normalizeText(
                        doc.getFileName() != null ? doc.getFileName() : doc.getTitle()
                ))
                .orElse(null);
        String sourceDocumentContent = sourceDocumentOptional
                .map(Document::getContent)
                .map(this::normalizeText)
                .orElse(null);

        String title = sourceDocumentTitle != null
                ? sourceDocumentTitle
                : normalizeText(p.getTitle());
        if (title == null) {
            title = "Без названия";
        }

        String content = firstNotBlank(normalizeText(p.getContent()), sourceDocumentContent);
        String summary = normalizeText(p.getSummary());
        if (summary == null && content != null) {
            summary = buildPreview(content, SUMMARY_PREVIEW_LIMIT);
        }

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
                title,
                summary,
                content,
                p.getCourtName(),
                p.getDecisionDate(),
                p.getDecision(),
                similarityScore,
                categoryResponse,
                tagResponses
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String buildPreview(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private String firstNotBlank(String primary, String fallback) {
        return primary != null ? primary : fallback;
    }

    private Optional<Document> resolveSourceDocument(Precedent precedent) {
        if (precedent.getSourceDocument() != null) {
            return Optional.of(precedent.getSourceDocument());
        }

        String caseNumber = normalizeText(precedent.getCaseNumber());
        if (caseNumber == null || !caseNumber.toUpperCase(Locale.ROOT).startsWith("UPLOAD-")) {
            return Optional.empty();
        }

        String documentIdPart = caseNumber.substring("UPLOAD-".length()).trim();
        if (documentIdPart.isEmpty()) {
            return Optional.empty();
        }

        try {
            Long documentId = Long.parseLong(documentIdPart);
            return documentRepository.findById(documentId);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
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
