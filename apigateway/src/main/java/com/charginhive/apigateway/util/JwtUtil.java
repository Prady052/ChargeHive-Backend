package com.charginhive.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    private SecretKey key;

    // must be same as in auth service for successful validation
    @PostConstruct
    public void init() {
        log.info("Initializing JWT secret key for validation.");
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public void validateToken(final String token) {
        log.debug("Validating token... {}", token);
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        log.debug("Token validation successful.");
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
