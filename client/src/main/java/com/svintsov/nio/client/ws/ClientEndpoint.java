package com.svintsov.nio.client.ws;

import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ClientEndpoint extends Endpoint {

    private final String currentUser;
    private final BiConsumer<String, Session> onMessage;
    private final Consumer<Session> onOpen;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        log.info("Opening websocket connection for {}", currentUser);
        session.addMessageHandler((MessageHandler.Whole<String>) message -> onMessage.accept(message, session));
        onOpen.accept(session);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        log.error("Error during websocket session for user {}", currentUser, thr);
        super.onError(session, thr);
    }
}
