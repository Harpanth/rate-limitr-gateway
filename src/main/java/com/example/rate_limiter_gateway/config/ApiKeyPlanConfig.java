package com.example.rate_limiter_gateway.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.example.rate_limiter_gateway.config.RateLimitConfig.LimitConfig;

@Configuration
@ConfigurationProperties(prefix = "ratelimit")
public class ApiKeyPlanConfig {

    private Map<String, ApiKeyMapping> apiKeys;
    private Map<String, Map<String, LimitConfig>> plans;

    public Map<String, ApiKeyMapping> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(Map<String, ApiKeyMapping> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public Map<String, Map<String, LimitConfig>> getPlans() {
        return plans;
    }

    public void setPlans(Map<String, Map<String, LimitConfig>> plans) {
        this.plans = plans;
    }

    public static class ApiKeyMapping {

        private String plan;

        public String getPlan() {
            return plan;
        }

        public void setPlan(String plan) {
            this.plan = plan;
        }
    }

    public static class LimitConfig {
        private int capacity;
        private int refillRate;

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getRefillRate() {
            return refillRate;
        }

        public void setRefillRate(int refillRate) {
            this.refillRate = refillRate;
        }
    }
}
