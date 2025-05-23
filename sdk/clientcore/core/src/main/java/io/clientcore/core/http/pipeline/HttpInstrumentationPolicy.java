// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.implementation.instrumentation.SdkInstrumentationOptionsAccessHelper;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.instrumentation.logging.LoggingEvent;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.clientcore.core.implementation.UrlRedactionUtil.getRedactedUri;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.ERROR_TYPE_KEY;
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
import static io.clientcore.core.implementation.instrumentation.InstrumentationUtils.getServerPort;
import static io.clientcore.core.implementation.instrumentation.LoggingEventNames.HTTP_REQUEST_EVENT_NAME;
import static io.clientcore.core.implementation.instrumentation.LoggingEventNames.HTTP_RESPONSE_EVENT_NAME;
import static io.clientcore.core.instrumentation.tracing.SpanKind.CLIENT;

/**
 * The {@link HttpInstrumentationPolicy} is responsible for instrumenting the HTTP request and response with distributed tracing
 * and (in the future) metrics following
 * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-spans.md">OpenTelemetry Semantic Conventions</a>.
 * <p>
 * It propagates context to the downstream service following <a href="https://www.w3.org/TR/trace-context-1/">W3C Trace Context</a> specification.
 * <p>
 * The {@link HttpInstrumentationPolicy} should be added to the HTTP pipeline by client libraries. It should be added
 * after {@link HttpRetryPolicy} and {@link HttpRedirectPolicy} so that it's executed on each try or redirect and
 * logging happens in the scope of the span.
 * <p>
 * The policy supports basic customizations using {@link HttpInstrumentationOptions}.
 * <p>
 * If your client library needs a different approach to distributed tracing,
 * you can create a custom policy and use it instead of the {@link HttpInstrumentationPolicy}. If you want to enrich instrumentation
 * policy spans with additional attributes, you can create a custom policy and add it under the {@link HttpInstrumentationPolicy}
 * so that it's executed in the scope of the span created by the {@link HttpInstrumentationPolicy}.
 *
 * <p><strong>Configure instrumentation policy:</strong></p>
 * <!-- src_embed io.clientcore.core.instrumentation.instrumentationpolicy -->
 * <pre>
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .addPolicy&#40;new HttpRetryPolicy&#40;&#41;&#41;
 *     .addPolicy&#40;new HttpInstrumentationPolicy&#40;instrumentationOptions&#41;&#41;
 *     .build&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.instrumentation.instrumentationpolicy -->
 *
 * <p><strong>Customize instrumentation policy:</strong></p>
 * <!-- src_embed io.clientcore.core.instrumentation.customizeinstrumentationpolicy -->
 * <pre>
 *
 * &#47;&#47; You can configure URL sanitization to include additional query parameters to preserve
 * &#47;&#47; in `url.full` attribute.
 * HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions&#40;&#41;;
 * instrumentationOptions.addAllowedQueryParamName&#40;&quot;documentId&quot;&#41;;
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .addPolicy&#40;new HttpRetryPolicy&#40;&#41;&#41;
 *     .addPolicy&#40;new HttpInstrumentationPolicy&#40;instrumentationOptions&#41;&#41;
 *     .build&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.instrumentation.customizeinstrumentationpolicy -->
 *
 * <p><strong>Enrich HTTP spans with additional attributes:</strong></p>
 * <!-- src_embed io.clientcore.core.instrumentation.enrichhttpspans -->
 * <pre>
 *
 * HttpPipelinePolicy enrichingPolicy = new HttpPipelinePolicy&#40;&#41; &#123;
 *     &#64;Override
 *     public Response&lt;BinaryData&gt; process&#40;HttpRequest request, HttpPipelineNextPolicy next&#41; &#123;
 *         Span span = request.getContext&#40;&#41;.getInstrumentationContext&#40;&#41;.getSpan&#40;&#41;;
 *         if &#40;span.isRecording&#40;&#41;&#41; &#123;
 *             span.setAttribute&#40;&quot;custom.request.id&quot;, request.getHeaders&#40;&#41;.getValue&#40;CUSTOM_REQUEST_ID&#41;&#41;;
 *         &#125;
 *
 *         return next.process&#40;&#41;;
 *     &#125;
 *
 *     &#64;Override
 *     public HttpPipelinePosition getPipelinePosition&#40;&#41; &#123;
 *         return HttpPipelinePosition.AFTER_INSTRUMENTATION;
 *     &#125;
 * &#125;;
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .addPolicy&#40;new HttpRetryPolicy&#40;&#41;&#41;
 *     .addPolicy&#40;new HttpInstrumentationPolicy&#40;instrumentationOptions&#41;&#41;
 *     .addPolicy&#40;enrichingPolicy&#41;
 *     .build&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.instrumentation.enrichhttpspans -->
 *
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class HttpInstrumentationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpInstrumentationPolicy.class);
    private static final HttpInstrumentationOptions DEFAULT_OPTIONS = new HttpInstrumentationOptions();
    private static final String SDK_NAME;
    private static final String SDK_VERSION;
    private static final SdkInstrumentationOptions SDK_OPTIONS;
    private static final TraceContextSetter<HttpHeaders> SETTER
        = (headers, name, value) -> headers.set(HttpHeaderName.fromString(name), value);

    static {
        Map<String, String> properties = getProperties("core.properties");
        SDK_NAME = properties.getOrDefault("name", "unknown");
        SDK_VERSION = properties.getOrDefault("version", "unknown");
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions(SDK_NAME).setSdkVersion(SDK_VERSION)
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        // HTTP tracing is special - we suppress nested public API spans, but
        // preserve nested HTTP ones.
        // We might want to make it configurable for other cases, but let's hide the API for now.
        SdkInstrumentationOptionsAccessHelper.disableSpanSuppression(sdkOptions);

        SDK_OPTIONS = sdkOptions;
    }

    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";

    // HTTP request duration metric is formally defined in the OpenTelemetry Semantic Conventions:
    // https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-metrics.md#metric-httpclientrequestduration
    private static final String REQUEST_DURATION_METRIC_NAME = "http.client.request.duration";
    private static final String REQUEST_DURATION_METRIC_DESCRIPTION = "Duration of HTTP client requests";
    private static final String REQUEST_DURATION_METRIC_UNIT = "s";
    // the histogram boundaries are optimized for typical HTTP request durations and could be customized by users on
    // the OTel side. These are the defaults documented in the OpenTelemetry Semantic Conventions (link above).
    private static final List<Double> REQUEST_DURATION_BOUNDARIES_ADVICE = Collections.unmodifiableList(
        Arrays.asList(0.005d, 0.01d, 0.025d, 0.05d, 0.075d, 0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d, 7.5d, 10d));

    // request log level is low (verbose) since almost all request details are also
    // captured on the response log.
    private static final LogLevel HTTP_REQUEST_LOG_LEVEL = LogLevel.VERBOSE;
    private static final LogLevel HTTP_RESPONSE_LOG_LEVEL = LogLevel.INFORMATIONAL;

    private final Tracer tracer;
    private final Meter meter;
    private final boolean isTracingEnabled;
    private final boolean isMetricsEnabled;
    private final Instrumentation instrumentation;
    private final DoubleHistogram httpRequestDuration;
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
        this.instrumentation = Instrumentation.create(instrumentationOptions, SDK_OPTIONS);
        this.tracer = instrumentation.getTracer();
        this.meter = instrumentation.getMeter();
        this.httpRequestDuration = meter.createDoubleHistogram(REQUEST_DURATION_METRIC_NAME,
            REQUEST_DURATION_METRIC_DESCRIPTION, REQUEST_DURATION_METRIC_UNIT, REQUEST_DURATION_BOUNDARIES_ADVICE);
        this.traceContextPropagator = instrumentation.getW3CTraceContextPropagator();

        HttpInstrumentationOptions optionsToUse
            = instrumentationOptions == null ? DEFAULT_OPTIONS : instrumentationOptions;
        this.isLoggingEnabled = optionsToUse.getHttpLogLevel() != HttpInstrumentationOptions.HttpLogLevel.NONE;
        this.isContentLoggingEnabled = optionsToUse.getHttpLogLevel() == HttpInstrumentationOptions.HttpLogLevel.BODY
            || optionsToUse.getHttpLogLevel() == HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS;
        this.isRedactedHeadersLoggingEnabled = optionsToUse.isRedactedHeaderNamesLoggingEnabled();
        this.allowedHeaderNames = optionsToUse.getAllowedHeaderNames();
        this.allowedQueryParameterNames = optionsToUse.getAllowedQueryParamNames()
            .stream()
            .map(queryParamName -> queryParamName.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        this.isTracingEnabled = tracer.isEnabled();
        this.isMetricsEnabled = meter.isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("try")
    @Override
    public Response<BinaryData> process(HttpRequest request, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled && !isLoggingEnabled && !isMetricsEnabled) {
            return next.process();
        }

        ClientLogger logger = getLogger(request);
        final long startNs = System.nanoTime();
        final String redactedUrl = getRedactedUri(request.getUri(), allowedQueryParameterNames);
        final int tryCount = HttpRequestAccessHelper.getTryCount(request);
        final long requestContentLength = getContentLength(logger, request.getBody(), request.getHeaders(), true);

        Map<String, Object> metricAttributes = isMetricsEnabled ? new HashMap<>(8) : null;

        InstrumentationContext parentContext = request.getContext().getInstrumentationContext();

        SpanBuilder spanBuilder = tracer.spanBuilder(request.getHttpMethod().toString(), CLIENT, parentContext);
        setStartAttributes(request, redactedUrl, spanBuilder, metricAttributes);
        Span span = spanBuilder.startSpan();

        InstrumentationContext currentContext
            = span.getInstrumentationContext().isValid() ? span.getInstrumentationContext() : parentContext;

        if (currentContext != null && currentContext.isValid()) {
            request.setContext(request.getContext().toBuilder().setInstrumentationContext(currentContext).build());
            // even if tracing is disabled, we could have a valid context to propagate
            // if it was provided by the application explicitly.
            traceContextPropagator.inject(currentContext, request.getHeaders(), SETTER);
        }

        logRequest(logger, request, startNs, requestContentLength, redactedUrl, tryCount, currentContext);

        try (TracingScope scope = span.makeCurrent()) {
            Response<BinaryData> response = next.process();

            if (response == null) {
                LOGGER.atError()
                    .setInstrumentationContext(span.getInstrumentationContext())
                    .addKeyValue(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod())
                    .addKeyValue(URL_FULL_KEY, redactedUrl)
                    .log(
                        "HTTP response is null and no exception is thrown. Please report it to the client library maintainers.");

                return null;
            }

            addDetails(request, response.getStatusCode(), tryCount, span, metricAttributes);
            response
                = logResponse(logger, response, startNs, requestContentLength, redactedUrl, tryCount, currentContext);
            span.end();
            return response;
        } catch (RuntimeException t) {
            Throwable cause = unwrap(t);
            if (metricAttributes != null) {
                metricAttributes.put(ERROR_TYPE_KEY, cause.getClass().getCanonicalName());
            }
            span.end(cause);
            throw logException(logger, request, null, t, startNs, null, requestContentLength, redactedUrl, tryCount,
                currentContext);
        } finally {
            if (isMetricsEnabled) {
                httpRequestDuration.record((System.nanoTime() - startNs) / 1_000_000_000.0,
                    instrumentation.createAttributes(metricAttributes), currentContext);
            }
        }
    }

    private void setStartAttributes(HttpRequest request, String sanitizedUrl, SpanBuilder spanBuilder,
        Map<String, Object> metricAttributes) {
        if (!isTracingEnabled && !isMetricsEnabled) {
            return;
        }

        int port = getServerPort(request.getUri());
        if (isTracingEnabled) {
            spanBuilder.setAttribute(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod().toString())
                .setAttribute(URL_FULL_KEY, sanitizedUrl)
                .setAttribute(SERVER_ADDRESS_KEY, request.getUri().getHost());

            if (port > 0) {
                spanBuilder.setAttribute(SERVER_PORT_KEY, port);
            }
        }

        if (isMetricsEnabled) {
            metricAttributes.put(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod().toString());
            metricAttributes.put(SERVER_ADDRESS_KEY, request.getUri().getHost());
            if (port > 0) {
                metricAttributes.put(SERVER_PORT_KEY, port);
            }
        }
    }

    private void addDetails(HttpRequest request, int statusCode, int tryCount, Span span,
        Map<String, Object> metricAttributes) {
        if (!span.isRecording() && !isMetricsEnabled) {
            return;
        }

        String error = null;
        if (statusCode >= 400) {
            error = String.valueOf(statusCode);
        }

        if (span.isRecording()) {
            span.setAttribute(HTTP_RESPONSE_STATUS_CODE_KEY, (long) statusCode);

            if (tryCount > 0) {
                span.setAttribute(HTTP_REQUEST_RESEND_COUNT_KEY, (long) tryCount);
            }

            String userAgent = request.getHeaders().getValue(HttpHeaderName.USER_AGENT);
            if (userAgent != null) {
                span.setAttribute(USER_AGENT_ORIGINAL_KEY, userAgent);
            }

            if (error != null) {
                span.setError(error);
            }
        }

        if (isMetricsEnabled) {
            if (statusCode > 0) {
                metricAttributes.put(HTTP_RESPONSE_STATUS_CODE_KEY, statusCode);
            }

            if (error != null) {
                metricAttributes.put(ERROR_TYPE_KEY, error);
            }
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

        if (httpRequest.getContext() != null && httpRequest.getContext().getLogger() != null) {
            logger = httpRequest.getContext().getLogger();
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
                .setThrowable(ex)
                .log("Failed to read properties.");
        }

        return Collections.emptyMap();
    }

    private void logRequest(ClientLogger logger, HttpRequest request, long startNanoTime, long requestContentLength,
        String redactedUrl, int tryCount, InstrumentationContext context) {
        LoggingEvent logBuilder = logger.atLevel(HTTP_REQUEST_LOG_LEVEL);
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

    private Response<BinaryData> logResponse(ClientLogger logger, Response<BinaryData> response, long startNanoTime,
        long requestContentLength, String redactedUrl, int tryCount, InstrumentationContext context) {
        LoggingEvent logBuilder = logger.atLevel(HTTP_RESPONSE_LOG_LEVEL);
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
                    getContentLength(logger, response.getValue(), response.getHeaders(), false));

            addHeadersToLogMessage(response.getHeaders(), logBuilder);
        }

        if (isContentLoggingEnabled && canLogBody(response.getValue())) {
            return new LoggingHttpResponse(response, content -> {
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

    private <T extends Throwable> T logException(ClientLogger logger, HttpRequest request,
        Response<BinaryData> response, T throwable, long startNanoTime, Long responseStartNanoTime,
        long requestContentLength, String redactedUrl, int tryCount, InstrumentationContext context) {

        LoggingEvent log = logger.atLevel(LogLevel.WARNING);
        if (!log.isEnabled() || !isLoggingEnabled) {
            return throwable;
        }

        log.setThrowable(unwrap(throwable))
            .setEventName(HTTP_RESPONSE_EVENT_NAME)
            .setInstrumentationContext(context)
            .addKeyValue(HTTP_REQUEST_METHOD_KEY, request.getHttpMethod())
            .addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount)
            .addKeyValue(URL_FULL_KEY, redactedUrl)
            .addKeyValue(HTTP_REQUEST_BODY_SIZE_KEY, requestContentLength)
            .addKeyValue(HTTP_REQUEST_DURATION_KEY, getDurationMs(startNanoTime, System.nanoTime()));

        if (response != null) {
            addHeadersToLogMessage(response.getHeaders(), log);
            log.addKeyValue(HTTP_RESPONSE_BODY_SIZE_KEY,
                getContentLength(logger, response.getValue(), response.getHeaders(), false))
                .addKeyValue(HTTP_RESPONSE_STATUS_CODE_KEY, response.getStatusCode());

            if (responseStartNanoTime != null) {
                log.addKeyValue(HTTP_REQUEST_TIME_TO_RESPONSE_KEY, getDurationMs(startNanoTime, responseStartNanoTime));
            }
        }

        log.log();
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
        // TODO (limolkova) we might want to filter out binary data, but if somebody enabled logging it - why not log it?
        return data != null && data.getLength() != null && data.getLength() > 0 && data.getLength() < MAX_BODY_LOG_SIZE;
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the log message.
     *
     * @param headers HTTP headers on the request or response.
     * @param logBuilder Log message builder.
     */
    private void addHeadersToLogMessage(HttpHeaders headers, LoggingEvent logBuilder) {
        headers.stream().forEach(header -> {
            HttpHeaderName headerName = header.getName();
            if (allowedHeaderNames.contains(headerName)) {
                logBuilder.addKeyValue(headerName.toString(), header.getValue());
            } else if (isRedactedHeadersLoggingEnabled) {
                logBuilder.addKeyValue(headerName.toString(), REDACTED_PLACEHOLDER);
            }
        });
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

        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException e) {
            logger.atVerbose()
                .addKeyValue(
                    isRequest ? HTTP_REQUEST_HEADER_CONTENT_LENGTH_KEY : HTTP_RESPONSE_HEADER_CONTENT_LENGTH_KEY,
                    contentLengthString)
                .setThrowable(e)
                .log("Could not parse the HTTP header content-length");
        }

        return contentLength;
    }

    private static final class LoggingHttpResponse extends Response<BinaryData> {
        private final Consumer<BinaryData> onContent;
        private final Consumer<Throwable> onException;
        private final BinaryData originalBody;
        private BinaryData bufferedBody;

        private LoggingHttpResponse(Response<BinaryData> actualResponse, Consumer<BinaryData> onContent,
            Consumer<Throwable> onException) {
            super(actualResponse.getRequest(), actualResponse.getStatusCode(), actualResponse.getHeaders(),
                actualResponse.getValue());

            this.onContent = onContent;
            this.onException = onException;
            this.originalBody = actualResponse.getValue();
        }

        @Override
        public BinaryData getValue() {
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
        public void close() {
            if (bufferedBody == null) {
                getValue();
            }
            if (bufferedBody != null) {
                bufferedBody.close();
            }
            originalBody.close();
        }
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.INSTRUMENTATION;
    }
}
