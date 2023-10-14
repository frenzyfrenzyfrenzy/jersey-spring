package com.svintsov.rest;

import static com.svintsov.rest.ExceptionUtils.createException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Path("/home")
public class HomeController {

    @Autowired
    private ThreadPoolExecutor clientRequestExecutor;

    @Autowired
    private BeanFactory beanFactory;

    @GET
    @Path("/suspend")
    @Produces(MediaType.APPLICATION_JSON)
    public void suspend(HttpServletRequest request, @Suspended AsyncResponse response) {
        log.info("Suspending the request in the controller");
        response.resume("suspended");
    }

    @GET
    @Path("/error")
    @Produces(MediaType.APPLICATION_JSON)
    public void error(@QueryParam("mode") String mode, @Suspended AsyncResponse response) {
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

}
