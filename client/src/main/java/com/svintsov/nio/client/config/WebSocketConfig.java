package com.svintsov.nio.client.config;

import com.svintsov.nio.client.ws.EchoEndpoint;
import jakarta.websocket.Endpoint;
import jakarta.websocket.server.ServerApplicationConfig;
import jakarta.websocket.server.ServerEndpointConfig;

import java.util.Set;

public class WebSocketConfig implements ServerApplicationConfig {

    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
        return Set.of(
                ServerEndpointConfig.Builder.create(EchoEndpoint.class, "/websocket/echo")
                        .build()
        );
    }

    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
        return scanned;
    }

}
