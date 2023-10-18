package com.svintsov.nio.client.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/home")
public class HomeController {

    @GET
    @Path("/echo")
    @Produces(MediaType.APPLICATION_JSON)
    public void suspended(@Context HttpServletRequest request) {
        log.info("Here in echo controller");
    }

}
