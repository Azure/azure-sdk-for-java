// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.implementation.http.UrlSanitizer;
import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.implementation.logging.LoggingKeys;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.ByteBufferContent;
import com.azure.core.implementation.util.HttpHeadersAccessHelper;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.logging.LoggingEventBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.core.http.HttpHeaderName.CONTENT_LENGTH;
import static com.azure.core.http.HttpHeaderName.TRACEPARENT;
import static com.azure.core.http.HttpHeaderName.X_MS_CLIENT_REQUEST_ID;

/**
 * The {@code HttpLoggingPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface.
 * This policy handles logging of HTTP requests and responses based on the provided {@link HttpLogOptions}.
 *
 * <p>This class is useful when you need to log HTTP traffic for debugging or auditing purposes. It allows you to
 * control the amount of information that is logged, including the URL, headers, and body of requests and responses.</p>
 *
 * <p><b>NOTE:</b> Enabling body logging (using the {@link HttpLogDetailLevel#BODY BODY} or
 * {@link HttpLogDetailLevel#BODY_AND_HEADERS BODY_AND_HEADERS} levels) will buffer the response body into memory even
 * if it is never consumed by your application, possibly impacting performance.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, an {@code HttpLogOptions} is created and the log level is set to
 * {@code HttpLogDetailLevel.BODY_AND_HEADERS}. This means that the URL, HTTP method, headers, and body content of
 * each request and response will be logged. The {@code HttpLogOptions} is then used to create an
 * {@code HttpLoggingPolicy}, which can then added to the pipeline.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.HttpLoggingPolicy.constructor -->
 * <pre>
 * HttpLogOptions logOptions = new HttpLogOptions&#40;&#41;;
 * logOptions.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;;
 * HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy&#40;logOptions&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.HttpLoggingPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 * @see com.azure.core.http.policy.HttpLogOptions
 * @see com.azure.core.http.policy.HttpLogDetailLevel
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final ObjectMapperShim PRETTY_PRINTER = ObjectMapperShim.createPrettyPrintMapper();
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;

    // Use a cache to retain the caller method ClientLogger.
    //
    // The same method may be called thousands or millions of times, so it is wasteful to create a new logger instance
    // each time the method is called. Instead, retain the created ClientLogger until a certain number of unique method
    // calls have been made and then clear the cache and rebuild it. Long term, this should be replaced with an LRU,
    // or another type of cache, for better cache management.
    private static final int LOGGER_CACHE_MAX_SIZE = 1000;
    private static final String CONTENT_LENGTH_KEY = CONTENT_LENGTH.getCaseInsensitiveName();
    private static final Map<String, ClientLogger> CALLER_METHOD_LOGGER_CACHE = new ConcurrentHashMap<>();

    private static final ClientLogger LOGGER = new ClientLogger(HttpLoggingPolicy.class);

    private final HttpLogDetailLevel httpLogDetailLevel;
    private final Set<String> allowedHeaderNames;
    private final UrlSanitizer urlSanitizer;
    private final boolean prettyPrintBody;
    private final boolean disableRedactedHeaderLogging;
    private final HttpRequestLogger requestLogger;
    private final HttpResponseLogger responseLogger;

    /**
     * Key for {@link Context} to pass request retry count metadata for logging.
     */
    public static final String RETRY_COUNT_CONTEXT = "requestRetryCount";

    private static final String REQUEST_LOG_MESSAGE = "HTTP request";
    private static final String RESPONSE_LOG_MESSAGE = "HTTP response";

    /**
     * Creates an HttpLoggingPolicy with the given log configurations.
     *
     * @param httpLogOptions The HTTP logging configuration options.
     */
    public HttpLoggingPolicy(HttpLogOptions httpLogOptions) {
        if (httpLogOptions == null) {
            this.httpLogDetailLevel = HttpLogDetailLevel.ENVIRONMENT_HTTP_LOG_DETAIL_LEVEL;
            this.allowedHeaderNames = HttpLogOptions.DEFAULT_HEADERS_ALLOWLIST.stream()
                .map(headerName -> headerName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
            this.urlSanitizer = new UrlSanitizer(null);
            this.prettyPrintBody = false;
            this.disableRedactedHeaderLogging = false;

            this.requestLogger = new DefaultHttpRequestLogger();
            this.responseLogger = new DefaultHttpResponseLogger();
        } else {
            this.httpLogDetailLevel = httpLogOptions.getLogLevel();
            this.allowedHeaderNames = httpLogOptions.getAllowedHeaderNames()
                .stream()
                .map(headerName -> headerName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
            this.urlSanitizer = new UrlSanitizer(httpLogOptions.getAllowedQueryParamNames());
            this.prettyPrintBody = httpLogOptions.isPrettyPrintBody();
            this.disableRedactedHeaderLogging = httpLogOptions.isRedactedHeaderLoggingDisabled();

            this.requestLogger = (httpLogOptions.getRequestLogger() == null)
                ? new DefaultHttpRequestLogger()
                : httpLogOptions.getRequestLogger();
            this.responseLogger = (httpLogOptions.getResponseLogger() == null)
                ? new DefaultHttpResponseLogger()
                : httpLogOptions.getResponseLogger();
        }
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // No logging will be performed, trigger a no-op.
        if (httpLogDetailLevel == HttpLogDetailLevel.NONE) {
            return next.process();
        }

        final ClientLogger logger = getOrCreateMethodLogger(context.getContext());
        final long startNs = System.nanoTime();

        return requestLogger.logRequest(logger, getRequestLoggingOptions(context))
            .then(next.process())
            .flatMap(
                response -> responseLogger.logResponse(logger, getResponseLoggingOptions(response, startNs, context)))
            .doOnError(throwable -> createBasicLoggingContext(logger, LogLevel.WARNING, context.getHttpRequest())
                .log("HTTP FAILED", throwable));
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        // No logging will be performed, trigger a no-op.
        if (httpLogDetailLevel == HttpLogDetailLevel.NONE) {
            return next.processSync();
        }

        final ClientLogger logger = getOrCreateMethodLogger(context.getContext());
        final long startNs = System.nanoTime();

        requestLogger.logRequestSync(logger, getRequestLoggingOptions(context));
        try {
            HttpResponse response = next.processSync();
            if (response != null) {
                response
                    = responseLogger.logResponseSync(logger, getResponseLoggingOptions(response, startNs, context));
            }
            return response;
        } catch (RuntimeException e) {
            createBasicLoggingContext(logger, LogLevel.WARNING, context.getHttpRequest()).log("HTTP FAILED", e);
            throw e;
        }
    }

    private LoggingEventBuilder createBasicLoggingContext(ClientLogger logger, LogLevel level, HttpRequest request) {
        LoggingEventBuilder log = logger.atLevel(level);
        if (LOGGER.canLogAtLevel(level) && request != null) {
            if (allowedHeaderNames.contains(X_MS_CLIENT_REQUEST_ID.getCaseInsensitiveName())) {
                String clientRequestId = request.getHeaders().getValue(X_MS_CLIENT_REQUEST_ID);
                if (clientRequestId != null) {
                    log.addKeyValue(X_MS_CLIENT_REQUEST_ID.getCaseInsensitiveName(), clientRequestId);
                }
            }

            if (allowedHeaderNames.contains(TRACEPARENT.getCaseInsensitiveName())) {
                String traceparent = request.getHeaders().getValue(TRACEPARENT);
                if (traceparent != null) {
                    log.addKeyValue(TRACEPARENT.getCaseInsensitiveName(), traceparent);
                }
            }
        }

        return log;
    }

    private HttpRequestLoggingContext getRequestLoggingOptions(HttpPipelineCallContext callContext) {
        return new HttpRequestLoggingContext(callContext.getHttpRequest(), callContext.getContext(),
            getRequestRetryCount(callContext.getContext()));
    }

    private HttpResponseLoggingContext getResponseLoggingOptions(HttpResponse httpResponse, long startNs,
        HttpPipelineCallContext callContext) {
        return new HttpResponseLoggingContext(httpResponse, Duration.ofNanos(System.nanoTime() - startNs),
            callContext.getContext(), getRequestRetryCount(callContext.getContext()));
    }

    private final class DefaultHttpRequestLogger implements HttpRequestLogger {
        @Override
        public Mono<Void> logRequest(ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
            logRequestSync(logger, loggingOptions);

            return Mono.empty();
        }

        @Override
        public void logRequestSync(ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
            log(getLogLevel(loggingOptions), logger, loggingOptions);
        }

        private void log(LogLevel logLevel, ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
            if (!logger.canLogAtLevel(logLevel) || httpLogDetailLevel == HttpLogDetailLevel.NONE) {
                return;
            }

            final HttpRequest request = loggingOptions.getHttpRequest();

            LoggingEventBuilder logBuilder
                = getLogBuilder(logLevel, logger).addKeyValue(LoggingKeys.HTTP_METHOD_KEY, request.getHttpMethod())
                    .addKeyValue(LoggingKeys.URL_KEY, urlSanitizer.getRedactedUrl(request.getUrl()));

            Integer retryCount = loggingOptions.getTryCount();

            if (retryCount != null) {
                logBuilder.addKeyValue(LoggingKeys.TRY_COUNT_KEY, retryCount);
            }

            if (httpLogDetailLevel.shouldLogHeaders() && logger.canLogAtLevel(LogLevel.INFORMATIONAL)) {
                addHeadersToLogMessage(allowedHeaderNames, request.getHeaders(), logBuilder,
                    disableRedactedHeaderLogging);
            }

            Long contentLength = getAndLogContentLength(request.getHeaders(), logBuilder, logger);

            if (request.getBody() == null) {
                logBuilder.log(REQUEST_LOG_MESSAGE);

                return;
            }

            String contentType = request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);

            if (httpLogDetailLevel.shouldLogBody() && shouldBodyBeLogged(contentType, contentLength)) {
                // shouldBodyBeLogged ensures contentLength is not null and within limits.
                int contentLengthInt = contentLength.intValue();

                logBody(request, contentLengthInt, logBuilder, logger, contentType);

                return;
            }

            logBuilder.log(REQUEST_LOG_MESSAGE);
        }
    }

    private void logBody(HttpRequest request, int contentLength, LoggingEventBuilder logBuilder, ClientLogger logger,
        String contentType) {
        BinaryData data = request.getBodyAsBinaryData();
        BinaryDataContent content = BinaryDataHelper.getContent(data);

        if (content instanceof StringContent
            || content instanceof ByteBufferContent
            || content instanceof SerializableContent
            || content instanceof ByteArrayContent) {

            logBody(logBuilder, logger, contentType, content.toString());
        } else if (content instanceof InputStreamContent) {
            // TODO (limolkova) Implement sync version with logging stream wrapper
            byte[] contentBytes = content.toBytes();

            request.setBody(contentBytes);
            logBody(logBuilder, logger, contentType, new String(contentBytes, StandardCharsets.UTF_8));
        } else {
            // Add non-mutating operators to the data stream.
            AccessibleByteArrayOutputStream stream = new AccessibleByteArrayOutputStream(contentLength);

            request.setBody(Flux.using(() -> stream, s -> content.toFluxByteBuffer().doOnNext(byteBuffer -> {
                try {
                    ImplUtils.writeByteBufferToStream(byteBuffer.duplicate(), s);
                } catch (IOException ex) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
                }
            }), s -> logBody(logBuilder, logger, contentType, s.toString(StandardCharsets.UTF_8))));
        }
    }

    private void logBody(LoggingEventBuilder logBuilder, ClientLogger logger, String contentType, String data) {
        logBuilder.addKeyValue(LoggingKeys.BODY_KEY, prettyPrintIfNeeded(logger, prettyPrintBody, contentType, data))
            .log(REQUEST_LOG_MESSAGE);
    }

    private final class DefaultHttpResponseLogger implements HttpResponseLogger {
        @Override
        public Mono<HttpResponse> logResponse(ClientLogger logger, HttpResponseLoggingContext loggingOptions) {
            final LogLevel logLevel = getLogLevel(loggingOptions);
            HttpResponse response = loggingOptions.getHttpResponse();

            if (!logger.canLogAtLevel(logLevel) || httpLogDetailLevel == HttpLogDetailLevel.NONE) {
                return Mono.just(response);
            }

            LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

            addBasicResponseProperties(logger, loggingOptions, response, logBuilder);

            Long contentLength = getAndLogContentLength(response.getHeaders(), logBuilder, logger);
            Mono<HttpResponse> responseMono = Mono.just(response);

            if (httpLogDetailLevel.shouldLogBody()) {
                String contentTypeHeader = response.getHeaderValue(HttpHeaderName.CONTENT_TYPE);

                if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
                    // Make sure we buffer the response body to avoid keeping the connection open.
                    final HttpResponse bufferedResponse = response.buffer();

                    responseMono = FluxUtil.collectBytesInByteBufferStream(bufferedResponse.getBody()).map(bytes -> {
                        logBuilder.addKeyValue(LoggingKeys.BODY_KEY, prettyPrintIfNeeded(logger, prettyPrintBody,
                            contentTypeHeader, new String(bytes, StandardCharsets.UTF_8)));

                        return bufferedResponse;
                    });
                }
            }

            return responseMono.doOnNext(ignored -> logBuilder.log(RESPONSE_LOG_MESSAGE));
        }

        private void logHeaders(ClientLogger logger, HttpResponse response, LoggingEventBuilder logBuilder) {
            if (httpLogDetailLevel.shouldLogHeaders() && logger.canLogAtLevel(LogLevel.INFORMATIONAL)) {
                addHeadersToLogMessage(allowedHeaderNames, response.getHeaders(), logBuilder,
                    disableRedactedHeaderLogging);
            }
        }

        private void addBasicResponseProperties(ClientLogger logger, HttpResponseLoggingContext loggingOptions,
            HttpResponse response, LoggingEventBuilder logBuilder) {
            logBuilder.addKeyValue(LoggingKeys.STATUS_CODE_KEY, response.getStatusCode())
                .addKeyValue(LoggingKeys.URL_KEY, urlSanitizer.getRedactedUrl(response.getRequest().getUrl()))
                .addKeyValue(LoggingKeys.DURATION_MS_KEY, loggingOptions.getResponseDuration().toMillis());

            getAndLogContentLength(response.getHeaders(), logBuilder, logger);
            logHeaders(logger, response, logBuilder);
        }

        @Override
        public HttpResponse logResponseSync(ClientLogger logger, HttpResponseLoggingContext loggingOptions) {
            final LogLevel logLevel = getLogLevel(loggingOptions);
            HttpResponse response = loggingOptions.getHttpResponse();

            if (!logger.canLogAtLevel(logLevel)) {
                return response;
            }

            LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

            addBasicResponseProperties(logger, loggingOptions, response, logBuilder);

            Long contentLength = getAndLogContentLength(response.getHeaders(), logBuilder, logger);

            if (httpLogDetailLevel.shouldLogBody()) {
                String contentTypeHeader = response.getHeaderValue(HttpHeaderName.CONTENT_TYPE);

                if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
                    // Make sure we buffer the response body to avoid keeping the connection open.
                    response = response.buffer();

                    logBuilder.addKeyValue(LoggingKeys.BODY_KEY, prettyPrintIfNeeded(logger, prettyPrintBody,
                        contentTypeHeader, response.getBodyAsBinaryData().toString()));
                }
            }

            logBuilder.log(RESPONSE_LOG_MESSAGE);

            return response;
        }
    }

    /*
     * Adds HTTP headers into the StringBuilder that is generating the log message.
     *
     * @param headers HTTP headers on the request or response.
     *
     * @param sb StringBuilder that is generating the log message.
     *
     * @param logLevel Log level the environment is configured to use.
     */
    private static void addHeadersToLogMessage(Set<String> allowedHeaderNames, HttpHeaders headers,
        LoggingEventBuilder logBuilder, boolean disableRedactedHeaderLogging) {

        final StringBuilder redactedHeaders = new StringBuilder();

        // The raw header map uses keys that are already lower-cased.
        HttpHeadersAccessHelper.getRawHeaderMap(headers).forEach((key, value) -> {
            if (CONTENT_LENGTH_KEY.equals(key)) {
                return;
            }

            if (allowedHeaderNames.contains(key)) {
                logBuilder.addKeyValue(value.getName(), value.getValue());
            } else if (!disableRedactedHeaderLogging) {
                if (redactedHeaders.length() > 0) {
                    redactedHeaders.append(',');
                }

                redactedHeaders.append(value.getName());
            }
        });

        if (redactedHeaders.length() > 0) {
            logBuilder.addKeyValue("redactedHeaders", redactedHeaders.toString());
        }
    }

    /*
     * Determines and attempts to pretty print the body if it is JSON.
     *
     * <p>The body is pretty printed if the Content-Type is JSON and the policy is configured to pretty print JSON.</p>
     *
     * @param logger Logger used to log a warning if the body fails to pretty print as JSON.
     *
     * @param contentType Content-Type header.
     *
     * @param body Body of the request or response.
     *
     * @return The body pretty printed if it is JSON, otherwise the unmodified body.
     */
    private static String prettyPrintIfNeeded(ClientLogger logger, boolean prettyPrintBody, String contentType,
        String body) {
        String result = body;

        if (prettyPrintBody
            && contentType != null
            && (contentType.startsWith(ContentType.APPLICATION_JSON) || contentType.startsWith("text/json"))) {

            try {
                final Object deserialized = PRETTY_PRINTER.readTree(body);
                result = PRETTY_PRINTER.writeValueAsString(deserialized);
            } catch (Exception e) {
                logger.log(LogLevel.WARNING, () -> "Failed to pretty print JSON", e);
            }
        }

        return result;
    }

    /*
     * Attempts to retrieve, log, and parse the Content-Length header into a numeric representation.
     *
     * @param logger Logger used to log Content-Length header value.
     *
     * @param headers HTTP headers that are checked for containing Content-Length.
     *
     * @return
     */
    private Long getAndLogContentLength(HttpHeaders headers, LoggingEventBuilder logBuilder, ClientLogger logger) {
        String contentLengthString = headers.getValue(HttpHeaderName.CONTENT_LENGTH);

        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            return null;
        }

        try {
            Long contentLength = Long.parseLong(contentLengthString);

            logBuilder.addKeyValue(CONTENT_LENGTH_KEY, contentLength);

            return contentLength;
        } catch (NumberFormatException e) {
            logger.atInfo()
                .addKeyValue(CONTENT_LENGTH_KEY, contentLengthString)
                .log("Could not parse the HTTP header content-length", e);

            return null;
        }
    }

    /*
     * Determines if the request or response body should be logged.
     *
     * <p>The request or response body is logged if the Content-Type is not "application/octet-stream" and the body
     * isn't empty and is less than 16KB in size.</p>
     *
     * @param contentTypeHeader Content-Type header value.
     *
     * @param contentLength Content-Length header represented as a numeric.
     *
     * @return A flag indicating if the request or response body should be logged.
     */
    private static boolean shouldBodyBeLogged(String contentTypeHeader, Long contentLength) {
        return contentLength != null
            && !ContentType.APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
            && contentLength != 0
            && contentLength < MAX_BODY_LOG_SIZE;
    }

    /*
     * Gets the request retry count to include in logging.
     *
     * If there is no value set, or it isn't a valid number null will be returned indicating that retry count won't be
     * logged.
     */
    private static Integer getRequestRetryCount(Context context) {
        Object rawRetryCount = context.getData(RETRY_COUNT_CONTEXT).orElse(null);

        if (rawRetryCount == null) {
            return null;
        }

        try {
            return Integer.valueOf(rawRetryCount.toString());
        } catch (NumberFormatException ex) {
            LOGGER.atInfo()
                .addKeyValue(LoggingKeys.TRY_COUNT_KEY, rawRetryCount)
                .log("Could not parse the request retry count.");

            return null;
        }
    }

    /*
     * Get or create the ClientLogger for the method having its request and response logged.
     */
    private static ClientLogger getOrCreateMethodLogger(Context context) {
        // caller-method-logger is a new way to handle this where the caller passes a full-fledged ClientLogger through
        // Context rather than the name of the logger. This way HttpLoggingPolicy doesn't need to keep track of
        // ClientLogger instances.
        ClientLogger logger = (ClientLogger) context.getData("caller-method-logger").orElse(null);

        if (logger != null) {
            return logger;
        }

        String methodName = (String) context.getData("caller-method").orElse("");

        if (CALLER_METHOD_LOGGER_CACHE.size() > LOGGER_CACHE_MAX_SIZE) {
            CALLER_METHOD_LOGGER_CACHE.clear();
        }

        return CALLER_METHOD_LOGGER_CACHE.computeIfAbsent(methodName, ClientLogger::new);
    }

    private static LoggingEventBuilder getLogBuilder(LogLevel logLevel, ClientLogger logger) {
        switch (logLevel) {
            case ERROR:
                return logger.atError();

            case WARNING:
                return logger.atWarning();

            case INFORMATIONAL:
                return logger.atInfo();

            case VERBOSE:
            default:
                return logger.atVerbose();
        }
    }
}
