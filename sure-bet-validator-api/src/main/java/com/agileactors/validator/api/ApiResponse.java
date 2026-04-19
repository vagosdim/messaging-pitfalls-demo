package com.agileactors.validator.api;

import java.time.Instant;

public record ApiResponse(boolean success, String message, Instant timestamp) {

    public static ApiResponse ok(String message) {
        return new ApiResponse(true, message, Instant.now());
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, Instant.now());
    }
}

