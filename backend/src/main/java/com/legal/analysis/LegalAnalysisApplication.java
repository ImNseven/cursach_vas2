package com.legal.analysis;

import java.net.URI;
import java.net.URISyntaxException;

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

        String candidate = rawDatabaseUrl.startsWith("jdbc:")
                ? rawDatabaseUrl.substring("jdbc:".length())
                : rawDatabaseUrl;

        try {
            URI uri = new URI(candidate);
            String scheme = uri.getScheme();
            if (!"postgres".equalsIgnoreCase(scheme) && !"postgresql".equalsIgnoreCase(scheme)) {
                return;
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return;
            }

            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath() == null ? "" : uri.getPath();
            String database = path.startsWith("/") ? path.substring(1) : path;
            if (database.isBlank()) {
                return;
            }

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                jdbcUrl += "?" + uri.getQuery();
            }

            System.setProperty("spring.datasource.url", jdbcUrl);

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                if (parts.length >= 1 && !parts[0].isBlank()) {
                    System.setProperty("spring.datasource.username", parts[0]);
                }
                if (parts.length == 2) {
                    System.setProperty("spring.datasource.password", parts[1]);
                }
            }
        } catch (URISyntaxException ignored) {
            String normalized = rawDatabaseUrl
                    .replaceFirst("^postgres://", "jdbc:postgresql://")
                    .replaceFirst("^postgresql://", "jdbc:postgresql://");
            if (normalized.startsWith("jdbc:postgresql://")) {
                System.setProperty("spring.datasource.url", normalized);
            }
        }
    }
}
