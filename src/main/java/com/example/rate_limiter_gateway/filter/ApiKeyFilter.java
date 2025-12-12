package com.example.rate_limiter_gateway.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.example.rate_limiter_gateway.dto.RateLimitCheckResponse;
import com.example.rate_limiter_gateway.exception.ApiKeyMissingException;
import com.example.rate_limiter_gateway.exception.InvalidApiKeyException;
import com.example.rate_limiter_gateway.metrics.RateLimiterMetricsService;
import com.example.rate_limiter_gateway.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    private final RateLimiterService rateLimiterService;
    private final RateLimiterMetricsService metrics;
    private final HandlerExceptionResolver resolver;

    @Autowired
    public ApiKeyFilter(
            RateLimiterService rateLimiterService,
            RateLimiterMetricsService metrics,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {

        this.rateLimiterService = rateLimiterService;
        this.metrics = metrics;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            process(request, response, filterChain);
        } catch (Exception ex) {
            resolver.resolveException(request, response, null, ex);
        }
    }

    private void process(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws IOException, ServletException {

        String ip = request.getRemoteAddr();
        String apiKey = request.getHeader("X-API-Key");
        String route = request.getRequestURI();

        metrics.markTotalRequest();
        log.info("Incoming → IP={}, API-Key={}, Route={}", ip, apiKey, route);

        // 1) API Key validation
        if (apiKey == null || apiKey.isBlank())
            throw new ApiKeyMissingException();

        if (!rateLimiterService.isValidApiKey(apiKey))
            throw new InvalidApiKeyException();

        // 2) IP rate limit
        RateLimitCheckResponse ipLimit = rateLimiterService.checkIpLimit(ip);
        addIpHeaders(response, ipLimit);
        if (!ipLimit.allowed())
            throw new com.example.rate_limiter_gateway.exception.RateLimitExceededException();

        // 3) API key rate limit
        RateLimitCheckResponse keyLimit = rateLimiterService.checkRateLimit(apiKey, route);
        addApiKeyHeaders(response, keyLimit);
        if (!keyLimit.allowed())
            throw new com.example.rate_limiter_gateway.exception.RateLimitExceededException();

        metrics.markAllowed();
        filterChain.doFilter(request, response);
    }

    private void addIpHeaders(HttpServletResponse res, RateLimitCheckResponse ip) {
        res.setHeader("X-RateLimit-IP-Limit", String.valueOf(ip.capacity()));
        res.setHeader("X-RateLimit-IP-Remaining", String.valueOf(ip.remaining()));
        res.setHeader("X-RateLimit-IP-Reset", String.valueOf(ip.resetAt()));
    }

    private void addApiKeyHeaders(HttpServletResponse res, RateLimitCheckResponse rl) {
        res.setHeader("X-RateLimit-Limit", String.valueOf(rl.capacity()));
        res.setHeader("X-RateLimit-Remaining", String.valueOf(rl.remaining()));
        res.setHeader("X-RateLimit-Reset", String.valueOf(rl.resetAt()));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/error".equals(request.getServletPath());
    }
}
