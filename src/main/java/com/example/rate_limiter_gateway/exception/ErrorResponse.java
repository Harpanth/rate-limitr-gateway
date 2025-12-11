package com.example.rate_limiter_gateway.exception;

public record ErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        long timestamp,
        String path) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(false, status, error, message, System.currentTimeMillis(), path);
    }
}
