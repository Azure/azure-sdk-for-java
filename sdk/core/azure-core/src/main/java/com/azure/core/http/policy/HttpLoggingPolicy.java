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
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.logging.LoggingEventBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.core.http.HttpHeaderName.TRACEPARENT;
import static com.azure.core.http.HttpHeaderName.X_MS_CLIENT_REQUEST_ID;

/**
 * The pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final ObjectMapperShim PRETTY_PRINTER = ObjectMapperShim.createPrettyPrintMapper();
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";

    // Use a cache to retain the caller method ClientLogger.
    //
    // The same method may be called thousands or millions of times, so it is wasteful to create a new logger instance
    // each time the method is called. Instead, retain the created ClientLogger until a certain number of unique method
    // calls have been made and then clear the cache and rebuild it. Long term, this should be replaced with an LRU,
    // or another type of cache, for better cache management.
    private static final int LOGGER_CACHE_MAX_SIZE = 1000;
    private static final Map<String, ClientLogger> CALLER_METHOD_LOGGER_CACHE = new ConcurrentHashMap<>();

    private static final ClientLogger LOGGER = new ClientLogger(HttpLoggingPolicy.class);

    private final HttpLogDetailLevel httpLogDetailLevel;
    private final Set<String> allowedHeaderNames;
    private final Set<String> allowedQueryParameterNames;
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
            this.allowedQueryParameterNames = HttpLogOptions.DEFAULT_QUERY_PARAMS_ALLOWLIST.stream()
                .map(queryParamName -> queryParamName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
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
            this.allowedQueryParameterNames = httpLogOptions.getAllowedQueryParamNames()
                .stream()
                .map(queryParamName -> queryParamName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
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

        final ClientLogger logger = getOrCreateMethodLogger((String) context.getData("caller-method").orElse(""));
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

        final ClientLogger logger = getOrCreateMethodLogger((String) context.getData("caller-method").orElse(""));
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
            final LogLevel logLevel = getLogLevel(loggingOptions);
            if (logger.canLogAtLevel(logLevel)) {
                log(logLevel, logger, loggingOptions);
            }

            return Mono.empty();
        }

        @Override
        public void logRequestSync(ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
            final LogLevel logLevel = getLogLevel(loggingOptions);
            if (logger.canLogAtLevel(logLevel)) {
                log(logLevel, logger, loggingOptions);
            }
        }

        private void log(LogLevel logLevel, ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
            final HttpRequest request = loggingOptions.getHttpRequest();
            LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

            if (httpLogDetailLevel.shouldLogUrl()) {
                logBuilder.addKeyValue(LoggingKeys.HTTP_METHOD_KEY, request.getHttpMethod())
                    .addKeyValue(LoggingKeys.URL_KEY, getRedactedUrl(request.getUrl(), allowedQueryParameterNames));

                Integer retryCount = loggingOptions.getTryCount();
                if (retryCount != null) {
                    logBuilder.addKeyValue(LoggingKeys.TRY_COUNT_KEY, retryCount);
                }
            }

            if (httpLogDetailLevel.shouldLogHeaders() && logger.canLogAtLevel(LogLevel.INFORMATIONAL)) {
                addHeadersToLogMessage(allowedHeaderNames, request.getHeaders(), logBuilder,
                    disableRedactedHeaderLogging);
            }

            if (request.getBody() == null) {
                logBuilder.addKeyValue(LoggingKeys.CONTENT_LENGTH_KEY, 0).log(REQUEST_LOG_MESSAGE);
                return;
            }

            String contentType = request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);
            long contentLength = getContentLength(logger, request.getHeaders());

            logBuilder.addKeyValue(LoggingKeys.CONTENT_LENGTH_KEY, contentLength);

            if (httpLogDetailLevel.shouldLogBody() && shouldBodyBeLogged(contentType, contentLength)) {
                logBody(request, (int) contentLength, logBuilder, logger, contentType);
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
            final HttpResponse response = loggingOptions.getHttpResponse();

            if (!logger.canLogAtLevel(logLevel)) {
                return Mono.just(response);
            }

            LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

            logContentLength(response, logBuilder);

            logUrl(loggingOptions, response, logBuilder);

            logHeaders(logger, response, logBuilder);

            if (httpLogDetailLevel.shouldLogBody()) {
                String contentTypeHeader = response.getHeaderValue(HttpHeaderName.CONTENT_TYPE);
                long contentLength = getContentLength(logger, response.getHeaders());
                if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
                    return Mono.just(new LoggingHttpResponse(response, logBuilder, logger, (int) contentLength,
                        contentTypeHeader, prettyPrintBody));
                }
            }

            logBuilder.log(RESPONSE_LOG_MESSAGE);
            return Mono.just(response);
        }

        private void logHeaders(ClientLogger logger, HttpResponse response, LoggingEventBuilder logBuilder) {
            if (httpLogDetailLevel.shouldLogHeaders() && logger.canLogAtLevel(LogLevel.INFORMATIONAL)) {
                addHeadersToLogMessage(allowedHeaderNames, response.getHeaders(), logBuilder,
                    disableRedactedHeaderLogging);
            }
        }

        private void logUrl(HttpResponseLoggingContext loggingOptions, HttpResponse response,
            LoggingEventBuilder logBuilder) {
            if (httpLogDetailLevel.shouldLogUrl()) {
                logBuilder.addKeyValue(LoggingKeys.STATUS_CODE_KEY, response.getStatusCode())
                    .addKeyValue(LoggingKeys.URL_KEY,
                        getRedactedUrl(response.getRequest().getUrl(), allowedQueryParameterNames))
                    .addKeyValue(LoggingKeys.DURATION_MS_KEY, loggingOptions.getResponseDuration().toMillis());
            }
        }

        private void logContentLength(HttpResponse response, LoggingEventBuilder logBuilder) {
            String contentLengthString = response.getHeaderValue(HttpHeaderName.CONTENT_LENGTH);
            if (!CoreUtils.isNullOrEmpty(contentLengthString)) {
                logBuilder.addKeyValue(LoggingKeys.CONTENT_LENGTH_KEY, contentLengthString);
            }
        }

        @Override
        public HttpResponse logResponseSync(ClientLogger logger, HttpResponseLoggingContext loggingOptions) {
            final LogLevel logLevel = getLogLevel(loggingOptions);
            final HttpResponse response = loggingOptions.getHttpResponse();

            if (!logger.canLogAtLevel(logLevel)) {
                return response;
            }
            LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

            logContentLength(response, logBuilder);

            logUrl(loggingOptions, response, logBuilder);

            logHeaders(logger, response, logBuilder);

            if (httpLogDetailLevel.shouldLogBody()) {
                String contentTypeHeader = response.getHeaderValue(HttpHeaderName.CONTENT_TYPE);
                long contentLength = getContentLength(logger, response.getHeaders());
                if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
                    return new LoggingHttpResponse(response, logBuilder, logger, (int) contentLength, contentTypeHeader,
                        prettyPrintBody);
                }
            }

            logBuilder.log(RESPONSE_LOG_MESSAGE);
            return response;
        }
    }

    /*
     * Generates the redacted URL for logging.
     *
     * @param url URL where the request is being sent.
     * 
     * @return A URL with query parameters redacted based on configurations in this policy.
     */
    private static String getRedactedUrl(URL url, Set<String> allowedQueryParameterNames) {
        String query = url.getQuery();
        if (CoreUtils.isNullOrEmpty(query)) {
            // URL doesn't have a query string, just return the URL as-is.
            return url.toString();
        }

        // URL does have a query string that may need redactions.
        // Use UrlBuilder to break apart the URL, clear the query string, and add the redacted query string.
        UrlBuilder urlBuilder = ImplUtils.parseUrl(url, false);

        CoreUtils.parseQueryParameters(query).forEachRemaining(queryParam -> {
            if (allowedQueryParameterNames.contains(queryParam.getKey().toLowerCase(Locale.ROOT))) {
                urlBuilder.addQueryParameter(queryParam.getKey(), queryParam.getValue());
            } else {
                urlBuilder.addQueryParameter(queryParam.getKey(), REDACTED_PLACEHOLDER);
            }
        });

        return urlBuilder.toString();
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
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param logger Logger used to log a warning if the Content-Length header is an invalid number.
     * 
     * @param headers HTTP headers that are checked for containing Content-Length.
     * 
     * @return
     */
    private static long getContentLength(ClientLogger logger, HttpHeaders headers) {
        long contentLength = 0;

        String contentLengthString = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException e) {
            logger.log(LogLevel.INFORMATIONAL,
                () -> "Could not parse the HTTP header content-length: '" + contentLengthString + "'.", e);
        }

        return contentLength;
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
    private static boolean shouldBodyBeLogged(String contentTypeHeader, long contentLength) {
        return !ContentType.APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
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
    private static ClientLogger getOrCreateMethodLogger(String methodName) {
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

    private static final class LoggingHttpResponse extends HttpResponse {
        private final HttpResponse actualResponse;
        private final LoggingEventBuilder logBuilder;
        private final int contentLength;
        private final ClientLogger logger;
        private final boolean prettyPrintBody;
        private final String contentTypeHeader;

        private LoggingHttpResponse(HttpResponse actualResponse, LoggingEventBuilder logBuilder, ClientLogger logger,
            int contentLength, String contentTypeHeader, boolean prettyPrintBody) {
            super(actualResponse.getRequest());
            this.actualResponse = actualResponse;
            this.logBuilder = logBuilder;
            this.logger = logger;
            this.contentLength = contentLength;
            this.contentTypeHeader = contentTypeHeader;
            this.prettyPrintBody = prettyPrintBody;
        }

        @Override
        public int getStatusCode() {
            return actualResponse.getStatusCode();
        }

        @Override
        @Deprecated
        public String getHeaderValue(String name) {
            return actualResponse.getHeaderValue(name);
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return actualResponse.getHeaderValue(headerName);
        }

        @Override
        public HttpHeaders getHeaders() {
            return actualResponse.getHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            AccessibleByteArrayOutputStream stream = new AccessibleByteArrayOutputStream(contentLength);

            return Flux.using(() -> stream, s -> actualResponse.getBody().doOnNext(byteBuffer -> {
                try {
                    ImplUtils.writeByteBufferToStream(byteBuffer.duplicate(), s);
                } catch (IOException ex) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
                }
            }), s -> doLog(s.toString(StandardCharsets.UTF_8)));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesFromNetworkResponse(getBody(), actualResponse.getHeaders());
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsByteArray().map(String::new);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }

        @Override
        public BinaryData getBodyAsBinaryData() {
            BinaryData content = actualResponse.getBodyAsBinaryData();
            doLog(content.toString());
            return content;
        }

        @Override
        public void close() {
            actualResponse.close();
        }

        private void doLog(String body) {
            logBuilder
                .addKeyValue(LoggingKeys.BODY_KEY,
                    prettyPrintIfNeeded(logger, prettyPrintBody, contentTypeHeader, body))
                .log(RESPONSE_LOG_MESSAGE);
        }
    }
}
