package com.example.rate_limiter_gateway.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitConfig {

    private Map<String, LimitConfig> limits;

    public Map<String, LimitConfig> getLimits() {
        return limits;
    }

    public void setLimits(Map<String, LimitConfig> limits) {
        this.limits = limits;
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
