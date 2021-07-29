// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.http.HttpPipelineCallContextHelper;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final ObjectMapper PRETTY_PRINTER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";

    private final ClientLogger logger = new ClientLogger(HttpLoggingPolicy.class);

    private final HttpLogDetailLevel httpLogDetailLevel;
    private final Set<String> allowedHeaderNames;
    private final Set<String> allowedQueryParameterNames;
    private final boolean prettyPrintBody;

    private final HttpRequestLogger requestLogger;
    private final HttpResponseLogger responseLogger;

    /**
     * Key for {@link Context} to pass request retry count metadata for logging.
     */
    public static final String RETRY_COUNT_CONTEXT = "requestRetryCount";

    /**
     * Creates an HttpLoggingPolicy with the given log configurations.
     *
     * @param httpLogOptions The HTTP logging configuration options.
     */
    public HttpLoggingPolicy(HttpLogOptions httpLogOptions) {
        if (httpLogOptions == null) {
            this.httpLogDetailLevel = HttpLogDetailLevel.NONE;
            this.allowedHeaderNames = Collections.emptySet();
            this.allowedQueryParameterNames = Collections.emptySet();
            this.prettyPrintBody = false;

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

        final ClientLogger logger = new ClientLogger((String) context.getData("caller-method").orElse(""));
        final long startNs = System.nanoTime();

        return requestLogger.logRequest(logger, getRequestLoggingOptions(context))
            .then(next.process())
            .flatMap(response -> responseLogger.logResponse(logger,
                getResponseLoggingOptions(response, startNs, context)))
            .doOnError(throwable -> logger.warning("<-- HTTP FAILED: ", throwable));
    }

    private HttpRequestLoggingContext getRequestLoggingOptions(HttpPipelineCallContext callContext) {
        return new HttpRequestLoggingContext(callContext.getHttpRequest(),
            HttpPipelineCallContextHelper.getContext(callContext),
            getRequestRetryCount(HttpPipelineCallContextHelper.getContext(callContext), getHttpLoggingPolicyLogger()));
    }

    private HttpResponseLoggingContext getResponseLoggingOptions(HttpResponse httpResponse, long startNs,
        HttpPipelineCallContext callContext) {
        return new HttpResponseLoggingContext(httpResponse, Duration.ofNanos(System.nanoTime() - startNs),
            HttpPipelineCallContextHelper.getContext(callContext),
            getRequestRetryCount(HttpPipelineCallContextHelper.getContext(callContext), getHttpLoggingPolicyLogger()));
    }

    private final class DefaultHttpRequestLogger implements HttpRequestLogger {
        @Override
        public Mono<Void> logRequest(ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
            final LogLevel logLevel = getLogLevel(loggingOptions);

            if (!logger.canLogAtLevel(logLevel)) {
                return Mono.empty();
            }

            final HttpRequest request = loggingOptions.getHttpRequest();

            StringBuilder requestLogMessage = new StringBuilder();
            if (httpLogDetailLevel.shouldLogUrl()) {
                requestLogMessage.append("--> ")
                    .append(request.getHttpMethod())
                    .append(" ")
                    .append(getRedactedUrl(request.getUrl(), allowedQueryParameterNames))
                    .append(System.lineSeparator());

                Integer retryCount = loggingOptions.getTryCount();
                if (retryCount != null) {
                    requestLogMessage.append("Try count: ")
                        .append(retryCount)
                        .append(System.lineSeparator());
                }
            }

            if (httpLogDetailLevel.shouldLogHeaders() && logger.canLogAtLevel(LogLevel.VERBOSE)) {
                addHeadersToLogMessage(allowedHeaderNames, request.getHeaders(), requestLogMessage);
            }

            if (!httpLogDetailLevel.shouldLogBody()) {
                return logAndReturn(logger, logLevel, requestLogMessage, null);
            }

            if (request.getBody() == null) {
                requestLogMessage.append("(empty body)")
                    .append(System.lineSeparator())
                    .append("--> END ")
                    .append(request.getHttpMethod())
                    .append(System.lineSeparator());

                return logAndReturn(logger, logLevel, requestLogMessage, null);
            }

            String contentType = request.getHeaders().getValue("Content-Type");
            long contentLength = getContentLength(logger, request.getHeaders());

            if (shouldBodyBeLogged(contentType, contentLength)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) contentLength);
                WritableByteChannel bodyContentChannel = Channels.newChannel(outputStream);

                // Add non-mutating operators to the data stream.
                request.setBody(
                    request.getBody()
                        .flatMap(byteBuffer -> writeBufferToBodyStream(bodyContentChannel, byteBuffer))
                        .doFinally(ignored -> {
                            requestLogMessage.append(contentLength)
                                .append("-byte body:")
                                .append(System.lineSeparator())
                                .append(prettyPrintIfNeeded(logger, prettyPrintBody, contentType,
                                    convertStreamToString(outputStream, logger)))
                                .append(System.lineSeparator())
                                .append("--> END ")
                                .append(request.getHttpMethod())
                                .append(System.lineSeparator());

                            logAndReturn(logger, logLevel, requestLogMessage, null);
                        }));

                return Mono.empty();
            } else {
                requestLogMessage.append(contentLength)
                    .append("-byte body: (content not logged)")
                    .append(System.lineSeparator())
                    .append("--> END ")
                    .append(request.getHttpMethod())
                    .append(System.lineSeparator());

                return logAndReturn(logger, logLevel, requestLogMessage, null);
            }
        }
    }

    private final class DefaultHttpResponseLogger implements HttpResponseLogger {
        @Override
        public Mono<HttpResponse> logResponse(ClientLogger logger, HttpResponseLoggingContext loggingOptions) {
            final LogLevel logLevel = getLogLevel(loggingOptions);
            final HttpResponse response = loggingOptions.getHttpResponse();

            if (!logger.canLogAtLevel(logLevel)) {
                return Mono.just(response);
            }

            String contentLengthString = response.getHeaderValue("Content-Length");
            String bodySize = (CoreUtils.isNullOrEmpty(contentLengthString))
                ? "unknown-length body"
                : contentLengthString + "-byte body";

            StringBuilder responseLogMessage = new StringBuilder();
            if (httpLogDetailLevel.shouldLogUrl()) {
                responseLogMessage.append("<-- ")
                    .append(response.getStatusCode())
                    .append(" ")
                    .append(getRedactedUrl(response.getRequest().getUrl(), allowedQueryParameterNames))
                    .append(" (")
                    .append(loggingOptions.getResponseDuration().toMillis())
                    .append(" ms, ")
                    .append(bodySize)
                    .append(")")
                    .append(System.lineSeparator());
            }

            if (httpLogDetailLevel.shouldLogHeaders() && logger.canLogAtLevel(LogLevel.VERBOSE)) {
                addHeadersToLogMessage(allowedHeaderNames, response.getHeaders(), responseLogMessage);
            }

            if (!httpLogDetailLevel.shouldLogBody()) {
                responseLogMessage.append("<-- END HTTP");
                return logAndReturn(logger, logLevel, responseLogMessage, response);
            }

            String contentTypeHeader = response.getHeaderValue("Content-Type");
            long contentLength = getContentLength(logger, response.getHeaders());

            if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
                HttpResponse bufferedResponse = response.buffer();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) contentLength);
                WritableByteChannel bodyContentChannel = Channels.newChannel(outputStream);
                return bufferedResponse.getBody()
                    .flatMap(byteBuffer -> writeBufferToBodyStream(bodyContentChannel, byteBuffer))
                    .doFinally(ignored -> {
                        responseLogMessage.append("Response body:")
                            .append(System.lineSeparator())
                            .append(prettyPrintIfNeeded(logger, prettyPrintBody, contentTypeHeader,
                                convertStreamToString(outputStream, logger)))
                            .append(System.lineSeparator())
                            .append("<-- END HTTP");

                        logAndReturn(logger, logLevel, responseLogMessage, response);
                    }).then(Mono.just(bufferedResponse));
            } else {
                responseLogMessage.append("(body content not logged)")
                    .append(System.lineSeparator())
                    .append("<-- END HTTP");

                return logAndReturn(logger, logLevel, responseLogMessage, response);
            }
        }
    }

    private ClientLogger getHttpLoggingPolicyLogger() {
        return this.logger;
    }

    private static <T> Mono<T> logAndReturn(ClientLogger logger, LogLevel logLevel, StringBuilder logMessageBuilder,
        T data) {
        switch (logLevel) {
            case VERBOSE:
                logger.verbose(logMessageBuilder.toString());
                break;

            case INFORMATIONAL:
                logger.info(logMessageBuilder.toString());
                break;

            case WARNING:
                logger.warning(logMessageBuilder.toString());
                break;

            case ERROR:
                logger.error(logMessageBuilder.toString());
                break;

            default:
                break;
        }

        return Mono.justOrEmpty(data);
    }

    /*
     * Generates the redacted URL for logging.
     *
     * @param url URL where the request is being sent.
     * @return A URL with query parameters redacted based on configurations in this policy.
     */
    private static String getRedactedUrl(URL url, Set<String> allowedQueryParameterNames) {
        return UrlBuilder.parse(url)
            .setQuery(getAllowedQueryString(url.getQuery(), allowedQueryParameterNames))
            .toString();
    }

    /*
     * Generates the logging safe query parameters string.
     *
     * @param queryString Query parameter string from the request URL.
     * @return A query parameter string redacted based on the configurations in this policy.
     */
    private static String getAllowedQueryString(String queryString, Set<String> allowedQueryParameterNames) {
        if (CoreUtils.isNullOrEmpty(queryString)) {
            return "";
        }

        StringBuilder queryStringBuilder = new StringBuilder();
        String[] queryParams = queryString.split("&");
        for (String queryParam : queryParams) {
            if (queryStringBuilder.length() > 0) {
                queryStringBuilder.append("&");
            }

            String[] queryPair = queryParam.split("=", 2);
            if (queryPair.length == 2) {
                String queryName = queryPair[0];
                if (allowedQueryParameterNames.contains(queryName.toLowerCase(Locale.ROOT))) {
                    queryStringBuilder.append(queryParam);
                } else {
                    queryStringBuilder.append(queryPair[0]).append("=").append(REDACTED_PLACEHOLDER);
                }
            } else {
                queryStringBuilder.append(queryParam);
            }
        }

        return queryStringBuilder.toString();
    }

    /*
     * Adds HTTP headers into the StringBuilder that is generating the log message.
     *
     * @param headers HTTP headers on the request or response.
     * @param sb StringBuilder that is generating the log message.
     * @param logLevel Log level the environment is configured to use.
     */
    private static void addHeadersToLogMessage(Set<String> allowedHeaderNames, HttpHeaders headers, StringBuilder sb) {
        for (HttpHeader header : headers) {
            String headerName = header.getName();
            sb.append(headerName).append(":");
            if (allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))) {
                sb.append(header.getValue());
            } else {
                sb.append(REDACTED_PLACEHOLDER);
            }
            sb.append(System.lineSeparator());
        }
    }

    /*
     * Determines and attempts to pretty print the body if it is JSON.
     *
     * <p>The body is pretty printed if the Content-Type is JSON and the policy is configured to pretty print JSON.</p>
     *
     * @param logger Logger used to log a warning if the body fails to pretty print as JSON.
     * @param contentType Content-Type header.
     * @param body Body of the request or response.
     * @return The body pretty printed if it is JSON, otherwise the unmodified body.
     */
    private static String prettyPrintIfNeeded(ClientLogger logger, boolean prettyPrintBody, String contentType,
        String body) {
        String result = body;
        if (prettyPrintBody && contentType != null
            && (contentType.startsWith(ContentType.APPLICATION_JSON) || contentType.startsWith("text/json"))) {
            try {
                final Object deserialized = PRETTY_PRINTER.readTree(body);
                result = PRETTY_PRINTER.writeValueAsString(deserialized);
            } catch (Exception e) {
                logger.warning("Failed to pretty print JSON: {}", e.getMessage());
            }
        }
        return result;
    }

    /*
     * Attempts to retrieve and parse the Content-Length header into a numeric representation.
     *
     * @param logger Logger used to log a warning if the Content-Length header is an invalid number.
     * @param headers HTTP headers that are checked for containing Content-Length.
     * @return
     */
    private static long getContentLength(ClientLogger logger, HttpHeaders headers) {
        long contentLength = 0;

        String contentLengthString = headers.getValue("Content-Length");
        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException | NullPointerException e) {
            logger.warning("Could not parse the HTTP header content-length: '{}'.",
                headers.getValue("content-length"), e);
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
     * @param contentLength Content-Length header represented as a numeric.
     * @return A flag indicating if the request or response body should be logged.
     */
    private static boolean shouldBodyBeLogged(String contentTypeHeader, long contentLength) {
        return !ContentType.APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
            && contentLength != 0
            && contentLength < MAX_BODY_LOG_SIZE;
    }

    /*
     * Helper function which converts a ByteArrayOutputStream to a String without duplicating the internal buffer.
     */
    private static String convertStreamToString(ByteArrayOutputStream stream, ClientLogger logger) {
        try {
            return stream.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw logger.logExceptionAsError(new RuntimeException(ex));
        }
    }

    /*
     * Helper function which writes body ByteBuffers into the body message channel.
     */
    private static Mono<ByteBuffer> writeBufferToBodyStream(WritableByteChannel channel, ByteBuffer byteBuffer) {
        try {
            channel.write(byteBuffer.duplicate());
            return Mono.just(byteBuffer);
        } catch (IOException ex) {
            return Mono.error(ex);
        }
    }

    /*
     * Gets the request retry count to include in logging.
     *
     * If there is no value set or it isn't a valid number null will be returned indicating that retry count won't be
     * logged.
     */
    private static Integer getRequestRetryCount(Context context, ClientLogger logger) {
        Object rawRetryCount = context.getData(RETRY_COUNT_CONTEXT).orElse(null);
        if (rawRetryCount == null) {
            return null;
        }

        try {
            return Integer.valueOf(rawRetryCount.toString());
        } catch (NumberFormatException ex) {
            logger.warning("Could not parse the request retry count: '{}'.", rawRetryCount);
            return null;
        }
    }
}
