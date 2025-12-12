package com.example.rate_limiter_gateway.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import com.example.rate_limiter_gateway.entity.ApiKey;
import com.example.rate_limiter_gateway.entity.ApiKeyEntity;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, String> {

    ApiKeyEntity findByApiKey(String apiKey);
}
