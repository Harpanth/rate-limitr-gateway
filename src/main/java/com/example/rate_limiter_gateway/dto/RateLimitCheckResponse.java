package com.example.rate_limiter_gateway.dto;

public record RateLimitCheckResponse(
                boolean allowed,
                int capacity,
                int remaining,
                long resetAt) {
}
