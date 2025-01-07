// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.implementation.instrumentation.LibraryInstrumentationOptionsAccessHelper;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.net.URI;

import static io.clientcore.core.implementation.UrlRedactionUtil.getRedactedUri;
import static io.clientcore.core.instrumentation.Instrumentation.DISABLE_TRACING_KEY;
import static io.clientcore.core.instrumentation.Instrumentation.TRACE_CONTEXT_KEY;
import static io.clientcore.core.instrumentation.tracing.SpanKind.CLIENT;

/**
 * The {@link HttpInstrumentationPolicy} is responsible for instrumenting the HTTP request and response with distributed tracing
 * and (in the future) metrics following
 * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-spans.md">OpenTelemetry Semantic Conventions</a>.
 * <p>
 * It propagates context to the downstream service following <a href="https://www.w3.org/TR/trace-context-1/">W3C Trace Context</a> specification.
 * <p>
 * The {@link HttpInstrumentationPolicy} should be added to the HTTP pipeline by client libraries. It should be added between
 * {@link HttpRetryPolicy} and {@link HttpLoggingPolicy} so that it's executed on each try or redirect and logging happens
 * in the scope of the span.
 * <p>
 * The policy supports basic customizations using {@link InstrumentationOptions} and {@link HttpLogOptions}.
 * <p>
 * If your client library needs a different approach to distributed tracing,
 * you can create a custom policy and use it instead of the {@link HttpInstrumentationPolicy}. If you want to enrich instrumentation
 * policy spans with additional attributes, you can create a custom policy and add it under the {@link HttpInstrumentationPolicy}
 * so that it's executed in the scope of the span created by the {@link HttpInstrumentationPolicy}.
 *
 * <p><strong>Configure instrumentation policy:</strong></p>
 * <!-- src_embed io.clientcore.core.telemetry.tracing.instrumentationpolicy -->
 * <pre>
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;
 *         new HttpRetryPolicy&#40;&#41;,
 *         new HttpInstrumentationPolicy&#40;instrumentationOptions, logOptions&#41;,
 *         new HttpLoggingPolicy&#40;logOptions&#41;&#41;
 *     .build&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.tracing.instrumentationpolicy -->
 *
 * <p><strong>Customize instrumentation policy:</strong></p>
 * <!-- src_embed io.clientcore.core.telemetry.tracing.customizeinstrumentationpolicy -->
 * <pre>
 *
 * &#47;&#47; You can configure URL sanitization to include additional query parameters to preserve
 * &#47;&#47; in `url.full` attribute.
 * HttpLogOptions logOptions = new HttpLogOptions&#40;&#41;;
 * logOptions.addAllowedQueryParamName&#40;&quot;documentId&quot;&#41;;
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;
 *         new HttpRetryPolicy&#40;&#41;,
 *         new HttpInstrumentationPolicy&#40;instrumentationOptions, logOptions&#41;,
 *         new HttpLoggingPolicy&#40;logOptions&#41;&#41;
 *     .build&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.tracing.customizeinstrumentationpolicy -->
 *
 * <p><strong>Enrich HTTP spans with additional attributes:</strong></p>
 * <!-- src_embed io.clientcore.core.telemetry.tracing.enrichhttpspans -->
 * <pre>
 *
 * HttpPipelinePolicy enrichingPolicy = &#40;request, next&#41; -&gt; &#123;
 *     Object span = request.getRequestOptions&#40;&#41;.getContext&#40;&#41;.get&#40;TRACE_CONTEXT_KEY&#41;;
 *     if &#40;span instanceof Span&#41; &#123;
 *         &#40;&#40;Span&#41;span&#41;.setAttribute&#40;&quot;custom.request.id&quot;, request.getHeaders&#40;&#41;.getValue&#40;CUSTOM_REQUEST_ID&#41;&#41;;
 *     &#125;
 *
 *     return next.process&#40;&#41;;
 * &#125;;
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;
 *         new HttpRetryPolicy&#40;&#41;,
 *         new HttpInstrumentationPolicy&#40;instrumentationOptions, logOptions&#41;,
 *         enrichingPolicy,
 *         new HttpLoggingPolicy&#40;logOptions&#41;&#41;
 *     .build&#40;&#41;;
 *
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.tracing.enrichhttpspans -->
 *
 */
