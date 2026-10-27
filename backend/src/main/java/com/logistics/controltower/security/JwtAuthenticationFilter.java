package com.logistics.controltower.security;

import com.logistics.controltower.users.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository users;
    private final TokenRevocationRepository revokedTokens;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository users,
                                   TokenRevocationRepository revokedTokens) {
        this.jwtService = jwtService;
        this.users = users;
        this.revokedTokens = revokedTokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var claims = jwtService.parse(header.substring(7));
                if (revokedTokens.existsById(claims.tokenId())) {
                    SecurityContextHolder.clearContext();
                    chain.doFilter(request, response);
                    return;
                }
                users.findByEmailIgnoreCase(claims.subject()).filter(user -> user.isActive())
                        .ifPresent(user -> SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                                        user.getRoles().stream().map(role ->
                                                new SimpleGrantedAuthority("ROLE_" + role.name())).toList())));
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
