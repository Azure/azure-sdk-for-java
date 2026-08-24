package com.microsoft.agentserver.server.jersey;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Filters {

    private static final Boolean LOG_REQUESTS = Boolean.parseBoolean(System.getenv().getOrDefault("CA_LOG_REQUESTS", "false"));

    private static final Logger LOGGER = LoggerFactory.getLogger(Filters.class);

    @Provider
    public static class RequestLoggingFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (!LOG_REQUESTS) {
                return;
            }
            LOGGER.info("Incoming request: {} {} from {}",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                requestContext.getHeaders().getFirst("X-Forwarded-For"));
        }
    }

    @Provider
    public static class ResponseLoggingFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, jakarta.ws.rs.container.ContainerResponseContext responseContext) throws IOException {
            if (!LOG_REQUESTS) {
                return;
            }
            LOGGER.info("Outgoing response: {} for {} {}",
                responseContext.getStatus(),
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri());
        }
    }

    @Provider
    public static class CorsFilter implements ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        }
    }
}
