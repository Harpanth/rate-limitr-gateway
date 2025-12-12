package com.example.rate_limiter_gateway.exception;

import com.example.rate_limiter_gateway.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiKeyMissingException.class)
    public ResponseEntity<ErrorResponse> handleMissing(ApiKeyMissingException ex, HttpServletRequest req) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "API_KEY_MISSING",
                ex.getMessage(),
                req.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidApiKeyException ex, HttpServletRequest req) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "INVALID_API_KEY",
                ex.getMessage(),
                req.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleLimit(RateLimitExceededException ex, HttpServletRequest req) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage(),
                req.getRequestURI());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

}
