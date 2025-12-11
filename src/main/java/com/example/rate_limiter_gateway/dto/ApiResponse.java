package com.example.rate_limiter_gateway.dto;

public record ApiResponse<T>(
        boolean success,
        T data,
        long timestamp) {
    public ApiResponse(T data) {
        this(true, data, System.currentTimeMillis());
    }
}
