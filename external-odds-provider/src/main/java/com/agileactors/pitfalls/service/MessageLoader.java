package com.agileactors.pitfalls.service;

import com.agileactors.pitfalls.model.OddsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageLoader {

    private static final String PATH_PREFIX = "classpath:messages/";

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public OddsMessage loadMessage(String filename) throws IOException {
        String path = PATH_PREFIX + filename;
        Resource resource = resourceLoader.getResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(content, OddsMessage.class);
        }
    }

}
