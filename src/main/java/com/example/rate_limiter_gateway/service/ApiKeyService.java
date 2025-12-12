package com.example.rate_limiter_gateway.service;

import org.springframework.stereotype.Service;

import com.example.rate_limiter_gateway.entity.ApiKeyEntity;
import com.example.rate_limiter_gateway.repositories.ApiKeyRepository;

@Service
public class ApiKeyService {

    private final ApiKeyRepository repo;

    public ApiKeyService(ApiKeyRepository repo) {
        this.repo = repo;
    }

    public ApiKeyEntity validate(String key) {
        com.example.rate_limiter_gateway.entity.ApiKeyEntity apiKey = repo.findByApiKey(key);

        if (apiKey == null || !"ACTIVE".equals(apiKey.getStatus())) {
            return null;
        }

        return apiKey;
    }
}
