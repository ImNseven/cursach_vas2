package com.legal.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LegalAnalysisApplication {

    public static void main(String[] args) {
        normalizeDatabaseUrl();
        SpringApplication.run(LegalAnalysisApplication.class, args);
    }

    private static void normalizeDatabaseUrl() {
        String rawDatabaseUrl = System.getenv("DATABASE_URL");
        if (rawDatabaseUrl == null || rawDatabaseUrl.isBlank()) {
            return;
        }

        if (rawDatabaseUrl.startsWith("jdbc:")) {
            return;
        }

        String normalized = rawDatabaseUrl
                .replaceFirst("^postgres://", "jdbc:postgresql://")
                .replaceFirst("^postgresql://", "jdbc:postgresql://");

        if (normalized.startsWith("jdbc:postgresql://")) {
            System.setProperty("spring.datasource.url", normalized);
        }
    }
}
