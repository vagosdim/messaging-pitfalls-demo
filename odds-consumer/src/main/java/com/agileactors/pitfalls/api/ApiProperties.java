package com.agileactors.pitfalls.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.external-api")
public record ApiProperties(String baseUrl) {

}
