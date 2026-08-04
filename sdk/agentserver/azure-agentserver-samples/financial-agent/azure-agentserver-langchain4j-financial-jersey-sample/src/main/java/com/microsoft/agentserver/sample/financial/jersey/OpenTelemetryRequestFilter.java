package com.microsoft.agentserver.sample.financial.jersey;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import org.slf4j.Logger;

/**
 * Jersey filter that creates OpenTelemetry SERVER spans for every HTTP request.
 * When the Application Insights Java agent is attached, these spans are exported
 * as request telemetry. Without the agent, they are harmless no-ops.
 */
public class OpenTelemetryRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OpenTelemetryRequestFilter.class);

    private static final String SPAN_PROPERTY = "otel.span";
    private static final String SCOPE_PROPERTY = "otel.scope";

    // Resolved configuration — set by Main before the server starts
    public static String agentName = "unknown";
    public static String modelDeploymentName = "unknown";
    public static String projectEndpoint = "unknown";
    public static String openAiEndpoint = "unknown";
    public static String identityEndpoint = "not set";

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("agent-server");

    @Override
    public void filter(ContainerRequestContext request) {
        String method = request.getMethod();
        String path = "/" + request.getUriInfo().getPath();

        LOGGER.info("Incoming request: {} {} | Agent={}, Model={}, ProjectEndpoint={}, OpenAIEndpoint={}, IdentityEndpoint={}",
            method, path, agentName, modelDeploymentName, projectEndpoint, openAiEndpoint, identityEndpoint);

        Span span = tracer.spanBuilder(method + " " + path)
            .setSpanKind(SpanKind.SERVER)
            .setAttribute("http.request.method", method)
            .setAttribute("url.path", path)
            .startSpan();

        Scope scope = span.makeCurrent();

        request.setProperty(SPAN_PROPERTY, span);
        request.setProperty(SCOPE_PROPERTY, scope);
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        try {
            //String body = new String(request.getEntityStream().readAllBytes());
            LOGGER.debug("Incoming response: {}", request.getUriInfo().getPath());
        } catch (Exception e) {
            LOGGER.error("Error reading request entity", e);
        }
        Scope scope = (Scope) request.getProperty(SCOPE_PROPERTY);
        Span span = (Span) request.getProperty(SPAN_PROPERTY);

        if (span != null) {
            span.setAttribute("http.response.status_code", response.getStatus());
            if (response.getStatus() >= 400) {
                span.setStatus(StatusCode.ERROR);
            }
            span.end();
        }

        if (scope != null) {
            scope.close();
        }
    }
}

