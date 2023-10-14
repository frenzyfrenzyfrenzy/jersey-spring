package com.svintsov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Bean
    public ThreadPoolExecutor clientRequestExecutor() {
        return new ThreadPoolExecutor(2, 5, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(5));
    }

    @Bean
    public BlockingQueue<String> availableMessages() {
        return new ArrayBlockingQueue<>(10);
    }

}
