package com.svintsov.service;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.CompletableFuture;

public class RestClient implements InitializingBean {

    private static final String REST_URI = "http://worldtimeapi.org/api/timezone/Europe/Zurich";

    private Client client;

    @Override
    public void afterPropertiesSet() throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new NettyConnectorProvider());
        client = ClientBuilder.newClient(clientConfig);
    }

    public CompletableFuture<Response> getCurrentTime() {
        return (CompletableFuture<Response>) client.target(REST_URI)
                .request()
                .async()
                .get();
    }

}
