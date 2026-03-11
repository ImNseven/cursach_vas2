package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.request.AnnotationRequest;
import com.legal.analysis.application.dto.response.AnnotationResponse;
import com.legal.analysis.domain.model.Annotation;
import com.legal.analysis.domain.model.Document;
import com.legal.analysis.domain.model.User;
import com.legal.analysis.domain.repository.AnnotationRepository;
import com.legal.analysis.domain.repository.DocumentRepository;
import com.legal.analysis.domain.repository.UserRepository;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnotationService {

    private final AnnotationRepository annotationRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Transactional
    public AnnotationResponse createAnnotation(AnnotationRequest request, Long userId) {
        Document document = documentRepository.findById(request.documentId())
                .orElseThrow(() -> new ResourceNotFoundException("Document", request.documentId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Annotation annotation = Annotation.builder()
                .document(document)
                .user(user)
                .content(request.content())
                .startPosition(request.startPosition())
                .endPosition(request.endPosition())
                .build();

        return mapToResponse(annotationRepository.save(annotation));
    }

    public List<AnnotationResponse> getDocumentAnnotations(Long documentId) {
        return annotationRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAnnotation(Long annotationId, Long userId) {
        Annotation annotation = annotationRepository.findByIdAndUserId(annotationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Annotation", annotationId));
        annotationRepository.delete(annotation);
    }

    private AnnotationResponse mapToResponse(Annotation annotation) {
        return new AnnotationResponse(
                annotation.getId(),
                annotation.getDocument().getId(),
                annotation.getContent(),
                annotation.getStartPosition(),
                annotation.getEndPosition(),
                annotation.getCreatedAt()
        );
    }
}
