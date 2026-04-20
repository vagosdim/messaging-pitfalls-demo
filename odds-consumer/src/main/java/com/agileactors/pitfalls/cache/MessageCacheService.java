package com.agileactors.pitfalls.cache;

/**
 * P6/P7 - Idempotency Guard <br>
 * Check if a message was already processed to avoid duplicate processing
 */
public interface MessageCacheService {

    /**
     * Check if message was already processed
     * @param messageId the unique identifier of the message
     * @return true if message was already processed, false otherwise
     */
    boolean isAlreadyProcessed(String messageId);

    /**
     * Mark message as processed to prevent duplicate processing
     * @param messageId the unique identifier of the message
     */
    void markAsProcessed(String messageId);

}
