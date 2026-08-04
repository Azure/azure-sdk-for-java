package com.microsoft.agentserver.server.jersey;

import com.microsoft.agentserver.api.ApiError;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Provider
public class ExceptionMappers {

    @Provider
    public static class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
        private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

        @Context
        private UriInfo uriInfo;

        @Override
        public Response toResponse(NotFoundException exception) {
            String failedUri = (uriInfo != null) ? uriInfo.getRequestUri().toString() : "unknown";
            LOGGER.warn("404 Not Found: {} (Request URI: {})", exception.getMessage(), failedUri);

            return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", ApiError.invalidRequest("Resource not found")))
                .build();
        }
    }

    @Provider
    public static class GenericExceptionMapper implements ExceptionMapper<Exception> {
        private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GenericExceptionMapper.class);

        @Context
        private UriInfo uriInfo;

        @Override
        public Response toResponse(Exception exception) {
            String failedUri = (uriInfo != null) ? uriInfo.getRequestUri().toString() : "unknown";
            LOGGER.error("Unhandled exception at {}: {}", failedUri, exception.getMessage(), exception);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", ApiError.serverError("An internal error occurred.")))
                .build();
        }
    }
}
