package com.logistics.controltower.security;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRevocationRepository extends JpaRepository<TokenRevocation, String> {
    long deleteByExpiresAtBefore(Instant now);
}
