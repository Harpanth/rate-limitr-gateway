package com.example.rate_limiter_gateway.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    // Holds a bucket per API key
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    // Config: each API key gets 5 requests per 10 seconds
    private final int CAPACITY = 5;
    private final int REFILL_RATE_PER_SECOND = 1;

    public synchronized boolean isAllowed(String apiKey) {
        TokenBucket bucket = buckets.computeIfAbsent(
                apiKey,
                key -> new TokenBucket(CAPACITY, REFILL_RATE_PER_SECOND));

        return bucket.tryConsume();
    }

    // Inner class holding token bucket logic
    private static class TokenBucket {
        private int tokens;
        private final int capacity;
        private final int refillRatePerSecond;
        private long lastRefillTime;

        public TokenBucket(int capacity, int refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
            this.tokens = capacity;
            this.lastRefillTime = System.nanoTime();
        }

        public boolean tryConsume() {
            refill();

            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsedSeconds = (now - lastRefillTime) / 1_000_000_000;

            if (elapsedSeconds > 0) {
                int tokensToAdd = (int) (elapsedSeconds * refillRatePerSecond);
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}
