package com.example.rate_limiter_gateway.filter;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.rate_limiter_gateway.exception.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Set<String> VALID_KEYS = Set.of("test-key-123", "demo-key-abc");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");

        // Missing API key
        if (apiKey == null || apiKey.isBlank()) {
            writeError(response, "API key is missing", 401);
            return;
        }

        // Invalid API key
        if (!VALID_KEYS.contains(apiKey)) {
            writeError(response, "Invalid API key", 403);
            return;
        }

        // Valid key → continue
        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, String message, int status) throws IOException {
        ErrorResponse error = new ErrorResponse(message, status, System.currentTimeMillis());

        response.setStatus(status);
        response.setContentType("application/json");

        String json = String.format("{\"message\":\"%s\",\"status\":%d,\"timestamp\":%d}",
                error.getMessage(), error.getStatus(), error.getTimestamp());

        response.getWriter().write(json);
    }
}
