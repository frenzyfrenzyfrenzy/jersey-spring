package com.svintsov.nio.client.rest;

import com.svintsov.nio.client.config.WebSocketClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.Session;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
@Path("/home")
public class HomeController {

    @Autowired
    private WebSocketClient webSocketClient;

    @GET
    @Path("/startWebsocket")
    @Produces(MediaType.APPLICATION_JSON)
    public void startWebsocket(@Context HttpServletRequest request, @QueryParam("username") String username) {
        log.info("Initializing websocket session for {}", username);
        webSocketClient.connectToEchoServer(
                username,
                session -> {
                    try {
                        session.getBasicRemote().sendText("Hello from client");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                (message, session) -> {
                    log.info("Message received: {}", message);
                });
    }

}
