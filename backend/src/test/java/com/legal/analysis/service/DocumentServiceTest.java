package com.legal.analysis.service;

import com.legal.analysis.application.dto.response.DocumentResponse;
import com.legal.analysis.application.service.DocumentService;
import com.legal.analysis.domain.model.Document;
import com.legal.analysis.domain.model.Precedent;
import com.legal.analysis.domain.model.Role;
import com.legal.analysis.domain.model.User;
import com.legal.analysis.domain.repository.DocumentRepository;
import com.legal.analysis.domain.repository.PrecedentRepository;
import com.legal.analysis.domain.repository.UserRepository;
import com.legal.analysis.infrastructure.exception.DocumentParseException;
import com.legal.analysis.infrastructure.exception.ResourceNotFoundException;
import com.legal.analysis.infrastructure.parser.DocumentParserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PrecedentRepository precedentRepository;

    @Mock
    private DocumentParserFactory parserFactory;

    @InjectMocks
    private DocumentService documentService;

    private User testUser;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().id(1L).name("USER").build();
        testUser = User.builder().id(1L).email("test@test.com").role(userRole).build();
        testDocument = Document.builder()
                .id(1L)
                .title("test-doc")
                .content("Test content")
                .fileName("test-doc.txt")
                .fileType("txt")
                .fileSize(100L)
                .user(testUser)
                .isAnalyzed(false)
                .build();
    }

    @Test
    void uploadDocument_shouldReturnDocumentResponse_whenValidFile() {
        MultipartFile file = new MockMultipartFile(
                "file", "test-doc.txt", "text/plain", "Test content".getBytes()
        );

        when(parserFactory.isSupported("test-doc.txt")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(parserFactory.parseDocument(file)).thenReturn("Test content");
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        when(precedentRepository.findBySourceDocumentId(1L)).thenReturn(Optional.empty());
        when(precedentRepository.save(any(Precedent.class))).thenAnswer(invocation -> {
            Precedent precedent = invocation.getArgument(0);
            precedent.setId(100L);
            return precedent;
        });

        DocumentResponse response = documentService.uploadDocument(file, 1L);
        ArgumentCaptor<Precedent> precedentCaptor = ArgumentCaptor.forClass(Precedent.class);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("test-doc");
        assertThat(response.fileType()).isEqualTo("txt");
        verify(documentRepository).save(any(Document.class));
        verify(precedentRepository).save(precedentCaptor.capture());
        assertThat(precedentCaptor.getValue().getTitle()).isEqualTo("test-doc.txt");
        assertThat(precedentCaptor.getValue().getContent()).isEqualTo("Test content");
        assertThat(precedentCaptor.getValue().getSummary()).isEqualTo("Test content");
    }

    @Test
    void uploadDocument_shouldThrowException_whenUnsupportedFileType() {
        MultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes()
        );

        when(parserFactory.isSupported("test.pdf")).thenReturn(false);

        assertThatThrownBy(() -> documentService.uploadDocument(file, 1L))
                .isInstanceOf(DocumentParseException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void deleteDocument_shouldDeleteDocument_whenOwnerRequests() {
        when(documentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDocument));

        documentService.deleteDocument(1L, 1L);

        verify(documentRepository).delete(testDocument);
    }

    @Test
    void deleteDocument_shouldThrowException_whenDocumentNotFound() {
        when(documentRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteDocument(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDocumentById_shouldReturnDocument_whenExists() {
        when(documentRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testDocument));

        DocumentResponse response = documentService.getDocumentById(1L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
    }
}