public final class HttpInstrumentationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpInstrumentationPolicy.class);
    private static final HttpLogOptions DEFAULT_LOG_OPTIONS = new HttpLogOptions();
    private static final String LIBRARY_NAME;
    private static final String LIBRARY_VERSION;
    private static final LibraryInstrumentationOptions LIBRARY_OPTIONS;
    private static final TraceContextSetter<HttpHeaders> SETTER
        = (headers, name, value) -> headers.set(HttpHeaderName.fromString(name), value);

    static {
        Map<String, String> properties = getProperties("core.properties");
        LIBRARY_NAME = properties.getOrDefault("name", "unknown");
        LIBRARY_VERSION = properties.getOrDefault("version", "unknown");
        LibraryInstrumentationOptions libOptions
            = new LibraryInstrumentationOptions(LIBRARY_NAME).setLibraryVersion(LIBRARY_VERSION)
                .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        // HTTP tracing is special - we suppress nested public API spans, but
        // preserve nested HTTP ones.
        // We might want to make it configurable for other cases, but let's hide the API for now.
        LibraryInstrumentationOptionsAccessHelper.disableSpanSuppression(libOptions);

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
    private final TraceContextPropagator traceContextPropagator;
    private final Set<String> allowedQueryParameterNames;

    /**
     * Creates a new instrumentation policy.
     * @param instrumentationOptions Application telemetry options.
     * @param logOptions Http log options. TODO: we should merge this with telemetry options.
     */
    public HttpInstrumentationPolicy(InstrumentationOptions<?> instrumentationOptions, HttpLogOptions logOptions) {
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, LIBRARY_OPTIONS);
        this.tracer = instrumentation.getTracer();
        this.traceContextPropagator = instrumentation.getW3CTraceContextPropagator();

        HttpLogOptions logOptionsToUse = logOptions == null ? DEFAULT_LOG_OPTIONS : logOptions;
        this.allowedQueryParameterNames = logOptionsToUse.getAllowedQueryParamNames();
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

        String sanitizedUrl = getRedactedUri(request.getUri(), allowedQueryParameterNames);
        Span span = startHttpSpan(request, sanitizedUrl);

        if (request.getRequestOptions() == RequestOptions.none()) {
            request = request.setRequestOptions(new RequestOptions());
        }

        Context context = request.getRequestOptions().getContext().put(TRACE_CONTEXT_KEY, span);
        request.getRequestOptions().setContext(context);
        propagateContext(context, request.getHeaders());

        try (TracingScope scope = span.makeCurrent()) {
            Response<?> response = next.process();

            addDetails(request, response, span);

            span.end();
            return response;
        } catch (Throwable t) {
            span.end(unwrap(t));
            throw t;
        }
    }

    private Span startHttpSpan(HttpRequest request, String sanitizedUrl) {
        SpanBuilder spanBuilder
            = tracer.spanBuilder(request.getHttpMethod().toString(), CLIENT, request.getRequestOptions())
                .setAttribute(HTTP_REQUEST_METHOD, request.getHttpMethod().toString())
                .setAttribute(URL_FULL, sanitizedUrl)
                .setAttribute(SERVER_ADDRESS, request.getUri().getHost());
        maybeSetServerPort(spanBuilder, request.getUri());
        return spanBuilder.startSpan();
    }

    /**
     * Does the best effort to capture the server port with minimum perf overhead.
     * If port is not set, we check scheme for "http" and "https" (case-sensitive).
     * If scheme is not one of those, we don't set the port.
     *
     * @param spanBuilder span builder
     * @param uri request URI
     */
    private static void maybeSetServerPort(SpanBuilder spanBuilder, URI uri) {
        int port = uri.getPort();
        if (port != -1) {
            spanBuilder.setAttribute(SERVER_PORT, port);
        } else {
            switch (uri.getScheme()) {
                case "http":
                    spanBuilder.setAttribute(SERVER_PORT, 80);
                    break;

                case "https":
                    spanBuilder.setAttribute(SERVER_PORT, 443);
                    break;

                default:
                    break;
            }
        }
    }

    private void addDetails(HttpRequest request, Response<?> response, Span span) {
        if (!span.isRecording()) {
            return;
        }

        span.setAttribute(HTTP_RESPONSE_STATUS_CODE, (long) response.getStatusCode());

        int tryCount = HttpRequestAccessHelper.getTryCount(request);
        if (tryCount > 0) {
            span.setAttribute(HTTP_REQUEST_RESEND_COUNT, (long) tryCount);
        }

        String userAgent = request.getHeaders().getValue(HttpHeaderName.USER_AGENT);
        if (userAgent != null) {
            span.setAttribute(USER_AGENT_ORIGINAL, userAgent);
        }

        if (response.getStatusCode() >= 400) {
            span.setError(String.valueOf(response.getStatusCode()));
        }
        // TODO (lmolkova) url.template and experimental features
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
        traceContextPropagator.inject(context, headers, SETTER);
    }

    private static Map<String, String> getProperties(String propertiesFileName) {
        try (InputStream inputStream
            = HttpInstrumentationPolicy.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
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
