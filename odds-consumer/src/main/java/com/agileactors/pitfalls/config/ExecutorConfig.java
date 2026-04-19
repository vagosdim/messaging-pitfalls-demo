package com.agileactors.pitfalls.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    @Bean(name = "workerPool", destroyMethod = "shutdown")
    public ExecutorService workerPool() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "ackExecutor", destroyMethod = "shutdown")
    public ExecutorService ackExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
