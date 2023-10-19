package com.svintsov.ws;

import com.svintsov.service.HotTimerPublisher;
import jakarta.inject.Inject;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
@ServerEndpoint(value =  "/websocket/echo")
public class EchoServerEndpoint {

    @Inject
    private HotTimerPublisher timerPublisher;

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("Websocket message received: {}", message);
        timerPublisher.asFlux().subscribe(currentTime -> {
            try {
                session.getBasicRemote().sendText(currentTime);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        log.error("Error when opening websocket session", thr);
    }

}
