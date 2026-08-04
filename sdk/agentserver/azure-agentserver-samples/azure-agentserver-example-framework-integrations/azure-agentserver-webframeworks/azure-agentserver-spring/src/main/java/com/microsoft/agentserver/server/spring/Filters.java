// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring filters for request/response logging and CORS support.
 */
public class Filters {

    private static final boolean LOG_REQUESTS =
        Boolean.parseBoolean(System.getenv().getOrDefault("CA_LOG_REQUESTS", "false"));

    private static final Logger LOGGER = LoggerFactory.getLogger(Filters.class);

    /**
     * Logs incoming request method, URI, and forwarded-for header.
     * Only active when the {@code CA_LOG_REQUESTS} environment variable is {@code "true"}.
     */
    public static class RequestLoggingFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            if (LOG_REQUESTS) {
                LOGGER.info("Incoming request: {} {} from {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getHeader("X-Forwarded-For"));
            }
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Logs outgoing response status for each request.
     * Only active when the {@code CA_LOG_REQUESTS} environment variable is {@code "true"}.
     */
    public static class ResponseLoggingFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            filterChain.doFilter(request, response);
            if (LOG_REQUESTS) {
                LOGGER.info("Outgoing response: {} for {} {}",
                    response.getStatus(),
                    request.getMethod(),
                    request.getRequestURI());
            }
        }
    }

    /**
     * Adds CORS headers to all responses.
     */
    public static class CorsFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            filterChain.doFilter(request, response);
        }
    }
}

