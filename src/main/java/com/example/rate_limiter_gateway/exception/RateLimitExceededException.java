package com.example.rate_limiter_gateway.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("Too many requests — rate limit exceeded");
    }
}
