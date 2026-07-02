// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import com.microsoft.agentserver.api.Observability;
import com.microsoft.agentserver.api.PlatformHeaders;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
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
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring filter that provides structured inbound request logging and OpenTelemetry
 * span creation for all HTTP requests.
 * <p>
 * For every request, this filter:
 * <ul>
 *   <li>Records the start time for duration calculation</li>
 *   <li>Extracts trace context from headers (W3C traceparent) and starts a SERVER span</li>
 *   <li>Sets MDC fields ({@code requestId}, {@code traceId}, {@code spanId}) for structured logging</li>
 *   <li>On response: logs method, path, status code, duration, and request ID</li>
 *   <li>Sets HTTP semantic convention attributes on the span</li>
 * </ul>
 * <p>
 * This filter runs after the {@link PlatformHeaderFilter} (which sets the request ID
 * on the request attribute), so the MDC request ID uses the resolved value.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class InboundRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        long startTime = System.nanoTime();

        // Extract headers for trace context propagation
        Map<String, String> headers = extractHeaders(request);

        // Start a SERVER span
        String method = request.getMethod();
        String path = request.getRequestURI();
        String spanName = method + " " + path;

        Span span = Observability.startServerSpan(spanName, headers);
        Scope scope = span.makeCurrent();

        // Set MDC from span context
        String traceId = span.getSpanContext().getTraceId();
        String spanId = span.getSpanContext().getSpanId();
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);

        // Set span attributes
        span.setAttribute("http.request.method", method);
        span.setAttribute("url.path", path);

        String clientRequestId = request.getHeader(PlatformHeaders.CLIENT_REQUEST_ID);
        if (clientRequestId != null && !clientRequestId.isEmpty()) {
            span.setAttribute("az.client_request_id", clientRequestId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            int statusCode = response.getStatus();
            String requestId = (String) request.getAttribute(PlatformHeaders.REQUEST_ID);

            // Structured log
            LOGGER.info("{} {} → {} ({}ms) [request_id={}]",
                method, path, statusCode, durationMs,
                requestId != null ? requestId : "-");

            // Complete the span
            Observability.setHttpAttributes(span, method, path, statusCode);
            span.setAttribute("http.response.duration_ms", durationMs);
            span.end();
            scope.close();

            // Clear MDC
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                headers.put(name.toLowerCase(), request.getHeader(name));
            }
        }
        return headers;
    }
}

