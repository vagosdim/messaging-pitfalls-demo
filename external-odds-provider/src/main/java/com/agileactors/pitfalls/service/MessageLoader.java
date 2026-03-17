package com.agileactors.pitfalls.service;

import com.agileactors.pitfalls.model.OddsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageLoader {

    private final ObjectMapper objectMapper;

    public OddsMessage loadMessage(String filename) throws IOException {
        String path = "messages/" + filename;
        ClassPathResource resource = new ClassPathResource(path);
        String content = new String(Files.readAllBytes(Paths.get(resource.getFile().getPath())));
        return objectMapper.readValue(content, OddsMessage.class);
    }
}
