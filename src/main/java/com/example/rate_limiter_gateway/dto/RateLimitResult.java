package com.example.rate_limiter_gateway.dto;

public record RateLimitResult(boolean allowed, int remaining, long resetAt) {
}
