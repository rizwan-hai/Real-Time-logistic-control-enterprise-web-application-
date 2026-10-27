package com.logistics.controltower.security;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TokenRevocationCleanup {
    private final TokenRevocationRepository revokedTokens;
    public TokenRevocationCleanup(TokenRevocationRepository revokedTokens) {
        this.revokedTokens = revokedTokens;
    }
    @Scheduled(cron = "0 17 3 * * *")
    @Transactional
    public void removeExpiredRevocations() {
        revokedTokens.deleteByExpiresAtBefore(Instant.now());
    }
}
