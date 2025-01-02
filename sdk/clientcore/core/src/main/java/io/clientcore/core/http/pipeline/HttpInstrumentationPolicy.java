// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
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
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.util.Context;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.net.URI;

import static io.clientcore.core.implementation.UrlRedactionUtil.getRedactedUri;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_BODY_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_BODY_SIZE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_DURATION_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_METHOD_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_RESEND_COUNT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_TIME_TO_HEADERS_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_RESPONSE_BODY_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_RESPONSE_BODY_SIZE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_RESPONSE_STATUS_CODE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.URL_FULL_KEY;
import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;
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
 * The {@link HttpInstrumentationPolicy} should be added to the HTTP pipeline by client libraries. It should be added after
 * {@link HttpRetryPolicy} and {@link HttpRedirectPolicy} so that it's executed on each try (redirect), and logging happens
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
 *         new HttpInstrumentationPolicy&#40;instrumentationOptions, logOptions&#41;&#41;
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
 *         new HttpInstrumentationPolicy&#40;instrumentationOptions, logOptions&#41;&#41;
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

    private static final String SERVER_ADDRESS = "server.address";
    private static final String SERVER_PORT = "server.port";
    private static final String USER_AGENT_ORIGINAL = "user_agent.original";
    private static final String HTTP_REQUEST_EVENT_NAME = "http.request";
    private static final String HTTP_RESPONSE_EVENT_NAME = "http.response";
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";

    private final Tracer tracer;
    private final TraceContextPropagator traceContextPropagator;
    private final Set<String> allowedQueryParameterNames;
    private final HttpLogOptions.HttpLogDetailLevel httpLogDetailLevel;
    private final HttpRequestLogger requestLogger;
    private final HttpResponseLogger responseLogger;
    private final Set<HttpHeaderName> allowedHeaderNames;

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
        this.httpLogDetailLevel = logOptionsToUse.getLogLevel();

        this.requestLogger = logOptionsToUse.getRequestLogger() == null
            ? new DefaultHttpRequestLogger()
            : logOptionsToUse.getRequestLogger();
        this.responseLogger = logOptionsToUse.getResponseLogger() == null
            ? new DefaultHttpResponseLogger()
            : logOptionsToUse.getResponseLogger();
        this.allowedHeaderNames = logOptionsToUse.getAllowedHeaderNames();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("try")
    @Override
    public Response<?> process(HttpRequest request, HttpPipelineNextPolicy next) {
        boolean isTracingEnabled = isTracingEnabled(request);
        if (!isTracingEnabled && httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
            return next.process();
        }

        String sanitizedUrl = getRedactedUri(request.getUri(), allowedQueryParameterNames);
        int tryCount = HttpRequestAccessHelper.getTryCount(request);

        Span span = startHttpSpan(request, sanitizedUrl);

        traceContextPropagator.inject(request.getRequestOptions().getContext(), request.getHeaders(), SETTER);

        ClientLogger logger = getLogger(request);
        final long startNs = System.nanoTime();
        requestLogger.logRequest(logger, request, sanitizedUrl, tryCount);

        try (TracingScope scope = span.makeCurrent()) {
            Response<?> response = next.process();

            addDetails(request, response, tryCount, span);
            instrumentResponse((HttpResponse<?>) response, span, logger, startNs, sanitizedUrl, tryCount);
            return response;
        } catch (Throwable t) {
            responseLogger.logException(logger, request, null, t, startNs, null, sanitizedUrl, tryCount);
            span.end(unwrap(t));
            throw t;
        }
    }

    private Span startHttpSpan(HttpRequest request, String sanitizedUrl) {
        if (!isTracingEnabled(request)) {
            return Span.noop();
        }

        if (request.getRequestOptions() == null || request.getRequestOptions() == RequestOptions.none()) {
            request.setRequestOptions(new RequestOptions());
        }

        SpanBuilder spanBuilder
            = tracer.spanBuilder(request.getHttpMethod().toString(), CLIENT, request.getRequestOptions())
                .setAttribute(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod().toString())
                .setAttribute(URL_FULL_KEY, sanitizedUrl)
                .setAttribute(SERVER_ADDRESS, request.getUri().getHost());
        maybeSetServerPort(spanBuilder, request.getUri());
        Span span = spanBuilder.startSpan();
        request.getRequestOptions().putContext(TRACE_CONTEXT_KEY, span);
        return span;
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

        if (httpRequest.getRequestOptions() == null) {
            return true;
        }

        Context context = httpRequest.getRequestOptions().getContext();
        Object disableTracing = context.get(DISABLE_TRACING_KEY);
        if (disableTracing instanceof Boolean) {
            return !((Boolean) disableTracing);
        }

        return true;
    }

    private static Throwable unwrap(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private void setContext(HttpRequest request, Context context) {

    }

    private ClientLogger getLogger(HttpRequest httpRequest) {
        ClientLogger logger = null;

        if (httpRequest.getRequestOptions() != null) {
            logger = httpRequest.getRequestOptions().getLogger();
        }

        return logger == null ? LOGGER : logger;
    }

    private final class DefaultHttpRequestLogger implements HttpRequestLogger {
        @Override
        public void logRequest(ClientLogger logger, HttpRequest request, String redactedUrl, int tryCount) {
            ClientLogger.LoggingEvent log = logger.atLevel(getLogLevel(request));
            if (!log.isEnabled() || httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
                return;
            }

            log.setEventName(HTTP_REQUEST_EVENT_NAME)
                .setContext(request.getRequestOptions().getContext())
                .addKeyValue(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod())
                .addKeyValue(URL_FULL_KEY, redactedUrl)
                .addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount);

            addHeadersToLogMessage(request.getHeaders(), log);

            long contentLength = request.getBody() == null ? 0 : getContentLength(request.getHeaders());
            log.addKeyValue(HTTP_REQUEST_BODY_SIZE_KEY, contentLength);

            if (httpLogDetailLevel.shouldLogBody() && canLogBody(request.getBody())) {
                log.addKeyValue(HTTP_REQUEST_BODY_KEY, request.getBody().toString());
            }

            log.log();
        }
    }

    private final class DefaultHttpResponseLogger implements HttpResponseLogger {
        @Override
        public void logResponse(ClientLogger logger, Response<?> response, long startNanoTime, long headersNanoTime,
            String redactedUrl, int tryCount) {
            ClientLogger.LoggingEvent log = logger.atLevel(getLogLevel(response));
            if (!log.isEnabled() || httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
                return;
            }

            long contentLength = getContentLength(response.getHeaders());

            log.setEventName(HTTP_RESPONSE_EVENT_NAME)
                .setContext(response.getRequest().getRequestOptions().getContext())
                .addKeyValue(HTTP_REQUEST_METHOD_KEY, response.getRequest().getHttpMethod())
                .addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount)
                .addKeyValue(URL_FULL_KEY, redactedUrl)
                .addKeyValue(HTTP_REQUEST_TIME_TO_HEADERS_KEY, getDurationMs(startNanoTime, headersNanoTime))
                .addKeyValue(HTTP_RESPONSE_STATUS_CODE_KEY, response.getStatusCode())
                .addKeyValue(HTTP_RESPONSE_BODY_SIZE_KEY, contentLength);

            addHeadersToLogMessage(response.getHeaders(), log);

            if (httpLogDetailLevel.shouldLogBody() && canLogBody(response.getBody())) {
                // logResponse is called after body is requested and buffered, so it's safe to call getBody().toString() here.
                // not catching exception here, because it's caught in InstrumentedResponse.getBody() and will
                String content = response.getBody().toString();
                log.addKeyValue(HTTP_RESPONSE_BODY_KEY, content)
                   .addKeyValue(HTTP_REQUEST_DURATION_KEY, getDurationMs(startNanoTime, System.nanoTime()));
            }
            log.log();
        }

        @Override
        public void logException(ClientLogger logger, HttpRequest request, Response<?> response, Throwable throwable,
            long startNanoTime, Long headersNanoTime, String redactedUrl, int tryCount) {
            ClientLogger.LoggingEvent log = logger.atWarning();
            if (!log.isEnabled() || httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
                return;
            }

            log.setEventName(HTTP_RESPONSE_EVENT_NAME)
                .setContext(request.getRequestOptions().getContext())
                .addKeyValue(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod())
                .addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount)
                .addKeyValue(URL_FULL_KEY, redactedUrl);

            // exception could happen before response code and headers were received
            // or after that. So we may or may not have a response object.
            if (response != null) {
                log.addKeyValue(HTTP_RESPONSE_STATUS_CODE_KEY, response.getStatusCode());
                addHeadersToLogMessage(response.getHeaders(), log);

                if (headersNanoTime != null) {
                    log.addKeyValue(HTTP_REQUEST_TIME_TO_HEADERS_KEY,
                        getDurationMs(startNanoTime, headersNanoTime));
                }
            }

            // not logging body - there was an exception
            log.log(null, throwable);
        }
    }

    private double getDurationMs(long startNs, long endNs) {
        return (endNs - startNs) / 1_000_000.0;
    }

    /*
     * Adds HTTP headers into the StringBuilder that is generating the log message.
     *
     * @param headers HTTP headers on the request or response.
     * @param sb StringBuilder that is generating the log message.
     * @param logLevel Log level the environment is configured to use.
     */
    private void addHeadersToLogMessage(HttpHeaders headers, ClientLogger.LoggingEvent log) {
        if (httpLogDetailLevel.shouldLogHeaders()) {
            for (HttpHeader header : headers) {
                HttpHeaderName headerName = header.getName();
                String headerValue = allowedHeaderNames.contains(headerName) ? header.getValue() : REDACTED_PLACEHOLDER;
                log.addKeyValue(headerName.toString(), headerValue);
            }
        }
    }

    /*
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param logger Logger used to log a warning if the Content-Length header is an invalid number.
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return
     */
    private static long getContentLength(HttpHeaders headers) {
        long contentLength = 0;

        String contentLengthString = headers.getValue(HttpHeaderName.CONTENT_LENGTH);

        if (isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException | NullPointerException e) {
            LOGGER.atVerbose()
                .addKeyValue("contentLength", contentLengthString)
                .log("Could not parse the HTTP header content-length", e);
        }

        return contentLength;
    }

    /**
     * Determines if the request or response body should be logged.
     *
     * <p>The request or response body is logged if the Content-Type is not "application/octet-stream" and the body
     * isn't empty and is less than 16KB in size.</p>
     *
     * @param data The request or response body.
     * @return A flag indicating if the request or response body should be logged.
     */
    private static boolean canLogBody(BinaryData data) {
        return data != null && data.isReplayable() && data.getLength() != null && data.getLength() < MAX_BODY_LOG_SIZE;
    }

    private void instrumentResponse(HttpResponse<?> actualResponse, Span span, ClientLogger logger, long startNs,
        String sanitizedUrl, int tryCount) {
        long timeToHeadersNs = System.nanoTime();
        /*if (!httpLogDetailLevel.shouldLogBody()) {
            responseLogger.logResponse(logger, actualResponse, startNs, timeToHeadersNs, sanitizedUrl, tryCount);

            if (!span.isRecording()) {
                span.end();
                return;
            }
        }*/

        HttpResponseAccessHelper.setBody(actualResponse, new InstrumentedBinaryData(actualResponse.getBody(), error -> {
            if (error == null) {
                responseLogger.logResponse(logger, actualResponse, startNs, timeToHeadersNs, sanitizedUrl, tryCount);
                span.end();
            } else {
                responseLogger.logException(logger, actualResponse.getRequest(), actualResponse, error, startNs,
                    timeToHeadersNs, sanitizedUrl, tryCount);
                span.end(unwrap(error));
            }
        }));
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

    private static class InstrumentedBinaryData extends BinaryData {
        private final BinaryData inner;
        private final Consumer<Throwable> onComplete;
        private boolean reported;

        InstrumentedBinaryData(BinaryData inner, Consumer<Throwable> onComplete) {
            this.inner = inner;
            this.onComplete = onComplete;
        }

        @Override
        public byte[] toBytes() {
            try {
                byte[] bytes = inner.toBytes();
                report();
                return bytes;
            } catch (RuntimeException t) {
                report(t);
                throw t;
            }
        }

        @Override
        public String toString() {
            try {
                String str = inner.toString();
                report();
                return str;
            } catch (RuntimeException t) {
                onComplete.accept(t);
                throw t;
            }
        }

        @Override
        public <T> T toObject(Type type, ObjectSerializer serializer) throws IOException {
            try {
                T value = inner.toObject(type, serializer);
                report();
                return value;
            } catch (RuntimeException t) {
                report(t);
                throw t;
            }
        }

        @Override
        public InputStream toStream() {
            InputStream stream = inner.toStream();
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    try {
                        int read = stream.read();
                        if (read == -1) {
                            report();
                        }
                        return read;
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public int read(byte[] b) throws IOException {
                    try {
                        int read = stream.read(b);
                        if (read == -1) {
                            report();
                        }
                        return read;
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    try {
                        int read = stream.read(b, off, len);
                        if (read == -1) {
                            report();
                        }
                        return read;
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public byte[] readAllBytes() throws IOException {
                    try {
                        byte[] bytes = stream.readAllBytes();
                        report();
                        return bytes;
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public int readNBytes(byte[] b, int off, int len) throws IOException {
                    try {
                        int read = stream.readNBytes(b, off, len);
                        if (read < len) {
                            report();
                        }
                        return read;
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public byte[] readNBytes(int len) throws IOException {
                    try {
                        byte[] bytes = stream.readNBytes(len);
                        if (bytes.length < len) {
                            report();
                        }
                        return bytes;
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public void reset() throws IOException {
                    stream.reset();
                }

                @Override
                public long skip(long n) throws IOException {
                    try {
                        return stream.skip(n);
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public void skipNBytes(long n) throws IOException {
                    try {
                        stream.skipNBytes(n);
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public int available() throws IOException {
                    return stream.available();
                }

                @Override
                public void mark(int readlimit) {
                    stream.mark(readlimit);
                }

                @Override
                public boolean markSupported() {
                    return stream.markSupported();
                }

                @Override
                public void close() throws IOException {
                    try {
                        stream.close();
                        report();
                    } catch (IOException e) {
                        report(e);
                        throw e;
                    }
                }

                @Override
                public long transferTo(OutputStream out) throws IOException {
                    try {
                        long transferred = stream.transferTo(out);
                        report();
                        return transferred;
                    } catch (IOException | RuntimeException e) {
                        report(e);
                        throw e;
                    }
                }
            };
        }

        @Override
        public void writeTo(JsonWriter jsonWriter) throws IOException {
            try {
                inner.writeTo(jsonWriter);
                report();
            } catch (RuntimeException t) {
                report(t);
                throw t;
            }
        }

        @Override
        public ByteBuffer toByteBuffer() {
            try {
                ByteBuffer bb = inner.toByteBuffer();
                report();
                return bb;
            } catch (RuntimeException t) {
                report(t);
                throw t;
            }
        }

        @Override
        public Long getLength() {
            return inner.getLength();
        }

        @Override
        public boolean isReplayable() {
            return inner.isReplayable();
        }

        @Override
        public BinaryData toReplayableBinaryData() {
            if (inner.isReplayable()) {
                return this;
            }

            return new InstrumentedBinaryData(inner.toReplayableBinaryData(), onComplete);
        }

        @Override
        public void close() throws IOException {
            inner.close();
            report();
        }

        private void report() {
            report(null);
        }

        private void report(Throwable t) {
            if (!reported) {
                onComplete.accept(t);
                reported = true;
            }
        }
    }
}
