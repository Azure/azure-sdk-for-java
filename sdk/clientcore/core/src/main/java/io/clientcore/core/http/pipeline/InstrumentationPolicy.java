package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.observability.LibraryObservabilityOptions;
import io.clientcore.core.observability.ObservabilityOptions;
import io.clientcore.core.observability.ObservabilityProvider;
import io.clientcore.core.observability.Scope;
import io.clientcore.core.observability.tracing.Span;
import io.clientcore.core.observability.tracing.SpanKind;
import io.clientcore.core.observability.tracing.Tracer;
import io.clientcore.core.util.Context;

import java.util.Map;
import java.util.Set;

import static io.clientcore.core.observability.ObservabilityProvider.DISABLE_TRACING_KEY;

public class InstrumentationPolicy implements HttpPipelinePolicy {
    private static final LibraryObservabilityOptions LIBRARY_OPTIONS = new LibraryObservabilityOptions("clientcore")
        .setLibraryVersion("1.0.0")
        .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

    private static final String HTTP_REQUEST_METHOD = "http.request.method";
    private static final String HTTP_RESPONSE_STATUS_CODE = "http.response.status_code";
    private static final String SERVER_ADDRESS = "server.address";
    private static final String SERVER_PORT = "server.port";
    private static final String URL_FULL = "url.full";
    private static final String HTTP_REQUEST_RESEND_COUNT = "http.request.resend_count";
    private static final String USER_AGENT_ORIGINAL = "user_agent.original";

    private final Tracer tracer;
    private final Map<HttpHeaderName, String> requestHeaders;
    private final Map<HttpHeaderName, String> responseHeaders;

    public InstrumentationPolicy(ObservabilityOptions<?> options) {
        this(options, null, null);
    }

    public InstrumentationPolicy(ObservabilityOptions<?> options, Map<HttpHeaderName, String> requestHeaders, Map<HttpHeaderName, String> responseHeaders) {
        this.tracer = ObservabilityProvider.getInstance().getTracer(options, LIBRARY_OPTIONS);
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
    }

    @Override
    public Response<?> process(HttpRequest request, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled(request)) {
            return next.process();
        }

        Span span = tracer.spanBuilder(request.getHttpMethod().toString())
            .setSpanKind(SpanKind.CLIENT)
            .setParent(request.getRequestOptions().getContext())
            .setAttribute(HTTP_REQUEST_METHOD, request.getHttpMethod().toString())
            .setAttribute(URL_FULL, request.getUri().toString()) // TODO: sanitize
            .setAttribute(SERVER_ADDRESS, request.getUri().getHost())
            .setAttribute(SERVER_PORT, request.getUri().getPort() == -1 ? 443 : request.getUri().getPort())
            .startSpan();

        try(Scope scope = span.makeCurrent()) {
            propagateContext(request, span);
            Response<?> response = next.process();

            if (span.isRecording()) {
                span.setAttribute(HTTP_RESPONSE_STATUS_CODE, (long) response.getStatusCode());

                int retryCount = HttpRequestAccessHelper.getRetryCount(request);
                if (retryCount > 1) {
                    span.setAttribute(HTTP_REQUEST_RESEND_COUNT, (long) HttpRequestAccessHelper.getRetryCount(request) - 1);
                }

                String userAgent = request.getHeaders().getValue(HttpHeaderName.USER_AGENT);
                if (userAgent != null) {
                    span.setAttribute(USER_AGENT_ORIGINAL, userAgent);
                }

                addRequestHeaders(request, span);
                addResponseHeaders(response, span);
                // TODO url.template?
            }

            if (response.getStatusCode() >= 400) {
                span.setError(String.valueOf(response.getStatusCode()));
            }

            return response;
        } catch (Throwable t) {
            span.setError(unwrap(t));
            throw t;
        } finally {
            span.end();
        }
    }

    private boolean isTracingEnabled(HttpRequest httpRequest) {
        if (!tracer.isEnabled()) {
            return false;
        }

        Context context = httpRequest.getRequestOptions().getContext();
        Object disableTracing = context.get(DISABLE_TRACING_KEY);
        if (disableTracing instanceof Boolean) {
            return !((Boolean) disableTracing);
        }

        return true;
    }

    private Throwable unwrap(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private void propagateContext(HttpRequest request, Span span) {
        // TODO (lmolkova): we should support full propagation including tracestate and baggage, especially for messaging cases
        // for now we'll only support traceparent
        String traceparent = "00-" + span.getSpanContext().getTraceId() + "-" + span.getSpanContext().getSpanId() + "-" +
            span.getSpanContext().getTraceFlags().toString();
        request.getHeaders().set(HttpHeaderName.TRACEPARENT, traceparent);
    }

    private void addResponseHeaders(Response<?> response, Span span) {
        if (responseHeaders != null) {
            Set<Map.Entry<HttpHeaderName, String>> entries = responseHeaders.entrySet();
            for (Map.Entry<HttpHeaderName, String> entry : entries) {
                String value = response.getHeaders().getValue(entry.getKey());
                if (value != null) {
                    span.setAttribute(entry.getValue(), value);
                }
            }
        }
    }

    private void addRequestHeaders(HttpRequest httpRequest, Span span) {
        if (requestHeaders != null) {
            Set<Map.Entry<HttpHeaderName, String>> entries = requestHeaders.entrySet();
            for (Map.Entry<HttpHeaderName, String> entry : entries) {
                String value = httpRequest.getHeaders().getValue(entry.getKey());
                if (value != null) {
                    span.setAttribute(entry.getValue(), value);
                }
            }
        }
    }
}
