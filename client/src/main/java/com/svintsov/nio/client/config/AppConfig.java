package com.svintsov.nio.client.config;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan(basePackages = "com.svintsov.nio.client")
public class AppConfig {

    @Bean
    public ThreadPoolExecutor requestExecutor() {
        return new ThreadPoolExecutor(8, 8, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10));
    }

    @Bean
    public WebSocketContainer webSocketContainer() {
        return ContainerProvider.getWebSocketContainer();
    }

}
