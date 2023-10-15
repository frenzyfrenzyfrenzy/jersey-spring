package com.svintsov.rest;

import static com.svintsov.rest.ExceptionUtils.createException;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Path("/home")
public class HomeController {

    @Autowired
    private ThreadPoolExecutor clientRequestExecutor;

    @Autowired
    private BlockingQueue<String> availableMessages;

    @GET
    @Path("/suspended")
    @Produces(MediaType.APPLICATION_JSON)
    public void suspended(HttpServletRequest request, @Suspended AsyncResponse response) {
        log.info("Suspending the request in the controller");
        response.resume("suspended");
    }

    @GET
    @Path("/startAsync")
    @Produces(MediaType.APPLICATION_JSON)
    public void startAsync(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
        log.info("Suspending the sync request");
        PrintWriter responseWriter = response.getWriter();
        AsyncContext asyncContext = request.startAsync();
        asyncContext.start(() -> {
            log.info("Doing something asynchronous");
            responseWriter.println("DONE");
            responseWriter.flush();
            asyncContext.complete();
        });
        log.info("Leaving controller method, will continue asynchronously");
    }

    @GET
    @Path("/suspendedErrors")
    @Produces(MediaType.APPLICATION_JSON)
    public void suspendedErrors(@QueryParam("mode") String mode, @Suspended AsyncResponse response) {
        log.info("Gonna throw error from suspended request");
        switch (mode) {
            case "SYNC" -> throw createException(Response.Status.UNAUTHORIZED, "SYNC ERROR");
            case "ASYNC" -> response.resume(createException(Response.Status.SERVICE_UNAVAILABLE, "ASYNC ERROR"));
            case "EXEC" -> clientRequestExecutor.submit(() -> {
                log.info("responding from the executor");
                return response.resume(new BaseResponseDTO("EXECUTED"));
            });
        }
    }

    @GET
    @Path("/take")
    @Produces(MediaType.APPLICATION_JSON)
    public void take(@Suspended AsyncResponse response) {

        response.setTimeout(5, TimeUnit.SECONDS);
        response.setTimeoutHandler(asyncResponse -> {
            log.warn("Response timed out");
            asyncResponse.resume(new BaseResponseDTO("TIMEOUT"));
        });

        clientRequestExecutor.submit(() -> {
            try {
                log.info("Waiting for the message on the executor");
                String message = availableMessages.take();
                log.info("Message received, continue");
                boolean resumed = response.resume(new BaseResponseDTO(message));
                if (!resumed) {
                    log.warn("Response has already been timed out, message will be discarded");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GET
    @Path("/put")
    @Produces(MediaType.APPLICATION_JSON)
    public void put(@QueryParam("message") String message) throws InterruptedException {
        log.info("Putting message {} to the queue", message);
        availableMessages.put(message);
    }

    @GET
    @Path("/completionStage")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<BaseResponseDTO> completionStage() {
        log.info("Connection accepted");
        return supplyAsync(() -> {
            log.info("Starting a long running task");
            try {
                Thread.sleep(2000);
                log.info("Long running task is done, returning result");
                return new BaseResponseDTO("DONE");
            } catch (InterruptedException e) {
                log.warn("Interrupted during the task");
                throw new RuntimeException(e);
            }
        }, clientRequestExecutor);
    }
}
