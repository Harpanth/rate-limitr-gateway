package com.example.rate_limiter_gateway.filter;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.rate_limiter_gateway.dto.RateLimitCheckResponse;
import com.example.rate_limiter_gateway.exception.ApiKeyMissingException;
import com.example.rate_limiter_gateway.exception.InvalidApiKeyException;
import com.example.rate_limiter_gateway.metrics.RateLimiterMetricsService;
import com.example.rate_limiter_gateway.service.RateLimiterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    private final RateLimiterService rateLimiterService;
    private final RateLimiterMetricsService metrics;

    public ApiKeyFilter(RateLimiterService rateLimiterService,
            RateLimiterMetricsService metrics) {
        this.rateLimiterService = rateLimiterService;
        this.metrics = metrics;
    }

    private static final Set<String> VALID_KEYS = Set.of("test-key-123", "demo-key-abc");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String apiKey = request.getHeader("X-API-Key");
        String route = request.getRequestURI();

        metrics.markTotalRequest(); // ⭐ Count every request

        log.info("Incoming request → IP={}, API-Key={}, Route={}", ip, apiKey, route);

        // ----------------------------------------------------
        // 1. Validate API Key
        // ----------------------------------------------------
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Blocked request: Missing API key from IP {}", ip);
            metrics.markBlockedByApiKey();
            throw new ApiKeyMissingException();
        }

        if (!VALID_KEYS.contains(apiKey)) {
            log.warn("Blocked request: Invalid API key '{}' from IP {}", apiKey, ip);
            metrics.markBlockedByApiKey();
            throw new InvalidApiKeyException();
        }

        // ----------------------------------------------------
        // 2. IP Rate Limit Check
        // ----------------------------------------------------
        RateLimitCheckResponse ipResult = rateLimiterService.checkIpLimit(ip);

        response.setHeader("X-RateLimit-IP-Limit", String.valueOf(ipResult.capacity()));
        response.setHeader("X-RateLimit-IP-Remaining", String.valueOf(ipResult.remaining()));
        response.setHeader("X-RateLimit-IP-Reset", String.valueOf(ipResult.resetAt()));

        if (!ipResult.allowed()) {
            log.warn("IP limit exceeded → IP={}, Route={}, Remaining={}",
                    ip, route, ipResult.remaining());

            metrics.markBlockedByIp();

            response.setStatus(429);
            response.getWriter().write("{\"error\":\"Too many requests from this IP\"}");
            return;
        }

        // ----------------------------------------------------
        // 3. API Key Rate Limit Check
        // ----------------------------------------------------
        RateLimitCheckResponse apiKeyResult = rateLimiterService.checkRateLimit(apiKey, route);

        response.setHeader("X-RateLimit-Limit", String.valueOf(apiKeyResult.capacity()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(apiKeyResult.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(apiKeyResult.resetAt()));

        if (!apiKeyResult.allowed()) {
            log.warn("API key limit exceeded → API-Key={}, Route={}, Remaining={}",
                    apiKey, route, apiKeyResult.remaining());

            metrics.markBlockedByApiKey();

            response.setStatus(429);
            response.getWriter().write("{\"error\":\"API key rate limit exceeded\"}");
            return;
        }

        // ----------------------------------------------------
        // 4. SUCCESS — Request Allowed
        // ----------------------------------------------------
        log.info("Request allowed → IP={}, API-Key={}, Route={}, Remaining={}",
                ip, apiKey, route, apiKeyResult.remaining());

        metrics.markAllowed();

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/error".equals(request.getServletPath());
    }
}
