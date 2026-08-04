// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import com.microsoft.agentserver.api.PlatformHeaders;
import com.microsoft.agentserver.api.AgentServerVersion;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Spring filter that implements platform header handling for all HTTP responses:
 * <ul>
 *  <li><b>{@code x-request-id}</b> — echoes the client-provided request ID or generates one.
 *  Also placed in SLF4J MDC for structured logging correlation.</li>
 *  <li><b>{@code x-platform-server}</b> — identifies the server SDK stack
 *  (hosting version, protocol versions, language, runtime).</li>
 * </ul>
 * <p>
 * This filter runs at high priority to ensure headers are set before any
 * response body is written.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PlatformHeaderFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformHeaderFilter.class);
    private static final String MDC_REQUEST_ID = "requestId";

    /**
     * Request attribute the controller publishes for the filter to echo.
     */
    public static final String SESSION_ID_ATTR = "agent_session_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // Resolve request ID: use client-provided header, or generate a new one
        String requestId = request.getHeader(PlatformHeaders.REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Set on response immediately (before body write)
        response.setHeader(PlatformHeaders.REQUEST_ID, requestId);
        response.setHeader(PlatformHeaders.SERVER_VERSION, AgentServerVersion.getInstance().getHeaderValue());

        // Store in request attribute for downstream use (e.g., ResponseContext building)
        request.setAttribute(PlatformHeaders.REQUEST_ID, requestId);

        // Add to MDC for structured logging correlation
        MDC.put(MDC_REQUEST_ID, requestId);
        try {
            filterChain.doFilter(request, response);
            // after the controller runs, echo the resolved agent_session_id if it
            // published one to the request attribute. Set on response only if not yet
            // committed (typical for non-SSE; SSE controllers set the header inline).
            Object sid = request.getAttribute(SESSION_ID_ATTR);
            if (sid instanceof String s && !s.isEmpty() && !response.isCommitted()) {
                response.setHeader(PlatformHeaders.SESSION_ID, s);
            }
        } finally {
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}

