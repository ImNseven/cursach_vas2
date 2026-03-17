package com.legal.analysis.infrastructure.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubApiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${github.api.base-url}")
    private String baseUrl;

    public Optional<GitHubUserInfo> getUserInfo(String accessToken) {
        try {
            GitHubUserInfo userInfo = webClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/user")
                    .header("Authorization", "token " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(GitHubUserInfo.class)
                    .block();
            return Optional.ofNullable(userInfo);
        } catch (Exception e) {
            log.error("Error fetching GitHub user info", e);
            return Optional.empty();
        }
    }

    public Optional<String> getUserPrimaryEmail(String accessToken) {
        try {
            GitHubEmail[] emails = webClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/user/emails")
                    .header("Authorization", "token " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(GitHubEmail[].class)
                    .block();

            if (emails == null) return Optional.empty();

            return java.util.Arrays.stream(emails)
                    .filter(GitHubEmail::primary)
                    .map(GitHubEmail::email)
                    .findFirst();
        } catch (Exception e) {
            log.error("Error fetching GitHub user emails", e);
            return Optional.empty();
        }
    }

    public record GitHubUserInfo(
            Long id,
            String login,
            String name,
            String email,
            @JsonProperty("avatar_url") String avatarUrl
    ) {}

    public record GitHubEmail(
            String email,
            boolean primary,
            boolean verified
    ) {}
}
