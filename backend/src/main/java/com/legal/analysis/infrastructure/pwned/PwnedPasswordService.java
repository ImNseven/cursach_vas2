package com.legal.analysis.infrastructure.pwned;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Have I Been Pwned API client for password breach check.
 * Uses k-anonymity: only first 5 chars of SHA-1 hash are sent to the API.
 * @see <a href="https://haveibeenpwned.com/API/v3#PwnedPasswords">HIBP API</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PwnedPasswordService {

    private static final String HIBP_RANGE_URL = "https://api.pwnedpasswords.com/range/";

    private final WebClient.Builder webClientBuilder;

    /**
     * Checks if the password has been found in known data breaches.
     *
     * @param password plain text password
     * @return true if password was pwned, false otherwise
     */
    public boolean isPasswordPwned(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        try {
            String sha1Hex = sha1Hex(password);
            String prefix = sha1Hex.substring(0, 5).toUpperCase();
            String suffix = sha1Hex.substring(5).toUpperCase();

            String response = webClientBuilder.build()
                    .get()
                    .uri(HIBP_RANGE_URL + prefix)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                return false;
            }

            Set<String> suffixes = Arrays.stream(response.split("\r?\n"))
                    .map(line -> {
                        int colon = line.indexOf(':');
                        return colon > 0 ? line.substring(0, colon).trim() : line.trim();
                    })
                    .collect(Collectors.toSet());

            return suffixes.contains(suffix);
        } catch (Exception e) {
            log.warn("Pwned password check failed, allowing registration: {}", e.getMessage());
            return false;
        }
    }

    private String sha1Hex(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
