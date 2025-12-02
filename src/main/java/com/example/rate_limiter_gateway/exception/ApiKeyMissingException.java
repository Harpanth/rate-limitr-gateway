package com.example.rate_limiter_gateway.exception;

public class ApiKeyMissingException extends RuntimeException {

    public ApiKeyMissingException() {
        super("API key is missing");
    }
}
