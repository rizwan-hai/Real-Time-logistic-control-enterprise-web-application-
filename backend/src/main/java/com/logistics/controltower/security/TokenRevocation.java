package com.logistics.controltower.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "token_revocations")
public class TokenRevocation {
    @Id
    @Column(length = 36)
    private String tokenId;
    @Column(nullable = false)
    private Instant expiresAt;

    protected TokenRevocation() {}
    public TokenRevocation(String tokenId, Instant expiresAt) {
        this.tokenId = tokenId;
        this.expiresAt = expiresAt;
    }
    public String getTokenId() { return tokenId; }
    public Instant getExpiresAt() { return expiresAt; }
}
