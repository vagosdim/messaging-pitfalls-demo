package com.agileactors.pitfalls.api;

import java.time.Instant;

public record ProducerResponse(boolean success, String message, Instant timestamp) {

    public static ProducerResponse ok(String message) {
        return new ProducerResponse(true, message, Instant.now());
    }

    public static ProducerResponse error(String message) {
        return new ProducerResponse(false, message, Instant.now());
    }
}
