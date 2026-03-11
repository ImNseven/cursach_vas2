package com.legal.analysis.infrastructure.parser;

import com.legal.analysis.infrastructure.exception.DocumentParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TxtDocumentParser implements DocumentParser {

    @Override
    public String parse(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("Error parsing TXT file: {}", file.getOriginalFilename(), e);
            throw new DocumentParseException("Failed to parse TXT file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String fileExtension) {
        return "txt".equalsIgnoreCase(fileExtension);
    }
}
