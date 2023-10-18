package com.svintsov.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class ExceptionUtils {

    public static WebApplicationException createException(Response.Status status, String message) {
        return new WebApplicationException(
                Response.status(status)
                        .entity(new BaseResponseDTO(message))
                        .build()
        );
    }

}
