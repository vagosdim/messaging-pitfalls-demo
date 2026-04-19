package com.agileactors.pitfalls.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ProducerResponse> handleFileNotFound(FileNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ProducerResponse.error(e.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ProducerResponse> handleIOException(IOException e) {
        log.error("IO error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(ProducerResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProducerResponse> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getParameterName());
        return ResponseEntity.badRequest()
            .body(ProducerResponse.error("Missing required parameter: " + e.getParameterName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProducerResponse> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(ProducerResponse.error("Internal server error"));
    }
}
