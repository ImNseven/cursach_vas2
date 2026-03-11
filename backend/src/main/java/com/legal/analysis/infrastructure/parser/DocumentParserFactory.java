package com.legal.analysis.infrastructure.parser;

import com.legal.analysis.infrastructure.exception.DocumentParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DocumentParserFactory {

    private final List<DocumentParser> parsers;

    public String parseDocument(MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new DocumentParseException("File has no name"));

        String extension = getExtension(originalFilename);

        DocumentParser parser = parsers.stream()
                .filter(p -> p.supports(extension))
                .findFirst()
                .orElseThrow(() -> new DocumentParseException("Unsupported file type: " + extension));

        return parser.parse(file);
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            throw new DocumentParseException("File has no extension: " + filename);
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    public boolean isSupported(String filename) {
        if (filename == null) return false;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) return false;
        String extension = filename.substring(lastDot + 1).toLowerCase();
        return parsers.stream().anyMatch(p -> p.supports(extension));
    }
}
