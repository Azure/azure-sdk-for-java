// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.implementation.telemetry.LibraryTelemetryOptionsAccessHelper;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.TelemetryOptions;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.TextMapPropagator;
import io.clientcore.core.telemetry.tracing.TextMapSetter;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.telemetry.tracing.TracingScope;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static io.clientcore.core.implementation.UrlRedactionUtil.getRedactedUri;
import static io.clientcore.core.telemetry.TelemetryProvider.DISABLE_TRACING_KEY;
import static io.clientcore.core.telemetry.TelemetryProvider.TRACE_CONTEXT_KEY;
import static io.clientcore.core.telemetry.tracing.SpanKind.CLIENT;

/**
 * The {@link InstrumentationPolicy} is responsible for instrumenting the HTTP request and response with distributed tracing
 * and (in the future) metrics following
 * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-spans.md">OpenTelemetry Semantic Conventions</a>.
 * <p>
 * It propagates context to the downstream service following <a href="https://www.w3.org/TR/trace-context-1/">W3C Trace Context</a> specification.
 * <p>
 * The {@link InstrumentationPolicy} should be added to the HTTP pipeline by client libraries. It should be added between
 * {@link HttpRetryPolicy} and {@link HttpLoggingPolicy} so that it's executed on each try (redirect), and logging happens
 * in the scope of the span.
 * <p>
 * The policy supports basic customizations using {@link TelemetryOptions}, {@link HttpLogOptions}, and additional request and
 * response headers to record as attributes.
 * <p>
 * If your client library needs a different approach to distributed tracing,
 * you can create a custom policy and use it instead of the {@link InstrumentationPolicy}. If you want to enrich instrumentation
 * policy spans with additional attributes, you can create a custom policy and add it under the {@link InstrumentationPolicy}
 * so that it's executed in the scope of the span created by the {@link InstrumentationPolicy}.
 *
 * <p><strong>Configure instrumentation policy:</strong><p>
 * <!-- src_embed io.clientcore.core.telemetry.tracing.instrumentationpolicy -->
 * <pre>
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;
 *         new HttpRetryPolicy&#40;&#41;,
 *         new InstrumentationPolicy&#40;telemetryOptions, logOptions&#41;,
 *         new HttpLoggingPolicy&#40;logOptions&#41;&#41;
 *     .build&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.tracing.instrumentationpolicy -->
 * <p>
 * <strong>Customize instrumentation policy:</strong>
 * <p>
 * <!-- src_embed io.clientcore.core.telemetry.tracing.customizeinstrumentationpolicy -->
 * <pre>
 *
 * &#47;&#47; InstrumentationPolicy can capture custom headers from requests and responses - for example when the endpoint
 * &#47;&#47; supports legacy correlation headers.
 * Map&lt;HttpHeaderName, String&gt; requestHeadersToRecord
 *     = Collections.singletonMap&#40;HttpHeaderName.CLIENT_REQUEST_ID, &quot;custom.request.id&quot;&#41;;
 * Map&lt;HttpHeaderName, String&gt; responseHeadersToRecord
 *     = Collections.singletonMap&#40;HttpHeaderName.REQUEST_ID, &quot;custom.response.id&quot;&#41;;
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;
 *         new HttpRetryPolicy&#40;&#41;,
 *         new InstrumentationPolicy&#40;telemetryOptions, logOptions, requestHeadersToRecord, responseHeadersToRecord&#41;,
 *         new HttpLoggingPolicy&#40;logOptions&#41;&#41;
 *     .build&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.tracing.customizeinstrumentationpolicy -->
 */
