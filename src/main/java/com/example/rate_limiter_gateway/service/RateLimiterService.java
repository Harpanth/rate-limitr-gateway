package com.example.rate_limiter_gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.rate_limiter_gateway.dto.RateLimitCheckResponse;
import com.example.rate_limiter_gateway.dto.RateLimitResult;
import com.example.rate_limiter_gateway.entity.ApiKeyEntity;
import com.example.rate_limiter_gateway.config.ApiKeyPlanConfig;
import com.example.rate_limiter_gateway.config.RateLimitConfig;
import com.example.rate_limiter_gateway.repositories.ApiKeyRepository;
import com.example.rate_limiter_gateway.repositories.RedisRateLimiterRepository;

@Service
public class RateLimiterService {

    private final RateLimitConfig config;
    private final ApiKeyPlanConfig planConfig;
    private final RedisRateLimiterRepository redisRepo;

    @Autowired
    private ApiKeyRepository apiKeyRepo;

    public RateLimiterService(RateLimitConfig config, ApiKeyPlanConfig planConfig,
            RedisRateLimiterRepository redisRepo) {
        this.config = config;
        this.planConfig = planConfig;
        this.redisRepo = redisRepo;
    }

    private LimitValues getLimitForRoute(String apiKey, String route) {

        String plan = planConfig.getApiKeys().get(apiKey).getPlan();
        ApiKeyPlanConfig.LimitConfig planLimit = planConfig.getPlans().get(plan).get(route);

        RateLimitConfig.LimitConfig defaultLimit = config.getLimits().get("default");

        if (planLimit == null) {
            return new LimitValues(defaultLimit.getCapacity(), defaultLimit.getRefillRate());
        }

        return new LimitValues(planLimit.getCapacity(), planLimit.getRefillRate());
    }

    private record LimitValues(int capacity, int refillRate) {
    }

    public RateLimitCheckResponse checkRateLimit(String apiKey, String route) {

        LimitValues limits = getLimitForRoute(apiKey, route);

        String redisKey = "bucket:" + apiKey + ":" + route;

        RateLimitResult redisResult = redisRepo.consume(
                redisKey,
                limits.capacity(),
                limits.refillRate());

        return new RateLimitCheckResponse(
                redisResult.allowed(),
                limits.capacity(),
                redisResult.remaining(),
                redisResult.resetAt());
    }

    public RateLimitCheckResponse checkIpLimit(String ip) {

        int capacity = 20;
        int refillRate = 1; // or any non-zero rate

        String redisKey = "ip_bucket:" + ip;

        RateLimitResult result = redisRepo.consume(redisKey, capacity, refillRate);

        return new RateLimitCheckResponse(
                result.allowed(),
                capacity,
                result.remaining(),
                result.resetAt());
    }

    public RateLimitCheckResponse checkApiKeyLimit(String apiKey, String route) {

        LimitValues limits = getLimitForRoute(apiKey, route);

        String redisKey = "apikey_buckey:" + apiKey + ":" + route;

        RateLimitResult result = redisRepo.consume(
                redisKey,
                limits.capacity(),
                limits.refillRate());

        return new RateLimitCheckResponse(
                result.allowed(),
                limits.capacity(),
                result.remaining(),
                result.resetAt());

    }

    public boolean isValidApiKey(String apiKey) {
        ApiKeyEntity key = apiKeyRepo.findByApiKey(apiKey);

        return key != null && "ACTIVE".equalsIgnoreCase(key.getStatus());
    }

}
