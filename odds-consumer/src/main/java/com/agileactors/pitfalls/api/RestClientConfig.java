package com.agileactors.pitfalls.api;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties(ApiProperties.class)
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(ApiProperties properties) {
        return RestClient.builder()
            .baseUrl(properties.baseUrl())
            .build();
    }
}
