package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.response.TagResponse;
import com.legal.analysis.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getColor()))
                .collect(Collectors.toList());
    }
}
