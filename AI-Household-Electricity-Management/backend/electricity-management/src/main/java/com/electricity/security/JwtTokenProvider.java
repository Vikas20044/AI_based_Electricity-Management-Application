package com.electricity.security;

import com.electricity.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secretKey;
    private final long expirationSeconds;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(
            @Value("${jwt.secret:ai-electricity-management-super-secret-key-2026-secure-token-mca-major-project-key}") String secretKey,
            @Value("${jwt.expiration-hours:24}") long expirationHours,
            ObjectMapper objectMapper) {
        this.secretKey = secretKey;
        this.expirationSeconds = expirationHours * 3600;
        this.objectMapper = objectMapper;
    }

    public String generateToken(User user) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + expirationSeconds;

            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsBytes(header));

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", user.getEmail());
            payload.put("userId", user.getId());
            payload.put("role", user.getRole());
            payload.put("name", user.getFullName());
            payload.put("iat", now);
            payload.put("exp", exp);
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsBytes(payload));

            String dataToSign = encodedHeader + "." + encodedPayload;
            String signature = sign(dataToSign);

            return dataToSign + "." + signature;
        } catch (Exception e) {
            logger.error("Failed to generate JWT token: {}", e.getMessage());
            throw new RuntimeException("Could not generate authentication token", e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            String dataToSign = parts[0] + "." + parts[1];
            String expectedSignature = sign(dataToSign);

            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                logger.warn("JWT signature verification failed");
                return false;
            }

            JsonNode payloadNode = parsePayload(parts[1]);
            long exp = payloadNode.has("exp") ? payloadNode.get("exp").asLong() : 0;
            long now = Instant.now().getEpochSecond();

            if (now >= exp) {
                logger.warn("JWT token has expired (now: {}, exp: {})", now, exp);
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.warn("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        try {
            String[] parts = token.split("\\.");
            JsonNode payload = parsePayload(parts[1]);
            return payload.has("sub") ? payload.get("sub").asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            JsonNode payload = parsePayload(parts[1]);
            return payload.has("userId") ? payload.get("userId").asLong() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private JsonNode parsePayload(String encodedPayload) throws Exception {
        byte[] bytes = Base64.getUrlDecoder().decode(encodedPayload);
        return objectMapper.readTree(bytes);
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(hmacBytes);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
