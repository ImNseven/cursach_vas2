package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.response.DocumentResponse;
import com.legal.analysis.application.dto.response.CategoryResponse;
import com.legal.analysis.application.dto.response.TagResponse;
import com.legal.analysis.domain.model.Document;
import com.legal.analysis.domain.model.User;
import com.legal.analysis.domain.repository.DocumentRepository;
import com.legal.analysis.domain.repository.UserRepository;
import com.legal.analysis.infrastructure.exception.DocumentParseException;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
import com.legal.analysis.infrastructure.parser.DocumentParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentParserFactory parserFactory;

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, Long userId) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new DocumentParseException("File has no name"));

        if (!parserFactory.isSupported(originalFilename)) {
            throw new DocumentParseException("Unsupported file type. Supported types: .txt, .docx");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String content = parserFactory.parseDocument(file);

        String title = originalFilename.contains(".")
                ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
                : originalFilename;

        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase()
                : "";

        Document document = Document.builder()
                .title(title)
                .content(content)
                .fileName(originalFilename)
                .fileType(extension)
                .fileSize(file.getSize())
                .user(user)
                .isAnalyzed(false)
                .build();

        Document saved = documentRepository.save(document);
        log.info("Document uploaded: {} by user {}", saved.getId(), userId);
        return mapToResponse(saved);
    }

    public Page<DocumentResponse> getUserDocuments(Long userId, Pageable pageable) {
        return documentRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    public DocumentResponse getDocumentById(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        return mapToResponse(document);
    }

    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        documentRepository.delete(document);
        log.info("Document deleted: {} by user {}", documentId, userId);
    }

    public Document getDocumentEntityById(Long documentId, Long userId) {
        return documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
    }

    @Transactional
    public void markAsAnalyzed(Long documentId) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setIsAnalyzed(true);
            documentRepository.save(doc);
        });
    }

    DocumentResponse mapToResponse(Document document) {
        CategoryResponse categoryResponse = Optional.ofNullable(document.getCategory())
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
                        c.getParent() != null ? c.getParent().getId() : null))
                .orElse(null);

        Set<TagResponse> tagResponses = document.getTags().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getColor()))
                .collect(Collectors.toSet());

        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getFileName(),
                document.getFileType(),
                document.getFileSize(),
                categoryResponse,
                tagResponses,
                document.getUploadedAt(),
                document.getIsAnalyzed(),
                document.getMatchedPrecedents().size()
        );
    }
}
