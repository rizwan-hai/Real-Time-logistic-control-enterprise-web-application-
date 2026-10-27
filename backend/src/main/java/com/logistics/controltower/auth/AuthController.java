package com.logistics.controltower.auth;

import com.logistics.controltower.audit.AuditService;
import com.logistics.controltower.security.JwtService;
import com.logistics.controltower.security.TokenRevocation;
import com.logistics.controltower.security.TokenRevocationRepository;
import com.logistics.controltower.users.UserEntity;
import com.logistics.controltower.users.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRevocationRepository revokedTokens;
    private final AuditService audit;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder,
                          JwtService jwtService, TokenRevocationRepository revokedTokens, AuditService audit) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.revokedTokens = revokedTokens;
        this.audit = audit;
    }

    @PostMapping("/login")
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = users.findByEmailIgnoreCase(request.email().trim())
                .orElse(null);
        if (user == null) {
            audit.record("LOGIN_FAILED", "DENIED", "USER", null, "reason=invalid_credentials");
            throw invalidLogin();
        }
        if (!user.isActive() || (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now()))
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.recordFailedLogin();
            audit.recordAs(user.getEmail(), "LOGIN_FAILED", "DENIED", "USER", user.getId(),
                    "reason=invalid_credentials");
            throw invalidLogin();
        }
        user.recordSuccessfulLogin();
        audit.recordAs(user.getEmail(), "LOGIN_SUCCEEDED", "SUCCESS", "USER", user.getId(), null);
        String role = user.getRoles().stream().sorted().findFirst().orElseThrow().name();
        return new LoginResponse(jwtService.issue(user.getEmail(), role), "Bearer", jwtService.ttlSeconds());
    }

    @PostMapping("/logout")
    @Transactional
    public Map<String, String> logout(@RequestHeader("Authorization") String authorization) {
        var claims = jwtService.parse(authorization.substring("Bearer ".length()));
        revokedTokens.save(new TokenRevocation(claims.tokenId(), claims.expiresAt()));
        audit.record("LOGOUT", "SUCCESS", null, null, null);
        return Map.of("message", "Token revoked");
    }

    private ResponseStatusException invalidLogin() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    public record LoginRequest(@Email @NotBlank String email,
                               @NotBlank @Size(max = 72) String password) {}
    public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds) {}
}
