package com.legal.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LegalAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegalAnalysisApplication.class, args);
    }
}
