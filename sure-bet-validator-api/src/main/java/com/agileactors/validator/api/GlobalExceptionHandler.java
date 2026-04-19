package com.agileactors.validator.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(HttpMessageNotReadableException e) {
        log.warn("Invalid request body: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid request body"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(ApiResponse.error("Internal server error"));
    }
}

