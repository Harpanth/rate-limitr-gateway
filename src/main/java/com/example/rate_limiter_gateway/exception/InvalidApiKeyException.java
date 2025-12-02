package com.example.rate_limiter_gateway.exception;

public class InvalidApiKeyException extends RuntimeException {

    public InvalidApiKeyException() {
        super("Invalid API key");
    }
}
