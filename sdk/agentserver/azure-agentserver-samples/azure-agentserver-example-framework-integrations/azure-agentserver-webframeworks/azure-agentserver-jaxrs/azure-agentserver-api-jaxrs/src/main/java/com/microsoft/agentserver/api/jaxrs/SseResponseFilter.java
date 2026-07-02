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
        if (mediaType != null && MediaType.SERVER_SENT_EVENTS_TYPE.isCompatible(mediaType)) {
            // Per the SSE Response Headers contract, declare an explicit charset.
            responseContext.getHeaders().putSingle("Content-Type", "text/event-stream; charset=utf-8");
            responseContext.getHeaders().putSingle("X-Accel-Buffering", "no");
            responseContext.getHeaders().putSingle("Cache-Control", "no-cache");
            responseContext.getHeaders().putSingle("Connection", "keep-alive");
        }
    }
}

