package com.example.rate_limiter_gateway.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import org.springframework.util.StreamUtils;

import com.example.rate_limiter_gateway.dto.RateLimitResult;

@Repository
public class RedisRateLimiterRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // use RedisScript<List> (Lua returns a Redis array)
    private RedisScript<List> rateLimitScript;

    // Load the Lua script from src/main/resources/ratelimit.lua
    @Autowired
    public void loadScript() throws Exception {
        ClassPathResource resource = new ClassPathResource("ratelimit.lua");
        String lua = StreamUtils.copyToString(resource.getInputStream(),
                java.nio.charset.StandardCharsets.UTF_8);

        // Keep RedisScript typed as List (raw) — we'll cast the result safely later
        rateLimitScript = RedisScript.of(lua, List.class);
        System.out.println("✔ Lua rate limiter script loaded");
    }

    /**
     * Execute the Lua script atomically.
     * IMPORTANT: pass ARGV as separate varargs (capacity, refillRate, now).
     */
    public RateLimitResult consume(String key, int capacity, int refillRate) {

        long now = Instant.now().getEpochSecond();

        // Execute script: first param is KEYS (list), remaining are ARGV as separate
        // varargs
        @SuppressWarnings("unchecked")
        List<Long> result = (List<Long>) redisTemplate.execute(
                rateLimitScript,
                List.of(key), // KEYS
                String.valueOf(capacity), // ARGV[1]
                String.valueOf(refillRate), // ARGV[2]
                String.valueOf(now) // ARGV[3]
        );

        // Defensive null/size check — if Redis/script failed, fail-open (allow) or
        // choose behavior
        if (result == null || result.size() < 3) {
            // fail-open: allow request but report unlimited (adjust if you want
            // fail-closed)
            return new RateLimitResult(true, capacity, now + 1);
        }

        boolean allowed = (result.get(0) != 0L);
        int remaining = result.get(1).intValue();
        long resetAt = result.get(2);

        return new RateLimitResult(allowed, remaining, resetAt);
    }
}
