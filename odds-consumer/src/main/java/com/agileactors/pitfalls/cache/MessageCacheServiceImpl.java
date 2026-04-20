package com.agileactors.pitfalls.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageCacheServiceImpl implements MessageCacheService {

    private final Cache<String, Boolean> messageIdCache;

    public boolean isAlreadyProcessed(String messageId) {
        return messageIdCache.getIfPresent(messageId) != null;
    }

    public void markAsProcessed(String messageId) {
        messageIdCache.put(messageId, Boolean.TRUE);
    }
}
