// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.implementation.util.LoggingKeys;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.clientcore.core.http.models.HttpHeaderName.TRACEPARENT;
import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;

/**
 * The pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final HttpLogOptions DEFAULT_HTTP_LOG_OPTIONS = new HttpLogOptions();
    private static final Set<HttpHeaderName> ALWAYS_ALLOWED_HEADERS = Set.of(TRACEPARENT);
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";
    private static final ClientLogger LOGGER = new ClientLogger(HttpLoggingPolicy.class);
    private final HttpLogOptions.HttpLogDetailLevel httpLogDetailLevel;
    private final Set<HttpHeaderName> allowedHeaderNames;

    private final Set<String> allowedQueryParameterNames;

    private static final String HTTP_REQUEST_EVENT_NAME = "http.request";
    private static final String HTTP_RESPONSE_EVENT_NAME = "http.response";

    // request log level is low (verbose) since almost all request details are also
    // captured on the response log.
    private static final ClientLogger.LogLevel HTTP_REQUEST_LOG_LEVEL = ClientLogger.LogLevel.VERBOSE;
    private static final ClientLogger.LogLevel HTTP_RESPONSE_LOG_LEVEL = ClientLogger.LogLevel.INFORMATIONAL;

    /**
     * Creates an HttpLoggingPolicy with the given log configurations.
     *
     * @param httpLogOptions The HTTP logging configuration options.
     */
    public HttpLoggingPolicy(HttpLogOptions httpLogOptions) {
        HttpLogOptions logOptionsToUse = httpLogOptions == null ? DEFAULT_HTTP_LOG_OPTIONS : httpLogOptions;
        this.httpLogDetailLevel = logOptionsToUse.getLogLevel();
        this.allowedHeaderNames = logOptionsToUse.getAllowedHeaderNames();
        this.allowedQueryParameterNames = logOptionsToUse.getAllowedQueryParamNames()
            .stream()
            .map(queryParamName -> queryParamName.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        // No logging will be performed, trigger a no-op.
        if (httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
            return next.process();
        }

        ClientLogger logger = getLogger(httpRequest);

        final long startNs = System.nanoTime();
        final String redactedUrl = getRedactedUri(httpRequest.getUri(), allowedQueryParameterNames);
        final int tryCount = HttpRequestAccessHelper.getTryCount(httpRequest);
        final long requestContentLength = httpRequest.getBody() == null
            ? 0
            : getContentLength(logger, httpRequest.getBody(), httpRequest.getHeaders());

        logRequest(logger, httpRequest, startNs, requestContentLength, redactedUrl, tryCount);

        try {
            Response<?> response = next.process();

            if (response == null) {
                LOGGER.atError()
                    .addKeyValue(LoggingKeys.HTTP_METHOD_KEY, httpRequest.getHttpMethod())
                    .addKeyValue(LoggingKeys.URI_KEY, redactedUrl)
                    .log(
                        "HTTP response is null and no exception is thrown. Please report it to the client library maintainers.");

                return null;
            }

            return logResponse(logger, response, startNs, requestContentLength, redactedUrl, tryCount);
        } catch (RuntimeException e) {
            throw logException(logger, httpRequest, null, e, startNs, null, requestContentLength, redactedUrl,
                tryCount);
        }
    }

    private ClientLogger getLogger(HttpRequest request) {
        if (request.getRequestOptions() != null) {
            return request.getRequestOptions().getLogger();
        }

        return LOGGER;
    }

    private void logRequest(ClientLogger logger, HttpRequest request, long startNanoTime, long requestContentLength,
        String redactedUrl, int tryCount) {
        ClientLogger.LoggingEventBuilder logBuilder = logger.atLevel(HTTP_REQUEST_LOG_LEVEL);
        if (!logBuilder.isEnabled() || httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
            return;
        }

        logBuilder.setEventName(HTTP_REQUEST_EVENT_NAME)
            .addKeyValue(LoggingKeys.HTTP_METHOD_KEY, request.getHttpMethod())
            .addKeyValue(LoggingKeys.URI_KEY, redactedUrl)
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .addKeyValue(LoggingKeys.REQUEST_CONTENT_LENGTH_KEY, requestContentLength);

        addHeadersToLogMessage(request.getHeaders(), logBuilder);

        if (httpLogDetailLevel.shouldLogBody() && canLogBody(request.getBody())) {
            try {
                BinaryData bufferedBody = request.getBody().toReplayableBinaryData();
                request.setBody(bufferedBody);
                logBuilder.addKeyValue(LoggingKeys.BODY_KEY, bufferedBody.toString());
            } catch (RuntimeException e) {
                // we'll log exception at the appropriate level.
                throw logException(logger, request, null, e, startNanoTime, null, requestContentLength, redactedUrl,
                    tryCount);
            }
        }

        logBuilder.log();
    }

    private Response<?> logResponse(ClientLogger logger, Response<?> response, long startNanoTime,
        long requestContentLength, String redactedUrl, int tryCount) {
        ClientLogger.LoggingEventBuilder logBuilder = logger.atLevel(HTTP_RESPONSE_LOG_LEVEL);
        if (httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
            return response;
        }

        long responseStartNanoTime = System.nanoTime();

        // response may be disabled, but we still need to log the exception if an exception occurs during stream reading.
        if (logBuilder.isEnabled()) {
            logBuilder.setEventName(HTTP_RESPONSE_EVENT_NAME)
                .addKeyValue(LoggingKeys.HTTP_METHOD_KEY, response.getRequest().getHttpMethod())
                .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
                .addKeyValue(LoggingKeys.URI_KEY, redactedUrl)
                .addKeyValue(LoggingKeys.TIME_TO_RESPONSE_MS_KEY, getDurationMs(startNanoTime, responseStartNanoTime))
                .addKeyValue(LoggingKeys.STATUS_CODE_KEY, response.getStatusCode())
                .addKeyValue(LoggingKeys.REQUEST_CONTENT_LENGTH_KEY, requestContentLength)
                .addKeyValue(LoggingKeys.RESPONSE_CONTENT_LENGTH_KEY,
                    getContentLength(logger, response.getBody(), response.getHeaders()));

            addHeadersToLogMessage(response.getHeaders(), logBuilder);
        }

        if (httpLogDetailLevel.shouldLogBody() && canLogBody(response.getBody())) {
            return new LoggingHttpResponse<>(response, content -> {
                if (logBuilder.isEnabled()) {
                    logBuilder.addKeyValue(LoggingKeys.BODY_KEY, content.toString())
                        .addKeyValue(LoggingKeys.DURATION_MS_KEY, getDurationMs(startNanoTime, System.nanoTime()))
                        .log();
                }
            }, throwable -> logException(logger, response.getRequest(), response, throwable, startNanoTime,
                responseStartNanoTime, requestContentLength, redactedUrl, tryCount));
        }

        if (logBuilder.isEnabled()) {
            logBuilder.addKeyValue(LoggingKeys.DURATION_MS_KEY, getDurationMs(startNanoTime, System.nanoTime())).log();
        }

        return response;
    }

    private <T extends Throwable> T logException(ClientLogger logger, HttpRequest request, Response<?> response,
        T throwable, long startNanoTime, Long responseStartNanoTime, long requestContentLength, String redactedUrl,
        int tryCount) {
        ClientLogger.LoggingEventBuilder logBuilder = logger.atLevel(ClientLogger.LogLevel.WARNING);
        if (!logBuilder.isEnabled() || httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
            return throwable;
        }

        logBuilder.setEventName(HTTP_RESPONSE_EVENT_NAME)
            .addKeyValue(LoggingKeys.HTTP_METHOD_KEY, request.getHttpMethod())
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .addKeyValue(LoggingKeys.URI_KEY, redactedUrl)
            .addKeyValue(LoggingKeys.REQUEST_CONTENT_LENGTH_KEY, requestContentLength)
            .addKeyValue(LoggingKeys.DURATION_MS_KEY, getDurationMs(startNanoTime, System.nanoTime()));

        if (response != null) {
            addHeadersToLogMessage(response.getHeaders(), logBuilder);
            logBuilder
                .addKeyValue(LoggingKeys.RESPONSE_CONTENT_LENGTH_KEY,
                    getContentLength(logger, response.getBody(), response.getHeaders()))
                .addKeyValue(LoggingKeys.STATUS_CODE_KEY, response.getStatusCode());

            if (responseStartNanoTime != null) {
                logBuilder.addKeyValue(LoggingKeys.TIME_TO_RESPONSE_MS_KEY,
                    getDurationMs(startNanoTime, responseStartNanoTime));
            }
        }

        return logBuilder.log(null, throwable);
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
        // TODO: limolkova - we might want to filter out binary data, but
        // if somebody enabled logging it - why not log it?
        return data != null && data.getLength() != null && data.getLength() > 0 && data.getLength() < MAX_BODY_LOG_SIZE;
    }

    /**
     * Generates the redacted URI for logging.
     *
     * @param uri URI where the request is being sent.
     * @param allowedQueryParameterNames Query parameters that are allowed to be logged.
     * @return A URI with query parameters redacted based on configurations in this policy.
     */
    private static String getRedactedUri(URI uri, Set<String> allowedQueryParameterNames) {
        String query = uri.getQuery();
        StringBuilder uriBuilder = new StringBuilder();

        // Add the protocol, host and port to the uriBuilder
        uriBuilder.append(uri.getScheme()).append("://").append(uri.getHost());

        if (uri.getPort() != -1) {
            uriBuilder.append(":").append(uri.getPort());
        }

        // Add the path to the uriBuilder
        uriBuilder.append(uri.getPath());

        if (query != null && !query.isEmpty()) {
            uriBuilder.append("?");

            // Parse and redact the query parameters
            boolean firstQueryParam = true;
            for (Map.Entry<String, String> kvp : new ImplUtils.QueryParameterIterable(query)) {
                if (!firstQueryParam) {
                    uriBuilder.append('&');
                }

                uriBuilder.append(kvp.getKey());
                uriBuilder.append('=');

                if (allowedQueryParameterNames.contains(kvp.getKey().toLowerCase(Locale.ROOT))) {
                    uriBuilder.append(kvp.getValue());
                } else {
                    uriBuilder.append(REDACTED_PLACEHOLDER);
                }

                firstQueryParam = false;
            }
        }

        return uriBuilder.toString();
    }

    /**
     * Adds HTTP headers into the StringBuilder that is generating the log message.
     *
     * @param headers HTTP headers on the request or response.
     * @param logBuilder Log message builder.
     */
    private void addHeadersToLogMessage(HttpHeaders headers, ClientLogger.LoggingEventBuilder logBuilder) {
        if (httpLogDetailLevel.shouldLogHeaders()) {
            for (HttpHeader header : headers) {
                HttpHeaderName headerName = header.getName();
                String headerValue = allowedHeaderNames.contains(headerName) ? header.getValue() : REDACTED_PLACEHOLDER;
                logBuilder.addKeyValue(headerName.toString(), headerValue);
            }
        } else {
            for (HttpHeaderName headerName : ALWAYS_ALLOWED_HEADERS) {
                String headerValue = headers.getValue(headerName);
                if (headerValue != null) {
                    logBuilder.addKeyValue(headerName.toString(), headerValue);
                }
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
    private static long getContentLength(ClientLogger logger, BinaryData body, HttpHeaders headers) {
        if (body != null && body.getLength() != null) {
            return body.getLength();
        }

        long contentLength = 0;

        String contentLengthString = headers.getValue(HttpHeaderName.CONTENT_LENGTH);

        if (isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException | NullPointerException e) {
            logger.atVerbose()
                .addKeyValue("contentLength", contentLengthString)
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
