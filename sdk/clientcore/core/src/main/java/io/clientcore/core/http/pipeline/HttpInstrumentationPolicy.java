// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpInstrumentationOptions;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.implementation.instrumentation.LibraryInstrumentationOptionsAccessHelper;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.net.URI;

import static io.clientcore.core.implementation.UrlRedactionUtil.getRedactedUri;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_BODY_CONTENT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_BODY_SIZE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_DURATION_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_HEADER_CONTENT_LENGTH_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_METHOD_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_RESEND_COUNT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_TIME_TO_RESPONSE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_RESPONSE_BODY_CONTENT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_RESPONSE_BODY_SIZE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_RESPONSE_HEADER_CONTENT_LENGTH_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_RESPONSE_STATUS_CODE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_ADDRESS_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_PORT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.URL_FULL_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.USER_AGENT_ORIGINAL_KEY;
import static io.clientcore.core.implementation.instrumentation.LoggingEventNames.HTTP_REQUEST_EVENT_NAME;
import static io.clientcore.core.implementation.instrumentation.LoggingEventNames.HTTP_RESPONSE_EVENT_NAME;
import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;
import static io.clientcore.core.instrumentation.tracing.SpanKind.CLIENT;

/**
 * The {@link HttpInstrumentationPolicy} is responsible for instrumenting the HTTP request and response with distributed tracing
 * and (in the future) metrics following
 * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-spans.md">OpenTelemetry Semantic Conventions</a>.
 * <p>
 * It propagates context to the downstream service following <a href="https://www.w3.org/TR/trace-context-1/">W3C Trace Context</a> specification.
 * <p>
 * The {@link HttpInstrumentationPolicy} should be added to the HTTP pipeline by client libraries. It should be added after
 * {@link HttpRetryPolicy} and {@link HttpRedirectPolicy} so that it's executed on each try or redirect and logging happens
 * in the scope of the span.
 * <p>
 * The policy supports basic customizations using {@link HttpInstrumentationOptions}.
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
 *         new HttpInstrumentationPolicy&#40;instrumentationOptions&#41;&#41;
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
 * HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions&#40;&#41;;
 * instrumentationOptions.addAllowedQueryParamName&#40;&quot;documentId&quot;&#41;;
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;
 *         new HttpRetryPolicy&#40;&#41;,
 *         new HttpInstrumentationPolicy&#40;instrumentationOptions&#41;&#41;
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
 *     Span span = request.getRequestOptions&#40;&#41; == null
 *         ? Span.noop&#40;&#41;
 *         : request.getRequestOptions&#40;&#41;.getInstrumentationContext&#40;&#41;.getSpan&#40;&#41;;
 *     if &#40;span.isRecording&#40;&#41;&#41; &#123;
 *         span.setAttribute&#40;&quot;custom.request.id&quot;, request.getHeaders&#40;&#41;.getValue&#40;CUSTOM_REQUEST_ID&#41;&#41;;
 *     &#125;
 *
 *     return next.process&#40;&#41;;
 * &#125;;
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;
 *         new HttpRetryPolicy&#40;&#41;,
 *         new HttpInstrumentationPolicy&#40;instrumentationOptions&#41;,
 *         enrichingPolicy&#41;
 *     .build&#40;&#41;;
 *
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.tracing.enrichhttpspans -->
 *
 */
