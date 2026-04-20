package com.agileactors.pitfalls.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    @Value("${app.worker.pool.size:10}")
    private int workerPoolSize;

    @Bean(destroyMethod = "shutdown")
    public KeyedWorkerPool workerPool() {
        return new KeyedWorkerPool(workerPoolSize);
    }

    @Bean(name = "ackExecutor", destroyMethod = "shutdown")
    public ExecutorService ackExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
