package com.example.rate_limiter_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/redis-test")
    public String testRedis() {
        redisTemplate.opsForValue().set("test-key", "hello");
        String value = (String) redisTemplate.opsForValue().get("test-key");
        return "Redis says: " + value;
    }
}
