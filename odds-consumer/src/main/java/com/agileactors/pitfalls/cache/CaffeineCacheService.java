package com.agileactors.pitfalls.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine")
public class CaffeineCacheService implements MessageCacheService {

    private final Cache<String, Boolean> messageIdCache;

    public boolean isAlreadyProcessed(String messageId) {
        return messageIdCache.getIfPresent(messageId) != null;
    }

    public void markAsProcessed(String messageId) {
        log.info("Storing messageId={} to Caffeine", messageId);
        messageIdCache.put(messageId, Boolean.TRUE);
    }
}