public final class HttpInstrumentationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpInstrumentationPolicy.class);
    private static final HttpInstrumentationOptions DEFAULT_OPTIONS = new HttpInstrumentationOptions();
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

    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";

    // request log level is low (verbose) since almost all request details are also
    // captured on the response log.
    private static final ClientLogger.LogLevel HTTP_REQUEST_LOG_LEVEL = ClientLogger.LogLevel.VERBOSE;
    private static final ClientLogger.LogLevel HTTP_RESPONSE_LOG_LEVEL = ClientLogger.LogLevel.INFORMATIONAL;

    private final Tracer tracer;
    private final TraceContextPropagator traceContextPropagator;
    private final Set<String> allowedQueryParameterNames;
    private final Set<HttpHeaderName> allowedHeaderNames;
    private final boolean isLoggingEnabled;
    private final boolean isContentLoggingEnabled;
    private final boolean isRedactedHeadersLoggingEnabled;

    /**
     * Creates a new instrumentation policy.
     * @param instrumentationOptions Application telemetry options.
     */
    public HttpInstrumentationPolicy(HttpInstrumentationOptions instrumentationOptions) {
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, LIBRARY_OPTIONS);
        this.tracer = instrumentation.getTracer();
        this.traceContextPropagator = instrumentation.getW3CTraceContextPropagator();

        HttpInstrumentationOptions optionsToUse
            = instrumentationOptions == null ? DEFAULT_OPTIONS : instrumentationOptions;
        this.isLoggingEnabled = optionsToUse.getHttpLogLevel() != HttpInstrumentationOptions.HttpLogDetailLevel.NONE;
        this.isContentLoggingEnabled
            = optionsToUse.getHttpLogLevel() == HttpInstrumentationOptions.HttpLogDetailLevel.BODY
                || optionsToUse.getHttpLogLevel() == HttpInstrumentationOptions.HttpLogDetailLevel.BODY_AND_HEADERS;
        this.isRedactedHeadersLoggingEnabled = optionsToUse.isRedactedHeaderNamesLoggingEnabled();
        this.allowedHeaderNames = optionsToUse.getAllowedHeaderNames();
        this.allowedQueryParameterNames = optionsToUse.getAllowedQueryParamNames()
            .stream()
            .map(queryParamName -> queryParamName.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("try")
    @Override
    public Response<?> process(HttpRequest request, HttpPipelineNextPolicy next) {
        boolean isTracingEnabled = tracer.isEnabled();
        if (!isTracingEnabled && !isLoggingEnabled) {
            return next.process();
        }

        ClientLogger logger = getLogger(request);
        final long startNs = System.nanoTime();
        String redactedUrl = getRedactedUri(request.getUri(), allowedQueryParameterNames);
        int tryCount = HttpRequestAccessHelper.getTryCount(request);
        final long requestContentLength = getContentLength(logger, request.getBody(), request.getHeaders(), true);

        InstrumentationContext instrumentationContext
            = request.getRequestOptions() == null ? null : request.getRequestOptions().getInstrumentationContext();
        Span span = Span.noop();
        if (isTracingEnabled) {
            if (request.getRequestOptions() == null || request.getRequestOptions() == RequestOptions.none()) {
                request.setRequestOptions(new RequestOptions());
            }

            span = startHttpSpan(request, redactedUrl, instrumentationContext);
            instrumentationContext = span.getInstrumentationContext();
            request.getRequestOptions().setInstrumentationContext(instrumentationContext);
        }

        // even if tracing is disabled, we could have a valid context to propagate
        // if it was provided by the application explicitly.
        if (instrumentationContext != null && instrumentationContext.isValid()) {
            traceContextPropagator.inject(instrumentationContext, request.getHeaders(), SETTER);
        }

        logRequest(logger, request, startNs, requestContentLength, redactedUrl, tryCount, instrumentationContext);

        try (TracingScope scope = span.makeCurrent()) {
            Response<?> response = next.process();

            if (response == null) {
                LOGGER.atError()
                    .setInstrumentationContext(span.getInstrumentationContext())
                    .addKeyValue(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod())
                    .addKeyValue(URL_FULL_KEY, redactedUrl)
                    .log(
                        "HTTP response is null and no exception is thrown. Please report it to the client library maintainers.");

                return null;
            }

            addDetails(request, response, tryCount, span);
            response = logResponse(logger, response, startNs, requestContentLength, redactedUrl, tryCount,
                instrumentationContext);
            span.end();
            return response;
        } catch (RuntimeException t) {
            span.end(unwrap(t));
            // TODO (limolkova) test otel scope still covers this
            throw logException(logger, request, null, t, startNs, null, requestContentLength, redactedUrl, tryCount,
                instrumentationContext);
        }
    }

    private Span startHttpSpan(HttpRequest request, String sanitizedUrl, InstrumentationContext context) {
        SpanBuilder spanBuilder = tracer.spanBuilder(request.getHttpMethod().toString(), CLIENT, context)
            .setAttribute(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod().toString())
            .setAttribute(URL_FULL_KEY, sanitizedUrl)
            .setAttribute(SERVER_ADDRESS_KEY, request.getUri().getHost());
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
            spanBuilder.setAttribute(SERVER_PORT_KEY, port);
        } else {
            switch (uri.getScheme()) {
                case "http":
                    spanBuilder.setAttribute(SERVER_PORT_KEY, 80);
                    break;

                case "https":
                    spanBuilder.setAttribute(SERVER_PORT_KEY, 443);
                    break;

                default:
                    break;
            }
        }
    }

    private void addDetails(HttpRequest request, Response<?> response, int tryCount, Span span) {
        if (!span.isRecording()) {
            return;
        }

        span.setAttribute(HTTP_RESPONSE_STATUS_CODE_KEY, (long) response.getStatusCode());

        if (tryCount > 0) {
            span.setAttribute(HTTP_REQUEST_RESEND_COUNT_KEY, (long) tryCount);
        }

        String userAgent = request.getHeaders().getValue(HttpHeaderName.USER_AGENT);
        if (userAgent != null) {
            span.setAttribute(USER_AGENT_ORIGINAL_KEY, userAgent);
        }

        if (response.getStatusCode() >= 400) {
            span.setError(String.valueOf(response.getStatusCode()));
        }
        // TODO (lmolkova) url.template and experimental features
    }

    private static Throwable unwrap(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private ClientLogger getLogger(HttpRequest httpRequest) {
        ClientLogger logger = null;

        if (httpRequest.getRequestOptions() != null && httpRequest.getRequestOptions().getLogger() != null) {
            logger = httpRequest.getRequestOptions().getLogger();
        }

        return logger == null ? LOGGER : logger;
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

    private void logRequest(ClientLogger logger, HttpRequest request, long startNanoTime, long requestContentLength,
        String redactedUrl, int tryCount, InstrumentationContext context) {
        ClientLogger.LoggingEvent logBuilder = logger.atLevel(HTTP_REQUEST_LOG_LEVEL);
        if (!logBuilder.isEnabled() || !isLoggingEnabled) {
            return;
        }

        logBuilder.setEventName(HTTP_REQUEST_EVENT_NAME)
            .setInstrumentationContext(context)
            .addKeyValue(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod())
            .addKeyValue(URL_FULL_KEY, redactedUrl)
            .addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount)
            .addKeyValue(HTTP_REQUEST_BODY_SIZE_KEY, requestContentLength);

        addHeadersToLogMessage(request.getHeaders(), logBuilder);

        if (isContentLoggingEnabled && canLogBody(request.getBody())) {
            try {
                BinaryData bufferedBody = request.getBody().toReplayableBinaryData();
                request.setBody(bufferedBody);
                logBuilder.addKeyValue(HTTP_REQUEST_BODY_CONTENT_KEY, bufferedBody.toString());
            } catch (RuntimeException e) {
                // we'll log exception at the appropriate level.
                throw logException(logger, request, null, e, startNanoTime, null, requestContentLength, redactedUrl,
                    tryCount, context);
            }
        }

        logBuilder.log();
    }

    private Response<?> logResponse(ClientLogger logger, Response<?> response, long startNanoTime,
        long requestContentLength, String redactedUrl, int tryCount, InstrumentationContext context) {
        ClientLogger.LoggingEvent logBuilder = logger.atLevel(HTTP_RESPONSE_LOG_LEVEL);
        if (!isLoggingEnabled) {
            return response;
        }

        long responseStartNanoTime = System.nanoTime();

        // response may be disabled, but we still need to log the exception if an exception occurs during stream reading.
        if (logBuilder.isEnabled()) {
            logBuilder.setEventName(HTTP_RESPONSE_EVENT_NAME)
                .setInstrumentationContext(context)
                .addKeyValue(HTTP_REQUEST_METHOD_KEY, response.getRequest().getHttpMethod())
                .addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount)
                .addKeyValue(URL_FULL_KEY, redactedUrl)
                .addKeyValue(HTTP_REQUEST_TIME_TO_RESPONSE_KEY, getDurationMs(startNanoTime, responseStartNanoTime))
                .addKeyValue(HTTP_RESPONSE_STATUS_CODE_KEY, response.getStatusCode())
                .addKeyValue(HTTP_REQUEST_BODY_SIZE_KEY, requestContentLength)
                .addKeyValue(HTTP_RESPONSE_BODY_SIZE_KEY,
                    getContentLength(logger, response.getBody(), response.getHeaders(), false));

            addHeadersToLogMessage(response.getHeaders(), logBuilder);
        }

        if (isContentLoggingEnabled && canLogBody(response.getBody())) {
            return new LoggingHttpResponse<>(response, content -> {
                if (logBuilder.isEnabled()) {
                    logBuilder.addKeyValue(HTTP_RESPONSE_BODY_CONTENT_KEY, content.toString())
                        .addKeyValue(HTTP_REQUEST_DURATION_KEY, getDurationMs(startNanoTime, System.nanoTime()))
                        .log();
                }
            }, throwable -> logException(logger, response.getRequest(), response, throwable, startNanoTime,
                responseStartNanoTime, requestContentLength, redactedUrl, tryCount, context));
        }

        if (logBuilder.isEnabled()) {
            logBuilder.addKeyValue(HTTP_REQUEST_DURATION_KEY, getDurationMs(startNanoTime, System.nanoTime())).log();
        }

        return response;
    }

    private <T extends Throwable> T logException(ClientLogger logger, HttpRequest request, Response<?> response,
        T throwable, long startNanoTime, Long responseStartNanoTime, long requestContentLength, String redactedUrl,
        int tryCount, InstrumentationContext context) {

        ClientLogger.LoggingEvent log = logger.atLevel(ClientLogger.LogLevel.WARNING);
        if (!log.isEnabled() || !isLoggingEnabled) {
            return throwable;
        }

        log.setEventName(HTTP_RESPONSE_EVENT_NAME)
            .setInstrumentationContext(context)
            .addKeyValue(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod())
            .addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount)
            .addKeyValue(URL_FULL_KEY, redactedUrl)
            .addKeyValue(HTTP_REQUEST_BODY_SIZE_KEY, requestContentLength)
            .addKeyValue(HTTP_REQUEST_DURATION_KEY, getDurationMs(startNanoTime, System.nanoTime()));

        if (response != null) {
            addHeadersToLogMessage(response.getHeaders(), log);
            log.addKeyValue(HTTP_RESPONSE_BODY_SIZE_KEY,
                getContentLength(logger, response.getBody(), response.getHeaders(), false))
                .addKeyValue(HTTP_RESPONSE_STATUS_CODE_KEY, response.getStatusCode());

            if (responseStartNanoTime != null) {
                log.addKeyValue(HTTP_REQUEST_TIME_TO_RESPONSE_KEY, getDurationMs(startNanoTime, responseStartNanoTime));
            }
        }

        log.log(null, unwrap(throwable));
        return throwable;
    }

    private double getDurationMs(long startNs, long endNs) {
        return (endNs - startNs) / 1_000_000.0;
    }

    /**
     * Determines if the request or response body should be logged.
     *
     * <p>The request or response body is logged if the body is replayable, content length is known,
     * isn't empty, and is less than 16KB in size.</p>
     *
     * @param data The request or response body.
     * @return A flag indicating if the request or response body should be logged.
     */
    private static boolean canLogBody(BinaryData data) {
        // TODO (limolkova) we might want to filter out binary data, but
        // if somebody enabled logging it - why not log it?
        return data != null && data.getLength() != null && data.getLength() > 0 && data.getLength() < MAX_BODY_LOG_SIZE;
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the log message.
     *
     * @param headers HTTP headers on the request or response.
     * @param logBuilder Log message builder.
     */
    private void addHeadersToLogMessage(HttpHeaders headers, ClientLogger.LoggingEvent logBuilder) {
        for (HttpHeader header : headers) {
            HttpHeaderName headerName = header.getName();
            if (allowedHeaderNames.contains(headerName)) {
                logBuilder.addKeyValue(headerName.toString(), header.getValue());
            } else if (isRedactedHeadersLoggingEnabled) {
                logBuilder.addKeyValue(headerName.toString(), REDACTED_PLACEHOLDER);
            }
        }
    }

    /**
     * Attempts to get request or response body content length.
     * <p>
     * If the body length is known, it will be returned.
     * Otherwise, the method parses Content-Length header.
     *
     * @param logger Logger used to log a warning if the Content-Length header is an invalid number.
     * @param body The request or response body object.
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return The numeric value of the Content-Length header or 0 if the header is not present or invalid.
     */
    private static long getContentLength(ClientLogger logger, BinaryData body, HttpHeaders headers, boolean isRequest) {
        if (body == null) {
            return 0;
        }

        if (body.getLength() != null) {
            return body.getLength();
        }

        long contentLength = 0;

        String contentLengthString = headers.getValue(HttpHeaderName.CONTENT_LENGTH);

        if (isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException e) {
            logger.atVerbose()
                .addKeyValue(
                    isRequest ? HTTP_REQUEST_HEADER_CONTENT_LENGTH_KEY : HTTP_RESPONSE_HEADER_CONTENT_LENGTH_KEY,
                    contentLengthString)
                .log("Could not parse the HTTP header content-length", e);
        }

        return contentLength;
    }

    private static final class LoggingHttpResponse<T> extends HttpResponse<T> {
        private final Consumer<BinaryData> onContent;
        private final Consumer<Throwable> onException;
        private final BinaryData originalBody;
        private BinaryData bufferedBody;

        private LoggingHttpResponse(Response<T> actualResponse, Consumer<BinaryData> onContent,
            Consumer<Throwable> onException) {
            super(actualResponse.getRequest(), actualResponse.getStatusCode(), actualResponse.getHeaders(),
                actualResponse.getValue());

            this.onContent = onContent;
            this.onException = onException;
            this.originalBody = actualResponse.getBody();
        }

        @Override
        public BinaryData getBody() {
            if (bufferedBody != null) {
                return bufferedBody;
            }

            try {
                bufferedBody = originalBody.toReplayableBinaryData();
                onContent.accept(bufferedBody);
                return bufferedBody;
            } catch (RuntimeException e) {
                // we'll log exception at the appropriate level.
                onException.accept(e);
                throw e;
            }
        }

        @Override
        public void close() throws IOException {
            if (bufferedBody == null) {
                getBody();
            }
            if (bufferedBody != null) {
                bufferedBody.close();
            }
            originalBody.close();
        }
    }
}
