// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import com.microsoft.agentserver.api.Observability;
import com.microsoft.agentserver.api.PlatformHeaders;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JAX-RS filter that provides structured inbound request logging and OpenTelemetry
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
 * This filter is always active (not gated by environment variables) because production
 * observability requires consistent telemetry. It corresponds to the .NET
 * {@code InboundRequestLoggingMiddleware} + trace instrumentation.
 */
@Provider
public class InboundRequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundRequestLoggingFilter.class);

    private static final String START_TIME_PROPERTY = "agentserver.startTimeNanos";
    private static final String SPAN_PROPERTY = "agentserver.span";
    private static final String SCOPE_PROPERTY = "agentserver.scope";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Record start time
        requestContext.setProperty(START_TIME_PROPERTY, System.nanoTime());

        // Extract headers for trace context propagation
        Map<String, String> headers = extractHeaders(requestContext);

        // Start a SERVER span with propagated context
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String spanName = method + " /" + path;

        Span span = Observability.startServerSpan(spanName, headers);
        Scope scope = span.makeCurrent();

        requestContext.setProperty(SPAN_PROPERTY, span);
        requestContext.setProperty(SCOPE_PROPERTY, scope);

        // Set MDC for structured logging correlation
        String requestId = headers.getOrDefault(PlatformHeaders.REQUEST_ID, "");
        String traceId = span.getSpanContext().getTraceId();
        String spanId = span.getSpanContext().getSpanId();

        MDC.put("requestId", requestId);
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);

        // Set attributes on the span
        span.setAttribute("http.request.method", method);
        span.setAttribute("url.path", "/" + path);

        // Log client request ID if present (Azure SDK correlation)
        String clientRequestId = headers.get(PlatformHeaders.CLIENT_REQUEST_ID);
        if (clientRequestId != null && !clientRequestId.isEmpty()) {
            span.setAttribute("az.client_request_id", clientRequestId);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Calculate duration
        Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
        long durationMs = 0;
        if (startTime != null) {
            durationMs = (System.nanoTime() - startTime) / 1_000_000;
        }

        String method = requestContext.getMethod();
        String path = "/" + requestContext.getUriInfo().getPath();
        int statusCode = responseContext.getStatus();
        String requestId = MDC.get("requestId");

        // Structured log — always emitted at INFO level
        LOGGER.info("{} {} → {} ({}ms) [request_id={}]",
            method, path, statusCode, durationMs,
            requestId != null ? requestId : "-");

        // End the span
        Span span = (Span) requestContext.getProperty(SPAN_PROPERTY);
        if (span != null) {
            Observability.setHttpAttributes(span, method, path, statusCode);
            span.setAttribute("http.response.duration_ms", durationMs);
            span.end();
        }

        // Close the scope
        Scope scope = (Scope) requestContext.getProperty(SCOPE_PROPERTY);
        if (scope != null) {
            scope.close();
        }

        // Clear MDC
        MDC.remove("requestId");
        MDC.remove("traceId");
        MDC.remove("spanId");
    }

    private Map<String, String> extractHeaders(ContainerRequestContext requestContext) {
        Map<String, String> headers = new LinkedHashMap<>();
        requestContext.getHeaders().forEach((key, values) -> {
            if (values != null && !values.isEmpty()) {
                headers.put(key.toLowerCase(), values.get(0));
            }
        });
        return headers;
    }
}

