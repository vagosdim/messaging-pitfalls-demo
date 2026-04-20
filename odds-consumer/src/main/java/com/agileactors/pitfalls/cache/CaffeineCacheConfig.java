package com.agileactors.pitfalls.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine")
public class CaffeineCacheConfig {

    @Value("${app.cache.message-id.duration-minutes:10}")
    private int expiryMinutes;

    @Value("${app.cache.message-id.max-size:10000}")
    private long maxSize;

    @Bean
    public Cache<String, Boolean> messageIdCache() {
        return Caffeine.newBuilder()
            .expireAfterWrite(expiryMinutes, TimeUnit.MINUTES)
            .maximumSize(maxSize)
            .build();
    }
}
