package com.svintsov.nio.client.config;

import com.svintsov.nio.client.ws.ClientEndpoint;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
public class WebSocketClient {

    @Autowired
    private WebSocketContainer webSocketContainer;

    private static final String WEBSOCKET_SERVER_URL = "ws://server:8080/nio-test-server/websocket/echo";

    public void connectToEchoServer(String username, Consumer<Session> onOpen, BiConsumer<String, Session> onMessage) {
        try {
            webSocketContainer.connectToServer(
                    new ClientEndpoint(username, onMessage, onOpen),
                    ClientEndpointConfig.Builder.create().build(),
                    URI.create(WEBSOCKET_SERVER_URL));
        } catch (Exception exception) {
            throw new RuntimeException(String.format("Cannot connect to %s", WEBSOCKET_SERVER_URL), exception);
        }
    }

}
