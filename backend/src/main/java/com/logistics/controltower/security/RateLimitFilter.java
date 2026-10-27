package com.logistics.controltower.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000L;
    private final Map<String, RequestBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr();
        RequestBucket bucket = buckets.computeIfAbsent(key, ignore -> new RequestBucket());
        long now = Instant.now().toEpochMilli();
        synchronized (bucket) {
            if (bucket.windowStart + WINDOW_MS < now) {
                bucket.windowStart = now;
                bucket.count = 0;
            }
            if (bucket.count >= MAX_REQUESTS) {
                response.setStatus(429);
                response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
                response.setHeader("X-RateLimit-Reset", String.valueOf(WINDOW_MS));
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
                return;
            }
            bucket.count++;
        }
        filterChain.doFilter(request, response);
    }

    private static final class RequestBucket {
        long windowStart = Instant.now().toEpochMilli();
        int count = 0;
    }
}
