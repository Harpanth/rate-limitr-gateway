package com.example.rate_limiter_gateway.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class RateLimiterMetricsService {

    private final Counter allowedRequests;
    private final Counter blockedByApiKey;
    private final Counter blockedByIp;
    private final Counter totalRequests;

    public RateLimiterMetricsService(MeterRegistry registry) {

        this.totalRequests = Counter.builder("gateway_requests_total")
                .description("Total requests received by API Gateway")
                .register(registry);

        this.allowedRequests = Counter.builder("gateway_requests_allowed_total")
                .description("Requests successfully allowed by rate limiter")
                .register(registry);

        this.blockedByApiKey = Counter.builder("gateway_requests_blocked_apikey_total")
                .description("Requests blocked due to API key rate limiting")
                .register(registry);

        this.blockedByIp = Counter.builder("gateway_requests_blocked_ip_total")
                .description("Requests blocked due to IP rate limiting")
                .register(registry);
    }

    public void markTotalRequest() {
        totalRequests.increment();
    }

    public void markAllowed() {
        allowedRequests.increment();
    }

    public void markBlockedByApiKey() {
        blockedByApiKey.increment();
    }

    public void markBlockedByIp() {
        blockedByIp.increment();
    }
}
