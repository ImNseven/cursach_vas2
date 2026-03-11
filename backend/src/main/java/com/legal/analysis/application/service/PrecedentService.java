package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.request.PrecedentRequest;
import com.legal.analysis.application.dto.response.CategoryResponse;
import com.legal.analysis.application.dto.response.PrecedentResponse;
import com.legal.analysis.application.dto.response.TagResponse;
import com.legal.analysis.domain.model.Precedent;
import com.legal.analysis.domain.model.Tag;
import com.legal.analysis.domain.repository.CategoryRepository;
import com.legal.analysis.domain.repository.PrecedentRepository;
import com.legal.analysis.domain.repository.TagRepository;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrecedentService {

    private final PrecedentRepository precedentRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public Page<PrecedentResponse> getAllPrecedents(Pageable pageable) {
        return precedentRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<PrecedentResponse> getPrecedentsByCategory(Long categoryId, Pageable pageable) {
        return precedentRepository.findByCategoryId(categoryId, pageable).map(this::mapToResponse);
    }

    public PrecedentResponse getPrecedentById(Long id) {
        Precedent precedent = precedentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Precedent", id));
        return mapToResponse(precedent);
    }

    public List<PrecedentResponse> searchPrecedents(String query) {
        return precedentRepository.searchByText(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrecedentResponse createPrecedent(PrecedentRequest request) {
        Precedent precedent = buildPrecedent(new Precedent(), request);
        return mapToResponse(precedentRepository.save(precedent));
    }

    @Transactional
    public PrecedentResponse updatePrecedent(Long id, PrecedentRequest request) {
        Precedent precedent = precedentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Precedent", id));
        buildPrecedent(precedent, request);
        return mapToResponse(precedentRepository.save(precedent));
    }

    @Transactional
    public void deletePrecedent(Long id) {
        if (!precedentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Precedent", id);
        }
        precedentRepository.deleteById(id);
    }

    private Precedent buildPrecedent(Precedent precedent, PrecedentRequest request) {
        precedent.setCaseNumber(request.caseNumber());
        precedent.setTitle(request.title());
        precedent.setContent(request.content());
        precedent.setSummary(request.summary());
        precedent.setDecisionDate(request.decisionDate());
        precedent.setCourtName(request.courtName());
        precedent.setDecision(request.decision());

        if (request.categoryId() != null) {
            categoryRepository.findById(request.categoryId())
                    .ifPresent(precedent::setCategory);
        }

        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.tagIds()));
            precedent.setTags(tags);
        }

        return precedent;
    }

    PrecedentResponse mapToResponse(Precedent precedent) {
        CategoryResponse categoryResponse = Optional.ofNullable(precedent.getCategory())
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
                        c.getParent() != null ? c.getParent().getId() : null))
                .orElse(null);

        Set<TagResponse> tagResponses = precedent.getTags().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getColor()))
                .collect(Collectors.toSet());

        return new PrecedentResponse(
                precedent.getId(),
                precedent.getCaseNumber(),
                precedent.getTitle(),
                precedent.getContent(),
                precedent.getSummary(),
                precedent.getDecisionDate(),
                precedent.getCourtName(),
                precedent.getDecision(),
                categoryResponse,
                tagResponses,
                precedent.getCreatedAt()
        );
    }
}
