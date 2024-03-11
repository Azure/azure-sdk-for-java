// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.models.Header;
import com.generic.core.http.models.HeaderName;
import com.generic.core.http.models.Headers;
import com.generic.core.http.models.HttpLogOptions;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.Response;
import com.generic.core.http.pipeline.HttpPipelineNextPolicy;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.implementation.http.policy.HttpRequestLogger;
import com.generic.core.implementation.http.policy.HttpResponseLogger;
import com.generic.core.implementation.util.CoreUtils;
import com.generic.core.implementation.util.LoggingKeys;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.binarydata.BinaryData;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.generic.core.http.models.HeaderName.CLIENT_REQUEST_ID;
import static com.generic.core.http.models.HeaderName.TRACEPARENT;

/**
 * The pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final ClientLogger LOGGER = new ClientLogger(HttpLoggingPolicy.class);
    private final HttpLogOptions.HttpLogDetailLevel httpLogDetailLevel;
    private final List<HeaderName> allowedHeaderNames;
    private final Set<String> allowedQueryParameterNames;
    private final HttpRequestLogger requestLogger;
    private final HttpResponseLogger responseLogger;

    private static final String REQUEST_LOG_MESSAGE = "HTTP request";
    private static final String RESPONSE_LOG_MESSAGE = "HTTP response";

    /**
     * Creates an HttpLoggingPolicy with the given log configurations.
     *
     * @param httpLogOptions The HTTP logging configuration options.
     */
    public HttpLoggingPolicy(HttpLogOptions httpLogOptions) {
        if (httpLogOptions == null) {
            this.httpLogDetailLevel = HttpLogOptions.HttpLogDetailLevel.NONE;
            this.allowedHeaderNames = Collections.emptyList();
            this.allowedQueryParameterNames = Collections.emptySet();
            this.requestLogger = new DefaultHttpRequestLogger();
            this.responseLogger = new DefaultHttpResponseLogger();
        } else {
            this.httpLogDetailLevel = httpLogOptions.getLogLevel();
            this.allowedHeaderNames = httpLogOptions.getAllowedHeaderNames();
            this.allowedQueryParameterNames = httpLogOptions.getAllowedQueryParamNames()
                .stream()
                .map(queryParamName -> queryParamName.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
            this.requestLogger = (httpLogOptions.getRequestLogger() == null)
                ? new DefaultHttpRequestLogger()
                : httpLogOptions.getRequestLogger();
            this.responseLogger = (httpLogOptions.getResponseLogger() == null)
                ? new DefaultHttpResponseLogger()
                : httpLogOptions.getResponseLogger();
        }
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        // No logging will be performed, trigger a no-op.
        if (httpLogDetailLevel == HttpLogOptions.HttpLogDetailLevel.NONE) {
            return next.process();
        }

        final ClientLogger logger = httpRequest.getMetadata().getRequestLogger();
        final long startNs = System.nanoTime();

        requestLogger.logRequest(logger, httpRequest);
        try {
            Response<?> response = next.process();

            if (response != null) {
                response = responseLogger.logResponse(logger, response, Duration.ofNanos(System.nanoTime() - startNs));
            }
            return response;
        } catch (RuntimeException e) {
            createBasicLoggingContext(logger, ClientLogger.LogLevel.WARNING, httpRequest)
                .log(() -> "HTTP FAILED", e);
            throw e;
        }
    }

    private ClientLogger.LoggingEventBuilder createBasicLoggingContext(ClientLogger logger, ClientLogger.LogLevel level, HttpRequest request) {
        ClientLogger.LoggingEventBuilder log = logger.atLevel(level);
        if (LOGGER.canLogAtLevel(level) && request != null) {
            if (allowedHeaderNames.contains(CLIENT_REQUEST_ID)) {
                String clientRequestId = request.getHeaders().getValue(CLIENT_REQUEST_ID);
                if (clientRequestId != null) {
                    log.addKeyValue(CLIENT_REQUEST_ID.getCaseInsensitiveName(), clientRequestId);
                }
            }

            if (allowedHeaderNames.contains(TRACEPARENT)) {
                String traceparent = request.getHeaders().getValue(TRACEPARENT);
                if (traceparent != null) {
                    log.addKeyValue(TRACEPARENT.getCaseInsensitiveName(), traceparent);
                }
            }
        }

        return log;
    }

    private final class DefaultHttpRequestLogger implements HttpRequestLogger {
        @Override
        public void logRequest(ClientLogger logger, HttpRequest request) {
            final ClientLogger.LogLevel logLevel = getLogLevel(request);

            if (logger.canLogAtLevel(logLevel)) {
                log(logLevel, logger, request);
            }
        }

        private void log(ClientLogger.LogLevel logLevel, ClientLogger logger, HttpRequest request) {
            ClientLogger.LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

            if (httpLogDetailLevel.shouldLogUrl()) {
                logBuilder
                    .addKeyValue(LoggingKeys.HTTP_METHOD_KEY, request.getHttpMethod())
                    .addKeyValue(LoggingKeys.URL_KEY, getRedactedUrl(request.getUrl(), allowedQueryParameterNames));

                logBuilder.addKeyValue(LoggingKeys.TRY_COUNT_KEY, getRequestRetryCount(request));
            }

            if (httpLogDetailLevel.shouldLogHeaders() && logger.canLogAtLevel(ClientLogger.LogLevel.INFORMATIONAL)) {
                addHeadersToLogMessage(allowedHeaderNames, request.getHeaders(), logBuilder);
            }

            if (request.getBody() == null) {
                logBuilder.addKeyValue(LoggingKeys.CONTENT_LENGTH_KEY, 0)
                    .log(() -> REQUEST_LOG_MESSAGE);
                return;
            }

            String contentType = request.getHeaders().getValue(HeaderName.CONTENT_TYPE);
            long contentLength = getContentLength(logger, request.getHeaders());

            logBuilder.addKeyValue(LoggingKeys.CONTENT_LENGTH_KEY, contentLength);

            if (httpLogDetailLevel.shouldLogBody() && shouldBodyBeLogged(contentType, contentLength)) {
                logBody(request, logBuilder);
                return;
            }

            logBuilder.log(() -> REQUEST_LOG_MESSAGE);
        }
    }

    private void logBody(HttpRequest request, ClientLogger.LoggingEventBuilder logBuilder) {
        logBuilder.addKeyValue(LoggingKeys.BODY_KEY, request.getBody().toString())
                .log(() -> REQUEST_LOG_MESSAGE);
    }


    private final class DefaultHttpResponseLogger implements HttpResponseLogger {
        private void logHeaders(ClientLogger logger, Response<?> response,
                                ClientLogger.LoggingEventBuilder logBuilder) {
            if (httpLogDetailLevel.shouldLogHeaders() && logger.canLogAtLevel(ClientLogger.LogLevel.INFORMATIONAL)) {
                addHeadersToLogMessage(allowedHeaderNames, response.getHeaders(), logBuilder);
            }
        }

        private void logUrl(Response<?> response, Duration duration, ClientLogger.LoggingEventBuilder logBuilder) {
            if (httpLogDetailLevel.shouldLogUrl()) {
                logBuilder
                    .addKeyValue(LoggingKeys.STATUS_CODE_KEY, response.getStatusCode())
                    .addKeyValue(LoggingKeys.URL_KEY, getRedactedUrl(response.getRequest().getUrl(),
                        allowedQueryParameterNames))
                    .addKeyValue(LoggingKeys.DURATION_MS_KEY, duration.toMillis());
            }
        }

        private void logContentLength(Response<?> response, ClientLogger.LoggingEventBuilder logBuilder) {
            String contentLengthString = response.getHeaders().getValue(HeaderName.CONTENT_LENGTH);

            if (!CoreUtils.isNullOrEmpty(contentLengthString)) {
                logBuilder.addKeyValue(LoggingKeys.CONTENT_LENGTH_KEY, contentLengthString);
            }
        }

        @Override
        public Response<?> logResponse(ClientLogger logger, Response<?> response, Duration duration) {
            final ClientLogger.LogLevel logLevel = getLogLevel(response);

            if (!logger.canLogAtLevel(logLevel)) {
                return response;
            }

            ClientLogger.LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

            logContentLength(response, logBuilder);
            logUrl(response, duration, logBuilder);
            logHeaders(logger, response, logBuilder);

            if (httpLogDetailLevel.shouldLogBody()) {
                String contentTypeHeader = response.getHeaders().getValue(HeaderName.CONTENT_TYPE);
                long contentLength = getContentLength(logger, response.getHeaders());

                if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
                    return new LoggingHttpResponse<>(response, logBuilder);
                }
            }

            logBuilder.log(() -> RESPONSE_LOG_MESSAGE);

            return response;
        }
    }

    /*
     * Generates the redacted URL for logging.
     *
     * @param url URL where the request is being sent.
     * @return A URL with query parameters redacted based on configurations in this policy.
     */
    private static String getRedactedUrl(URL url, Set<String> allowedQueryParameterNames) {
        String query = url.getQuery();
        StringBuilder urlBuilder = new StringBuilder();

        // Add the protocol, host and port to the urlBuilder
        urlBuilder.append(url.getProtocol())
            .append("://")
            .append(url.getHost());

        if (url.getPort() != -1) {
            urlBuilder.append(":")
                .append(url.getPort());
        }

        // Add the path to the urlBuilder
        urlBuilder.append(url.getPath());

        if (query != null && !query.isEmpty()) {
            urlBuilder.append("?");

            // Parse and redact the query parameters
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");

                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];

                    urlBuilder.append(key);
                    urlBuilder.append("=");

                    if (allowedQueryParameterNames.contains(key.toLowerCase(Locale.ROOT))) {
                        urlBuilder.append(value);
                    } else {
                        urlBuilder.append(REDACTED_PLACEHOLDER);
                    }

                    urlBuilder.append("&");
                }
            }

            // Remove the trailing '&'
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }

        return urlBuilder.toString();
    }

    /*
     * Adds HTTP headers into the StringBuilder that is generating the log message.
     *
     * @param headers HTTP headers on the request or response.
     * @param sb StringBuilder that is generating the log message.
     * @param logLevel Log level the environment is configured to use.
     */
    private static void addHeadersToLogMessage(List<HeaderName> allowedHeaderNames, Headers headers,
                                               ClientLogger.LoggingEventBuilder logBuilder) {
        for (Header header : headers) {
            String headerName = header.getName();
            String headerValue = allowedHeaderNames.contains(HeaderName.fromString(headerName))
                ? header.getValue() : REDACTED_PLACEHOLDER;
            logBuilder.addKeyValue(headerName, headerValue);
        }
    }

    /*
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param logger Logger used to log a warning if the Content-Length header is an invalid number.
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return
     */
    private static long getContentLength(ClientLogger logger, Headers headers) {
        long contentLength = 0;

        String contentLengthString = headers.getValue(HeaderName.CONTENT_LENGTH);

        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException | NullPointerException e) {
            logger.atVerbose().log(() -> "Could not parse the HTTP header content-length: '"
                + contentLengthString + "'.", e);
        }

        return contentLength;
    }

    /**
     * Determines if the request or response body should be logged.
     *
     * <p>The request or response body is logged if the Content-Type is not "application/octet-stream" and the body
     * isn't empty and is less than 16KB in size.</p>
     *
     * @param contentTypeHeader Content-Type header value.
     * @param contentLength Content-Length header represented as a numeric.
     *
     * @return A flag indicating if the request or response body should be logged.
     */
    private static boolean shouldBodyBeLogged(String contentTypeHeader, long contentLength) {
        return !APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
            && contentLength != 0
            && contentLength < MAX_BODY_LOG_SIZE;
    }

    /**
     * Gets the request retry count to include in logging.
     */
    private static int getRequestRetryCount(HttpRequest request) {
        return request.getMetadata().getRetryCount();
    }

    private static ClientLogger.LoggingEventBuilder getLogBuilder(ClientLogger.LogLevel logLevel, ClientLogger logger) {
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

    private static final class LoggingHttpResponse<T> extends HttpResponse<T> {
        private final ClientLogger.LoggingEventBuilder logBuilder;

        private LoggingHttpResponse(Response<T> actualResponse, ClientLogger.LoggingEventBuilder logBuilder) {
            super(actualResponse.getRequest(), actualResponse.getStatusCode(), actualResponse.getHeaders(),
                actualResponse.getValue());

            this.logBuilder = logBuilder;
        }

        @Override
        public BinaryData getBody() {
            BinaryData content = super.getBody();

            doLog(content.toString());

            return content;
        }

        @Override
        public void close() throws IOException {
            super.close();
        }

        private void doLog(String body) {
            logBuilder.addKeyValue("body", body)
                .log(() -> RESPONSE_LOG_MESSAGE);
        }
    }
}
