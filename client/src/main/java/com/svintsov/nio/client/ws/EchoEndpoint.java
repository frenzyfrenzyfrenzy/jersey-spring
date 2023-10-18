package com.svintsov.nio.client.ws;

import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoEndpoint extends Endpoint {

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        log.info("Opening websocket connection...");
        session.addMessageHandler(new MessageHandler.Partial<String>() {
            @Override
            public void onMessage(String partialMessage, boolean last) {
                log.info("Websocket message received: {}", partialMessage);
            }
        });
    }

    @Override
    public void onError(Session session, Throwable thr) {
        log.info("Error when opening websocket session");
        super.onError(session, thr);
    }
}
