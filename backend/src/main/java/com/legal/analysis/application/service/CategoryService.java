package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.response.CategoryResponse;
import com.legal.analysis.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getParent() != null ? c.getParent().getId() : null
                ))
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(c -> new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        null
                ))
                .collect(Collectors.toList());
    }
}
