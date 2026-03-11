package com.legal.analysis.service;

import com.legal.analysis.application.dto.response.PrecedentResponse;
import com.legal.analysis.application.service.PrecedentService;
import com.legal.analysis.domain.model.Category;
import com.legal.analysis.domain.model.Precedent;
import com.legal.analysis.domain.repository.CategoryRepository;
import com.legal.analysis.domain.repository.PrecedentRepository;
import com.legal.analysis.domain.repository.TagRepository;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrecedentServiceTest {

    @Mock
    private PrecedentRepository precedentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PrecedentService precedentService;

    private Precedent testPrecedent;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder().id(1L).name("Гражданское право").build();
        testPrecedent = Precedent.builder()
                .id(1L)
                .caseNumber("2-123/2023")
                .title("Взыскание задолженности по договору займа")
                .content("Судебное решение по иску о взыскании займа")
                .summary("Суд удовлетворил иск")
                .decisionDate(LocalDate.of(2023, 4, 12))
                .courtName("Районный суд г. Минска")
                .decision("Иск удовлетворен")
                .category(testCategory)
                .build();
    }

    @Test
    void getAllPrecedents_shouldReturnPagedResults() {
        Page<Precedent> page = new PageImpl<>(List.of(testPrecedent));
        when(precedentRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<PrecedentResponse> result = precedentService.getAllPrecedents(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).caseNumber()).isEqualTo("2-123/2023");
    }

    @Test
    void getPrecedentById_shouldReturnPrecedent_whenExists() {
        when(precedentRepository.findById(1L)).thenReturn(Optional.of(testPrecedent));

        PrecedentResponse response = precedentService.getPrecedentById(1L);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Взыскание задолженности по договору займа");
        assertThat(response.category().name()).isEqualTo("Гражданское право");
    }

    @Test
    void getPrecedentById_shouldThrowException_whenNotFound() {
        when(precedentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> precedentService.getPrecedentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchPrecedents_shouldReturnMatchingPrecedents() {
        when(precedentRepository.searchByText("займ")).thenReturn(List.of(testPrecedent));

        List<PrecedentResponse> results = precedentService.searchPrecedents("займ");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).caseNumber()).isEqualTo("2-123/2023");
    }

    @Test
    void deletePrecedent_shouldThrowException_whenNotFound() {
        when(precedentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> precedentService.deletePrecedent(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
