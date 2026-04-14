package com.shortlink.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortlink.app.config.AppProperties;
import com.shortlink.app.service.RedisRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("POST".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/public/links")) {
            String clientKey = resolveClientKey(request);
            var gp = appProperties.getGuestPublic();
            if (!rateLimiterService.allow(
                    "guest-create", clientKey, gp.getCreateRequestsPerWindow(), gp.getCreateWindowSeconds())) {
                writeTooManyRequests(response);
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String bucket = bucketFor(path);
        if (bucket != null) {
            String clientKey = resolveClientKey(request);
            if (!rateLimiterService.allow(bucket, clientKey)) {
                writeTooManyRequests(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        pd.setTitle("rate_limited");
        pd.setProperty("timestamp", Instant.now());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), pd);
    }

    private String bucketFor(String path) {
        if (path.startsWith("/l/") || path.startsWith("/r/")) {
            return "redirect";
        }
        if (path.startsWith("/api/v1/auth/register") || path.startsWith("/api/v1/auth/login")) {
            return "auth";
        }
        return null;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return sanitizeKey(forwarded.split(",")[0].trim());
        }
        return sanitizeKey(Optional.ofNullable(request.getRemoteAddr()).orElse("unknown"));
    }

    private String sanitizeKey(String raw) {
        String s = raw.replaceAll("[^a-zA-Z0-9.:_-]", "_");
        return s.length() > 128 ? s.substring(0, 128) : s;
    }
}
