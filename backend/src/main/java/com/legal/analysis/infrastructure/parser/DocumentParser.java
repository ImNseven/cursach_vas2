package com.legal.analysis.infrastructure.parser;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentParser {

    String parse(MultipartFile file);

    boolean supports(String fileExtension);
}
