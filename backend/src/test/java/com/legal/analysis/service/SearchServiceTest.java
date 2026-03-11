package com.legal.analysis.service;

import com.legal.analysis.application.dto.response.SearchResultResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    private FavoriteRepository favoriteRepository;

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
        when(favoriteRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        SearchResultResponse result = searchService.analyzeDocument(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.documentId()).isEqualTo(1L);
        assertThat(result.totalMatches()).isEqualTo(1);
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
        when(favoriteRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        SearchResultResponse result = searchService.analyzeDocument(1L, 1L);

        assertThat(result.totalMatches()).isEqualTo(0);
    }
}
