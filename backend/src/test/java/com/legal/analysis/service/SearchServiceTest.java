package com.legal.analysis.service;

import com.legal.analysis.application.dto.response.SearchResultResponse;
import com.legal.analysis.application.dto.response.SimilarPrecedentsResponse;
import com.legal.analysis.application.service.SearchService;
import com.legal.analysis.domain.model.*;
import com.legal.analysis.domain.repository.*;
import com.legal.analysis.infrastructure.analyzer.TfIdfAnalysisStrategy;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PrecedentRepository precedentRepository;

    @Mock
    private DocumentPrecedentRepository documentPrecedentRepository;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TfIdfAnalysisStrategy textAnalysisStrategy;

    @InjectMocks
    private SearchService searchService;

    private Document testDocument;
    private User testUser;
    private Precedent testPrecedent;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").build();
        testUser = User.builder().id(1L).email("test@test.com").role(userRole).build();
        testDocument = Document.builder()
                .id(1L)
                .title("Test Document")
                .content("Иск о взыскании задолженности по договору займа")
                .user(testUser)
                .build();
        testPrecedent = Precedent.builder()
                .id(1L)
                .caseNumber("2-123/2023")
                .title("Взыскание займа")
                .content("Иск о взыскании задолженности по договору займа ответчик")
                .build();
    }

    @Test
    void analyzeDocument_shouldReturnResults_whenDocumentExists() {
        when(documentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDocument));
        when(precedentRepository.findAll()).thenReturn(List.of(testPrecedent));
        when(textAnalysisStrategy.computeVector(anyString())).thenReturn(java.util.Map.of("займ", 0.5, "иск", 0.3));
        when(textAnalysisStrategy.computeSimilarity(any(), any())).thenReturn(0.75);
        when(documentPrecedentRepository.saveAll(any())).thenReturn(List.of());
        when(documentRepository.save(any())).thenReturn(testDocument);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchHistoryRepository.save(any())).thenReturn(new SearchHistory());

        SearchResultResponse result = searchService.analyzeDocument(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.documentId()).isEqualTo(1L);
        assertThat(result.totalMatches()).isEqualTo(1);
        assertThat(result.matches().get(0).content()).isEqualTo(testPrecedent.getContent());
        assertThat(result.matches().get(0).summary()).isEqualTo(testPrecedent.getContent());
        verify(documentPrecedentRepository).deleteByDocumentId(1L);
    }

    @Test
    void analyzeDocument_shouldThrowException_whenDocumentNotFound() {
        when(documentRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> searchService.analyzeDocument(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void analyzeDocument_shouldFilterLowSimilarityResults() {
        when(documentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDocument));
        when(precedentRepository.findAll()).thenReturn(List.of(testPrecedent));
        when(textAnalysisStrategy.computeVector(anyString())).thenReturn(java.util.Map.of("слово", 1.0));
        when(textAnalysisStrategy.computeSimilarity(any(), any())).thenReturn(0.01); // Below threshold
        when(documentPrecedentRepository.saveAll(any())).thenReturn(List.of());
        when(documentRepository.save(any())).thenReturn(testDocument);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchHistoryRepository.save(any())).thenReturn(new SearchHistory());

        SearchResultResponse result = searchService.analyzeDocument(1L, 1L);

        assertThat(result.totalMatches()).isEqualTo(0);
    }

    @Test
    void analyzeDocument_shouldExcludeAutoCreatedPrecedentForSameDocument() {
        Precedent sourcePrecedent = Precedent.builder()
                .id(10L)
                .title("Auto precedent")
                .content("Тот же текст")
                .sourceDocument(testDocument)
                .build();

        Precedent otherPrecedent = Precedent.builder()
                .id(20L)
                .title("Other precedent")
                .content("Похожий спор")
                .build();

        when(documentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDocument));
        when(precedentRepository.findAll()).thenReturn(List.of(sourcePrecedent, otherPrecedent));
        when(textAnalysisStrategy.computeVector(anyString())).thenReturn(java.util.Map.of("иск", 1.0));
        when(textAnalysisStrategy.computeSimilarity(any(), any())).thenReturn(0.9);
        when(documentPrecedentRepository.saveAll(any())).thenReturn(List.of());
        when(documentRepository.save(any())).thenReturn(testDocument);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchHistoryRepository.save(any())).thenReturn(new SearchHistory());

        SearchResultResponse result = searchService.analyzeDocument(1L, 1L);

        assertThat(result.totalMatches()).isEqualTo(1);
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).precedentId()).isEqualTo(20L);
    }

    @Test
    void analyzeDocument_shouldUseSourceDocumentNameAndContentForUploadedPrecedents() {
        Document sourceDocument = Document.builder()
                .id(6L)
                .title("user-doc")
                .fileName("user-doc.txt")
                .content("Текст пользовательского прецедента")
                .user(testUser)
                .build();

        Precedent uploadedPrecedent = Precedent.builder()
                .id(30L)
                .caseNumber("UPLOAD-6")
                .title("Документ №6 от 16.03.2026")
                .content(null)
                .summary(null)
                .sourceDocument(sourceDocument)
                .build();

        when(documentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDocument));
        when(precedentRepository.findAll()).thenReturn(List.of(uploadedPrecedent));
        when(textAnalysisStrategy.computeVector(anyString())).thenReturn(java.util.Map.of("иск", 1.0));
        when(textAnalysisStrategy.computeSimilarity(any(), any())).thenReturn(0.8);
        when(documentPrecedentRepository.saveAll(any())).thenReturn(List.of());
        when(documentRepository.save(any())).thenReturn(testDocument);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchHistoryRepository.save(any())).thenReturn(new SearchHistory());

        SearchResultResponse result = searchService.analyzeDocument(1L, 1L);

        assertThat(result.totalMatches()).isEqualTo(1);
        assertThat(result.matches().get(0).title()).isEqualTo("user-doc.txt");
        assertThat(result.matches().get(0).content()).isEqualTo("Текст пользовательского прецедента");
        assertThat(result.matches().get(0).summary()).isEqualTo("Текст пользовательского прецедента");
    }

    @Test
    void analyzeDocument_shouldUseLegacyUploadCaseNumberFallback_whenSourceDocumentLinkIsMissing() {
        Document legacyDocument = Document.builder()
                .id(9L)
                .title("legacy-doc")
                .fileName("legacy-doc.txt")
                .content("Текст из legacy документа")
                .user(testUser)
                .build();

        Precedent legacyUploadedPrecedent = Precedent.builder()
                .id(90L)
                .caseNumber("UPLOAD-9")
                .title("Старое название")
                .content(null)
                .summary(null)
                .sourceDocument(null)
                .build();

        when(documentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDocument));
        when(precedentRepository.findAll()).thenReturn(List.of(legacyUploadedPrecedent));
        when(documentRepository.findById(9L)).thenReturn(Optional.of(legacyDocument));
        when(textAnalysisStrategy.computeVector(anyString())).thenReturn(java.util.Map.of("иск", 1.0));
        when(textAnalysisStrategy.computeSimilarity(any(), any())).thenReturn(0.8);
        when(documentPrecedentRepository.saveAll(any())).thenReturn(List.of());
        when(documentRepository.save(any())).thenReturn(testDocument);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchHistoryRepository.save(any())).thenReturn(new SearchHistory());

        SearchResultResponse result = searchService.analyzeDocument(1L, 1L);

        assertThat(result.totalMatches()).isEqualTo(1);
        assertThat(result.matches().get(0).title()).isEqualTo("legacy-doc.txt");
        assertThat(result.matches().get(0).content()).isEqualTo("Текст из legacy документа");
        assertThat(result.matches().get(0).summary()).isEqualTo("Текст из legacy документа");
    }

    @Test
    void findSimilarPrecedents_shouldReturnMatchesWithoutSelf() {
        Precedent basePrecedent = Precedent.builder()
                .id(1L)
                .title("Base")
                .content("Иск о займе")
                .build();
        Precedent similarPrecedent = Precedent.builder()
                .id(2L)
                .title("Similar")
                .content("Взыскание займа")
                .build();
        Precedent lowSimilarityPrecedent = Precedent.builder()
                .id(3L)
                .title("Different")
                .content("Наследственный спор")
                .build();

        when(precedentRepository.findById(1L)).thenReturn(Optional.of(basePrecedent));
        when(precedentRepository.findAll()).thenReturn(List.of(basePrecedent, similarPrecedent, lowSimilarityPrecedent));
        when(textAnalysisStrategy.computeVector(anyString())).thenReturn(java.util.Map.of("слово", 1.0));
        when(textAnalysisStrategy.computeSimilarity(any(), any())).thenReturn(0.8, 0.02);

        SimilarPrecedentsResponse result = searchService.findSimilarPrecedents(1L);

        assertThat(result.precedentId()).isEqualTo(1L);
        assertThat(result.totalMatches()).isEqualTo(1);
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).precedentId()).isEqualTo(2L);
    }
}