public class InstrumentationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(InstrumentationPolicy.class);
    private static final String LIBRARY_NAME;
    private static final String LIBRARY_VERSION;
    private static final LibraryTelemetryOptions LIBRARY_OPTIONS;
    private static final TextMapSetter<HttpHeaders> SETTER
        = (headers, name, value) -> headers.set(HttpHeaderName.fromString(name), value);

    static {
        Map<String, String> properties = getProperties("core.properties");
        LIBRARY_NAME = properties.getOrDefault("name", "unknown");
        LIBRARY_VERSION = properties.getOrDefault("version", "unknown");
        LibraryTelemetryOptions libOptions
            = new LibraryTelemetryOptions(LIBRARY_NAME).setLibraryVersion(LIBRARY_VERSION)
                .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        // HTTP tracing is special - we suppress nested public API spans, but
        // preserve nested HTTP ones.
        // We might want to make it configurable for other cases, but let's hide the API for now.
        LibraryTelemetryOptionsAccessHelper.disableSpanSuppression(libOptions);

        LIBRARY_OPTIONS = libOptions;
    }

    private static final String HTTP_REQUEST_METHOD = "http.request.method";
    private static final String HTTP_RESPONSE_STATUS_CODE = "http.response.status_code";
    private static final String SERVER_ADDRESS = "server.address";
    private static final String SERVER_PORT = "server.port";
    private static final String URL_FULL = "url.full";
    private static final String HTTP_REQUEST_RESEND_COUNT = "http.request.resend_count";
    private static final String USER_AGENT_ORIGINAL = "user_agent.original";

    private final Tracer tracer;
    private final Set<String> allowedQueryParams;
    private final Map<HttpHeaderName, String> requestHeaders;
    private final Map<HttpHeaderName, String> responseHeaders;
    private final TextMapPropagator contextPropagator;

    /**
     * Creates a new instrumentation policy.
     * @param telemetryOptions Application telemetry options.
     * @param logOptions Http log options. TODO: we should merge this with telemetry options.
     */
    public InstrumentationPolicy(TelemetryOptions<?> telemetryOptions, HttpLogOptions logOptions) {
        this(telemetryOptions, logOptions, null, null);
    }

    /**
     * Creates a new instrumentation policy with additional request and response headers to record as attributes.
     *
     * @param telemetryOptions Application telemetry options.
     * @param logOptions Http log options. TODO: we should merge this with telemetry options.
     * @param requestHeaders The request headers to capture as span attributes.
     * @param responseHeaders The response headers to capture as span attributes.
     */
    public InstrumentationPolicy(TelemetryOptions<?> telemetryOptions, HttpLogOptions logOptions,
        Map<HttpHeaderName, String> requestHeaders, Map<HttpHeaderName, String> responseHeaders) {
        TelemetryProvider telemetryProvider = TelemetryProvider.create(telemetryOptions, LIBRARY_OPTIONS);
        this.tracer = telemetryProvider.getTracer();
        this.contextPropagator = telemetryProvider.getW3CTraceContextPropagator();
        this.allowedQueryParams = logOptions == null ? Collections.emptySet() : logOptions.getAllowedQueryParamNames();
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

        String sanitizedUrl = getRedactedUri(request.getUri(), allowedQueryParams);

        Span span = tracer.spanBuilder(request.getHttpMethod().toString(), CLIENT, request.getRequestOptions())
            .setAttribute(HTTP_REQUEST_METHOD, request.getHttpMethod().toString())
            .setAttribute(URL_FULL, sanitizedUrl)
            .setAttribute(SERVER_ADDRESS, request.getUri().getHost())
            .setAttribute(SERVER_PORT, request.getUri().getPort() == -1 ? 443 : request.getUri().getPort())
            .startSpan();

        Context context = request.getRequestOptions().getContext();
        propagateContext(context.put(TRACE_CONTEXT_KEY, span), request.getHeaders());

        try (TracingScope scope = span.makeCurrent()) {
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

            span.end();
            return response;
        } catch (Throwable t) {
            span.end(unwrap(t));
            throw t;
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

    private void propagateContext(Context context, HttpHeaders headers) {
        contextPropagator.inject(context, headers, SETTER);
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
