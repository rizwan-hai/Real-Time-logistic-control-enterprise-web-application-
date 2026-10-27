package com.logistics.controltower.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final Duration ttl;

    public JwtService(@Value("${app.security.jwt-secret}") String secret,
                      @Value("${app.security.jwt-ttl:PT15M}") Duration ttl) {
        try {
            this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT_SECRET must be a Base64-encoded key of at least 256 bits", exception);
        }
        this.ttl = ttl;
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalStateException("JWT_TTL must be positive");
        }
    }

    public String issue(String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public String subject(String token) {
        return parse(token).subject();
    }

    public TokenClaims parse(String token) {
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new TokenClaims(claims.getSubject(), claims.getId(), claims.getExpiration().toInstant());
    }

    public long ttlSeconds() {
        return ttl.toSeconds();
    }

    public record TokenClaims(String subject, String tokenId, Instant expiresAt) {}
}
