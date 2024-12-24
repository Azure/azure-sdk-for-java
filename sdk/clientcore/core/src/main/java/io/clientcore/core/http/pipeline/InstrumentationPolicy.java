// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.TelemetryOptions;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.telemetry.Scope;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanContext;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static io.clientcore.core.telemetry.TelemetryProvider.DISABLE_TRACING_KEY;

/**
 * The instrumentation policy is responsible for instrumenting the HTTP request and response with distributed tracing
 * and (in the future) metrics.
 */
public class InstrumentationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(InstrumentationPolicy.class);
    private static final String LIBRARY_NAME;
    private static final String LIBRARY_VERSION;

    static {
        Map<String, String> properties = getProperties("core.properties");
        LIBRARY_NAME = properties.getOrDefault("name", "unknown");
        LIBRARY_VERSION = properties.getOrDefault("version", "unknown");
    }

    private static final LibraryTelemetryOptions LIBRARY_OPTIONS
        = new LibraryTelemetryOptions(LIBRARY_NAME).setLibraryVersion(LIBRARY_VERSION)
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

    /**
     * Creates a new instrumentation policy.
     *
     * @param options Application telemetry options.
     */
    public InstrumentationPolicy(TelemetryOptions<?> options) {
        this(options, null, null);
    }

    /**
     * Creates a new instrumentation policy with the ability to capture request and response headers.
     *
     * @param options Application telemetry options.
     * @param requestHeaders The request headers to capture.
     * @param responseHeaders The response headers to capture.
     */
    public InstrumentationPolicy(TelemetryOptions<?> options, Map<HttpHeaderName, String> requestHeaders,
        Map<HttpHeaderName, String> responseHeaders) {
        this.tracer = TelemetryProvider.getInstance().getTracer(options, LIBRARY_OPTIONS);
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("try")
    @Override
    public Response<?> process(HttpRequest request, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled(request)) {
            return next.process();
        }

        Span span = tracer.spanBuilder(request.getHttpMethod().toString())
            .setSpanKind(SpanKind.CLIENT)
            .setParent(request.getRequestOptions().getContext())
            .setAttribute(HTTP_REQUEST_METHOD, request.getHttpMethod().toString())
            .setAttribute(URL_FULL, request.getUri().toString()) // TODO (lmolkova): sanitize
            .setAttribute(SERVER_ADDRESS, request.getUri().getHost())
            .setAttribute(SERVER_PORT, request.getUri().getPort() == -1 ? 443 : request.getUri().getPort())
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            propagateContext(request, span.getSpanContext());
            Response<?> response = next.process();

            if (span.isRecording()) {
                span.setAttribute(HTTP_RESPONSE_STATUS_CODE, (long) response.getStatusCode());

                int retryCount = HttpRequestAccessHelper.getRetryCount(request);
                if (retryCount > 1) {
                    span.setAttribute(HTTP_REQUEST_RESEND_COUNT,
                        (long) HttpRequestAccessHelper.getRetryCount(request) - 1);
                }

                String userAgent = request.getHeaders().getValue(HttpHeaderName.USER_AGENT);
                if (userAgent != null) {
                    span.setAttribute(USER_AGENT_ORIGINAL, userAgent);
                }

                addRequestHeaders(request, span);
                addResponseHeaders(response, span);
                // TODO (lmolkova) url.template and experimental features
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

    private void propagateContext(HttpRequest request, SpanContext spanContext) {
        // TODO (lmolkova): we should support full propagation including tracestate and baggage, especially for messaging cases
        // for now we'll only support traceparent
        String traceparent
            = "00-" + spanContext.getTraceId() + "-" + spanContext.getSpanId() + "-" + spanContext.getTraceFlags();
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

    private static Map<String, String> getProperties(String propertiesFileName) {
        try (InputStream inputStream
            = InstrumentationPolicy.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                return Collections.unmodifiableMap(properties.entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())));
            }
        } catch (IOException ex) {
            LOGGER.atWarning()
                .addKeyValue("propertiesFileName", propertiesFileName)
                .log("Failed to read properties.", ex);
        }

        return Collections.emptyMap();
    }
}
