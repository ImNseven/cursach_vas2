package com.legal.analysis.infrastructure.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@Service
public class GitHubOAuthService {

    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";

    private final WebClient.Builder webClientBuilder;

    @Value("${github.oauth.client-id}")
    private String clientId;

    @Value("${github.oauth.client-secret}")
    private String clientSecret;

    @Value("${github.oauth.redirect-uri}")
    private String redirectUri;

    public GitHubOAuthService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Optional<String> exchangeCodeForToken(String code) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("code", code);
            formData.add("redirect_uri", redirectUri);

            TokenResponse response = webClientBuilder.build()
                    .post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            if (response != null && response.access_token() != null && !response.access_token().isBlank()) {
                return Optional.of(response.access_token());
            }
            log.warn("GitHub token exchange returned no access_token. error={}, description={}, redirect_uri_used={}",
                    response != null ? response.error() : "null",
                    response != null ? response.error_description() : "null",
                    redirectUri);
            return Optional.empty();
        } catch (Exception e) {
            log.error("GitHub token exchange failed", e);
            return Optional.empty();
        }
    }

    private record TokenResponse(
            @JsonProperty("access_token") String access_token,
            @JsonProperty("token_type") String token_type,
            @JsonProperty("scope") String scope,
            @JsonProperty("error") String error,
            @JsonProperty("error_description") String error_description
    ) {}
}
