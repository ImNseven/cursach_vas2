package com.legal.analysis.application.service;

import com.legal.analysis.application.dto.response.DocumentResponse;
import com.legal.analysis.application.dto.response.CategoryResponse;
import com.legal.analysis.application.dto.response.TagResponse;
import com.legal.analysis.domain.model.Document;
import com.legal.analysis.domain.model.Precedent;
import com.legal.analysis.domain.model.User;
import com.legal.analysis.domain.repository.DocumentRepository;
import com.legal.analysis.domain.repository.PrecedentRepository;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final int AUTO_PRECEDENT_SUMMARY_LIMIT = 500;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final PrecedentRepository precedentRepository;
    private final DocumentParserFactory parserFactory;
    private static final DateTimeFormatter UPLOAD_TITLE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
        createPrecedentFromUploadedDocument(saved);
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
                document.getMatchedPrecedents().size(),
                document.getContent()
        );
    }

    private void createPrecedentFromUploadedDocument(Document document) {
        precedentRepository.findBySourceDocumentId(document.getId()).ifPresentOrElse(
                existing -> log.debug("Precedent for document {} already exists (precedentId={})",
                        document.getId(), existing.getId()),
                () -> {
                    LocalDateTime uploadTime = document.getUploadedAt() != null
                            ? document.getUploadedAt()
                            : LocalDateTime.now();
                    String dateLabel = uploadTime.format(UPLOAD_TITLE_DATE_FORMAT);

                    Precedent precedent = Precedent.builder()
                            .caseNumber("UPLOAD-" + document.getId())
                            .title(resolveUploadedPrecedentTitle(document, dateLabel))
                            .content(document.getContent())
                            .summary(buildSummaryPreview(document.getContent()))
                            .sourceDocument(document)
                            .build();

                    Precedent savedPrecedent = precedentRepository.save(precedent);
                    log.info("Auto-created precedent {} from uploaded document {}",
                            savedPrecedent.getId(), document.getId());
                }
        );
    }

    private String buildSummaryPreview(String content) {
        if (content == null) {
            return null;
        }
        String normalized = content.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() <= AUTO_PRECEDENT_SUMMARY_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, AUTO_PRECEDENT_SUMMARY_LIMIT) + "...";
    }

    private String resolveUploadedPrecedentTitle(Document document, String dateLabel) {
        String fileName = Optional.ofNullable(document.getFileName())
                .map(String::trim)
                .orElse("");
        if (!fileName.isEmpty()) {
            return fileName;
        }

        String title = Optional.ofNullable(document.getTitle())
                .map(String::trim)
                .orElse("");
        if (!title.isEmpty()) {
            return title;
        }

        return "Документ №" + document.getId() + " от " + dateLabel;
    }
}
