package com.legal.analysis.infrastructure.parser;

import com.legal.analysis.infrastructure.exception.DocumentParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class DocxDocumentParser implements DocumentParser {

    @Override
    public String parse(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            log.error("Error parsing DOCX file: {}", file.getOriginalFilename(), e);
            throw new DocumentParseException("Failed to parse DOCX file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String fileExtension) {
        return "docx".equalsIgnoreCase(fileExtension);
    }
}
