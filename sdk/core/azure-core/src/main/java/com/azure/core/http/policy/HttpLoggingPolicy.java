// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.LogLevel;
import com.azure.core.implementation.LoggingUtil;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final ObjectMapper PRETTY_PRINTER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private final HttpLogDetailLevel httpLogDetailLevel;
    private final Set<String> allowedHeaderNames;
    private final Set<String> allowedQueryParameterNames;
    private final boolean prettyPrintJSON;

    /**
     * Creates an HttpLoggingPolicy with the given log configurations.
     *
     * @param httpLogOptions The HTTP logging configurations.
     */
    public HttpLoggingPolicy(HttpLogOptions httpLogOptions) {
        this(httpLogOptions, false);
    }

    /**
     * Creates an HttpLoggingPolicy with the given log configuration and pretty printing setting.
     *
     * @param httpLogOptions The HTTP logging configuration options.
     * @param prettyPrintJSON If true, pretty prints JSON message bodies when logging. If the detailLevel does not
     * include body logging, this flag does nothing.
     */
    private HttpLoggingPolicy(HttpLogOptions httpLogOptions, boolean prettyPrintJSON) {
        this.prettyPrintJSON = prettyPrintJSON;

        if (httpLogOptions == null) {
            this.httpLogDetailLevel = HttpLogDetailLevel.NONE;
            this.allowedHeaderNames = Collections.emptySet();
            this.allowedQueryParameterNames = Collections.emptySet();
        } else {
            this.httpLogDetailLevel = httpLogOptions.getLogLevel();
            this.allowedHeaderNames = httpLogOptions.getAllowedHeaderNames()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            this.allowedQueryParameterNames = httpLogOptions.getAllowedQueryParamNames()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
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

        return logRequest(logger, context.getHttpRequest())
            .then(next.process())
            .flatMap(response -> logResponse(logger, response, startNs))
            .doOnError(throwable -> logger.warning("<-- HTTP FAILED: ", throwable));
    }

    private Mono<String> logRequest(final ClientLogger logger, final HttpRequest request) {
        /*
         * Logging is either disabled or the logging level is above information (warning or error), this will result
         * in nothing being logged so perform a no-op.
         */
        int numericLogLevel = LoggingUtil.getEnvironmentLoggingLevel().toNumeric();
        if (numericLogLevel == LogLevel.DISABLED.toNumeric()
            || numericLogLevel > LogLevel.INFORMATIONAL.toNumeric()) {
            return Mono.empty();
        }

        StringBuilder requestLogMessage = new StringBuilder();
        if (httpLogDetailLevel.shouldLogUrl()) {
            requestLogMessage.append(String.format("--> %s %s%n", request.getHttpMethod(),
                getRedactedUrl(request.getUrl())));
        }

        addHeadersToLogMessage(request.getHeaders(), requestLogMessage, numericLogLevel);

        Mono<String> requestLoggingMono = Mono.defer(() -> Mono.just(requestLogMessage.toString()));

        if (httpLogDetailLevel.shouldLogBody()) {
            if (request.getBody() == null) {
                requestLogMessage.append(String.format("(empty body)%n--> END %s%n", request.getHttpMethod()));
            } else {
                String contentType = request.getHeaders().getValue("Content-Type");
                long contentLength = getContentLength(logger, request.getHeaders());

                if (bodyIsPrintable(contentType, contentLength)) {
                    requestLoggingMono = FluxUtil.collectBytesInByteBufferStream(request.getBody()).flatMap(bytes ->
                        Mono.just(requestLogMessage.append(String.format("%d-byte body:%n%s%n--> END %s%n",
                            contentLength, new String(bytes, StandardCharsets.UTF_8), request.getHttpMethod()))
                            .toString()));
                } else {
                    requestLogMessage.append(String.format("%d-byte body: (content not logged)%n--> END %s%n",
                        contentLength, request.getHttpMethod()));
                }
            }
        }

        return requestLoggingMono.doOnNext(logger::info);
    }

    private Mono<HttpResponse> logResponse(final ClientLogger logger, final HttpResponse response, long startNs) {
        /*
         * Logging is either disabled or the logging level is above information (warning or error), this will result
         * in nothing being logged so perform a no-op.
         */
        int numericLogLevel = LoggingUtil.getEnvironmentLoggingLevel().toNumeric();
        if (numericLogLevel == LogLevel.DISABLED.toNumeric()
            || numericLogLevel > LogLevel.INFORMATIONAL.toNumeric()) {
            return Mono.just(response);
        }

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        String contentLengthString = response.getHeaderValue("Content-Length");
        String bodySize = (CoreUtils.isNullOrEmpty(contentLengthString))
            ? "unknown-length"
            : contentLengthString + "-byte";

        StringBuilder responseLogMessage = new StringBuilder();
        if (httpLogDetailLevel.shouldLogUrl()) {
            responseLogMessage.append(String.format("<-- %d %s (%d ms, %s body)%n", response.getStatusCode(),
                getRedactedUrl(response.getRequest().getUrl()), tookMs, bodySize));
        }

        addHeadersToLogMessage(response.getHeaders(), responseLogMessage, numericLogLevel);

        Mono<String> responseLoggingMono = Mono.defer(() -> Mono.just(responseLogMessage.toString()));

        if (httpLogDetailLevel.shouldLogBody()) {
            final String contentTypeHeader = response.getHeaderValue("Content-Type");

            if (bodyIsPrintable(contentTypeHeader, getContentLength(logger, response.getHeaders()))) {
                final HttpResponse bufferedResponse = response.buffer();
                responseLoggingMono = bufferedResponse.getBodyAsString().flatMap(body ->
                    Mono.just(responseLogMessage.append(String.format("Response body:%n%s%n<-- END HTTP",
                        prettyPrintIfNeeded(logger, contentTypeHeader, body)))
                        .toString()))
                    .switchIfEmpty(responseLoggingMono);
            } else {
                responseLogMessage.append("(body content not logged)")
                    .append(System.lineSeparator())
                    .append("<-- END HTTP");
            }
        } else {
            responseLogMessage.append("<-- END HTTP");
        }

        return responseLoggingMono.doOnNext(logger::info).thenReturn(response);
    }

    private String getRedactedUrl(URL url) {
        return UrlBuilder.parse(url)
            .setQuery(getAllowedQueryString(url.getQuery()))
            .toString();
    }

    private String getAllowedQueryString(String queryString) {
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

    private void addHeadersToLogMessage(HttpHeaders headers, StringBuilder sb, int logLevel) {
        // Either headers shouldn't be logged or the logging level isn't set to VERBOSE, don't add headers.
        if (!httpLogDetailLevel.shouldLogHeaders() || logLevel > LogLevel.VERBOSE.toNumeric()) {
            return;
        }

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

    private String prettyPrintIfNeeded(ClientLogger logger, String contentType, String body) {
        String result = body;
        if (prettyPrintJSON && contentType != null
            && (contentType.startsWith("application/json") || contentType.startsWith("text/json"))) {
            try {
                final Object deserialized = PRETTY_PRINTER.readTree(body);
                result = PRETTY_PRINTER.writeValueAsString(deserialized);
            } catch (Exception e) {
                logger.warning("Failed to pretty print JSON: {}", e.getMessage());
            }
        }
        return result;
    }

    private long getContentLength(ClientLogger logger, HttpHeaders headers) {
        long contentLength = 0;
        try {
            contentLength = Long.parseLong(headers.getValue("content-length"));
        } catch (NumberFormatException | NullPointerException e) {
            logger.warning("Could not parse the HTTP header content-length: '{}'.",
                headers.getValue("content-length"), e);
        }

        return contentLength;
    }

    private boolean bodyIsPrintable(String contentTypeHeader, long contentLength) {
        return !APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
            && contentLength != 0
            && contentLength < MAX_BODY_LOG_SIZE;
    }
}
