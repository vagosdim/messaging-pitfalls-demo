package com.agileactors.pitfalls.cache;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.cache.type", havingValue = "redis")
public class RedisCacheService implements MessageCacheService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "messageId:";

    @Value("${app.cache.message-id.duration-minutes:10}")
    private long expiryMinutes;

    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAlreadyProcessed(String messageId) {
        Boolean exists = redisTemplate.hasKey(PREFIX + messageId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void markAsProcessed(String messageId) {
        log.info("Storing messageId={} to Redis", messageId);
        redisTemplate.opsForValue()
            .set(PREFIX + messageId, String.valueOf(Boolean.TRUE), expiryMinutes, TimeUnit.MINUTES);
    }
}

