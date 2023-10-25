package com.svintsov.rest;

import static com.svintsov.rest.ExceptionUtils.createException;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.svintsov.service.RestClient;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.ManagedAsync;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
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

    @Autowired
    private RestClient restClient;

    @GET
    @Path("/suspended")
    @Produces(MediaType.APPLICATION_JSON)
    public void suspended(HttpServletRequest request, @Suspended AsyncResponse response) {
        log.info("Suspending the request in the controller");
        clientRequestExecutor.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("Resuming from the executor");
            response.resume("suspended");
        });
        log.info("Returning from the controller method");
    }

    @GET
    @Path("/startAsync")
    @Produces(MediaType.APPLICATION_JSON)
    public void startAsync(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
        log.info("Suspending the sync request");
        ServletOutputStream outputStream = response.getOutputStream();
        AsyncContext asyncContext = request.startAsync();
        clientRequestExecutor.submit(() -> {
            log.info("Resuming from the executor");
            try {
                outputStream.println("DONE");
                outputStream.flush();
                asyncContext.complete();
            } catch (Exception exception) {
                log.error("An error happened while flushing", exception);
            }
        });
        log.info("Leaving controller method, will continue asynchronously");
    }

    @GET
    @Path("/managedAsync")
    @ManagedAsync
    @Produces(MediaType.APPLICATION_JSON)
    public BaseResponseDTO managedAsync() {
        log.info("Starting execution in a managed thread");
        return new BaseResponseDTO("MANAGED");
    }

    @GET
    @Path("/suspendedErrors")
    @Produces(MediaType.APPLICATION_JSON)
    public void suspendedErrors(@QueryParam("mode") String mode, @Suspended AsyncResponse response) {
        log.info("Gonna throw error from suspended request");
        switch (mode) {
            case "THROW" -> throw createException(Response.Status.UNAUTHORIZED, "ERROR FROM THROW");
            case "RESUME" -> response.resume(createException(Response.Status.SERVICE_UNAVAILABLE, "ERROR FROM RESUME"));
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

    @POST
    @Path("/listenerBasedRead")
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> listenerBasedRead(@Context HttpServletRequest request) throws IOException {
        AsyncContext asyncContext = request.startAsync();

        ServletInputStream inputStream = request.getInputStream();
        AccumulatingReadListener accumulatingReadListener = new AccumulatingReadListener(inputStream);
        inputStream.setReadListener(accumulatingReadListener);

        CompletableFuture<String> responseFuture = accumulatingReadListener.getRequestPromise()
                .publishOn(Schedulers.parallel())
                .doOnNext(s -> log.info("Request received in controller: {}", s))
                .toFuture();
        log.info("Future is constructed, leaving controller method");
        return responseFuture;
    }

    @GET
    @Path("/time")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<BaseResponseDTO> getCurrentTime() {
        return restClient.getCurrentTime()
                .thenApply(response -> {
                    log.info("Response is ready in netty thread, offloading next async operation to our scheduler");
                    return response;
                })
                .thenApplyAsync(response -> {
                    log.info("In our scheduler thread. Finalizing response");
                    return new BaseResponseDTO(response.readEntity(String.class));
                }, clientRequestExecutor);
    }
}
