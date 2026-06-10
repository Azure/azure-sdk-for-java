// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import com.microsoft.agentserver.api.PlatformHeaders;
import com.microsoft.agentserver.api.AgentServerVersion;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * JAX-RS response filter that implements platform header handling for all HTTP responses:
 * <ul>
 *  <li><b>{@code x-request-id}</b> — echoes the client-provided request ID or generates one.</li>
 *  <li><b>{@code x-platform-server}</b> — identifies the server SDK stack.</li>
 *  <li><b>{@code x-agent-session-id}</b> — echoed when the resource method publishes the
 *  resolved session ID under the request-context property of the same name.</li>
 * </ul>
 */
@Provider
public class PlatformHeaderResponseFilter implements ContainerResponseFilter {

    private static final String MDC_REQUEST_ID = "requestId";

    /**
     * Request-context property the resource method publishes for the filter to echo.
     */
    public static final String SESSION_ID_PROPERTY = "agent_session_id";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Resolve request ID: use client-provided header, or generate a new one
        String requestId = requestContext.getHeaderString(PlatformHeaders.REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Set platform headers on response
        responseContext.getHeaders().putSingle(PlatformHeaders.REQUEST_ID, requestId);
        responseContext.getHeaders().putSingle(PlatformHeaders.SERVER_VERSION,
            AgentServerVersion.getInstance().getHeaderValue());

        // echo the resolved agent_session_id when the resource method published it.
        Object sessionIdAttr = requestContext.getProperty(SESSION_ID_PROPERTY);
        if (sessionIdAttr instanceof String sid && !sid.isEmpty()) {
            responseContext.getHeaders().putSingle(PlatformHeaders.SESSION_ID, sid);
        }

        // Add to MDC for structured logging
        MDC.put(MDC_REQUEST_ID, requestId);
    }
}

