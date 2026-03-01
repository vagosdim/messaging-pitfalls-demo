package com.agileactors.pitfalls.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.external-api")
public class ApiProperties {

    private String baseUrl;
}
