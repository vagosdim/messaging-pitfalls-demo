package com.agileactors.pitfalls.broker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMqProperties {

    private String queueName;
    private String exchangeName;
    private String routingKey;
}
