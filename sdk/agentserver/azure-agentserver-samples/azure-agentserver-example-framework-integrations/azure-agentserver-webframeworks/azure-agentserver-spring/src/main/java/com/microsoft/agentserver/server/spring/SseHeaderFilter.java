// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring filter that adds anti-buffering headers to SSE responses.
 * <p>
 * Reverse proxies (nginx, Azure Front Door, Foundry proxy, etc.) buffer
 * response bodies by default. For Server-Sent Events to stream correctly
 * through these proxies, the following headers must be present:
 * <ul>
 *   <li>{@code X-Accel-Buffering: no} — disables nginx proxy buffering</li>
 *   <li>{@code Cache-Control: no-cache} — prevents intermediate caching</li>
 *   <li>{@code Connection: keep-alive} — maintains the persistent connection</li>
 * </ul>
 * Without these headers, the proxy accumulates the entire SSE stream before
 * forwarding it to the client, causing the client to see an infinite spinner.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SseHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        filterChain.doFilter(request, response);

        // After the response is committed, check if it's an SSE response
        String contentType = response.getContentType();
        if (contentType != null && contentType.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            // Per the SSE Response Headers contract, declare an explicit charset.
            response.setHeader("Content-Type", "text/event-stream; charset=utf-8");
            response.setHeader("X-Accel-Buffering", "no");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
        }
    }
}

