// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS response filter that adds anti-buffering headers to SSE responses.
 * <p>
 * Reverse proxies (nginx, Azure Front Door, Foundry proxy, etc.) buffer
 * response bodies by default. For Server-Sent Events to stream correctly
 * through these proxies, the following headers must be present:
 * <ul>
 *   <li>{@code X-Accel-Buffering: no} — disables nginx proxy buffering</li>
 *   <li>{@code Cache-Control: no-cache} — prevents intermediate caching</li>
 * </ul>
 * Without these headers, the proxy accumulates the entire SSE stream before
 * forwarding it to the client, causing the client to see an infinite spinner.
 */
@Provider
public class SseResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        MediaType mediaType = responseContext.getMediaType();
        boolean isSse = mediaType != null && MediaType.SERVER_SENT_EVENTS_TYPE.isCompatible(mediaType);

        // The RoutingFilter reroutes stream requests to SSE sub-resources that @Produces both
        // text/event-stream and application/json (so a client sending Accept: application/json does not
        // 406). When negotiation then picks application/json for a successful stream, the flag tells us the
        // response is still SSE. Error responses (4xx/5xx) from these endpoints are JSON, so only coerce
        // successful responses — otherwise we'd force text/event-stream onto a JSON error body.
        boolean sseRouted = Boolean.TRUE.equals(requestContext.getProperty(RoutingFilter.SSE_ROUTED_PROPERTY))
            && responseContext.getStatus() >= 200 && responseContext.getStatus() < 300;

        if (isSse || sseRouted) {
            // Per the SSE Response Headers contract, declare an explicit charset.
            responseContext.getHeaders().putSingle("Content-Type", "text/event-stream; charset=utf-8");
            responseContext.getHeaders().putSingle("X-Accel-Buffering", "no");
            responseContext.getHeaders().putSingle("Cache-Control", "no-cache");
            responseContext.getHeaders().putSingle("Connection", "keep-alive");
        }
    }
}

