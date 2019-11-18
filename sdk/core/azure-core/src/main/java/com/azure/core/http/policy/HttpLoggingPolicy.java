// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final ObjectMapper PRETTY_PRINTER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final HttpLogDetailLevel logDetailLevel;
    private final Set<String> allowedHttpHeaderNames;
    private final Pattern allowedHeaderPattern;
    private final Set<String> allowedQueryParamNames;
    private final boolean prettyPrintJSON;
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final String REDACTED_PLACEHOLDER = "REDACTED";

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
        if (httpLogOptions != null) {
            this.logDetailLevel = httpLogOptions.getLogLevel();
            this.allowedHttpHeaderNames = httpLogOptions.getAllowedHeaderNames()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            this.allowedQueryParamNames = httpLogOptions.getAllowedQueryParamNames()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

            Set<String> headerPatterns = httpLogOptions.getAllowedHeaderPatterns();
            if (CoreUtils.isNullOrEmpty(headerPatterns)) {
                this.allowedHeaderPattern = null;
            } else {
                StringBuilder patternBuilder = new StringBuilder();
                for (String headerPattern : headerPatterns) {
                    if (patternBuilder.length() > 0) {
                        patternBuilder.append("|");
                    }

                    patternBuilder.append("(?:")
                        .append(headerPattern)
                        .append(")");
                }

                this.allowedHeaderPattern = Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
            }
        } else {
            this.logDetailLevel = HttpLogDetailLevel.NONE;
            this.allowedHttpHeaderNames = Collections.emptySet();
            this.allowedHeaderPattern = null;
            this.allowedQueryParamNames = Collections.emptySet();
        }

        this.prettyPrintJSON = prettyPrintJSON;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // HttpLogDetailLevel is NONE perform a no-op.
        if (logDetailLevel == HttpLogDetailLevel.NONE) {
            return next.process();
        } else {
            Optional<Object> data = context.getData("caller-method");
            String callerMethod = (String) data.orElse("");
            //
            final ClientLogger logger = new ClientLogger(callerMethod);
            final long startNs = System.nanoTime();
            Mono<Void> logRequest = logRequest(logger, context.getHttpRequest());
            Function<HttpResponse, Mono<HttpResponse>> logResponseDelegate =
                logResponseDelegate(logger, context.getHttpRequest().getUrl(), startNs);
            //
            return logRequest.then(next.process()).flatMap(logResponseDelegate)
                .doOnError(throwable -> logger.warning("<-- HTTP FAILED: ", throwable));
        }
    }

    private Mono<Void> logRequest(final ClientLogger logger, final HttpRequest request) {
        // First log the URL information.
        if (logDetailLevel.shouldLogUrl()) {
            logger.info("--> {} {}", request.getHttpMethod(), request.getUrl());
            formatAllowableQueryParams(request.getUrl().getQuery(), logger);
        }

        // Then log the headers.
        logHeaders(request.getHeaders(), logger);

        // Finally log the body.
        Mono<Void> reqBodyLoggingMono = Mono.empty();
        if (logDetailLevel.shouldLogBody()) {
            if (request.getBody() == null) {
                logger.info("(empty body)");
                logger.info("--> END {}", request.getHttpMethod());
            } else {
                boolean isHumanReadableContentType =
                    !"application/octet-stream".equalsIgnoreCase(request.getHeaders().getValue("Content-Type"));
                final long contentLength = getContentLength(logger, request.getHeaders());

                if (contentLength > 0 && contentLength < MAX_BODY_LOG_SIZE && isHumanReadableContentType) {
                    try {
                        Mono<byte[]> collectedBytes = FluxUtil.collectBytesInByteBufferStream(request.getBody());
                        reqBodyLoggingMono = collectedBytes.flatMap(bytes -> {
                            String bodyString = new String(bytes, StandardCharsets.UTF_8);
                            bodyString = prettyPrintIfNeeded(
                                logger,
                                request.getHeaders().getValue("Content-Type"),
                                bodyString);
                            logger.info("{}-byte body:{}{}", contentLength, System.lineSeparator(), bodyString);
                            logger.info("--> END {}", request.getHttpMethod());
                            return Mono.empty();
                        });
                    } catch (Exception e) {
                        reqBodyLoggingMono = Mono.error(e);
                    }
                } else {
                    logger.info("{}-byte body: (content not logged)", contentLength);
                    logger.info("--> END {}", request.getHttpMethod());
                }
            }
        }
        return reqBodyLoggingMono;
    }

    private void logHeaders(HttpHeaders requestResponseHeaders, ClientLogger logger) {
        // If the HttpHeaders shouldn't be logged or there are no headers to log quit out early.
        if (!logDetailLevel.shouldLogHeaders() ||
            requestResponseHeaders == null ||
            requestResponseHeaders.getSize() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (HttpHeader header : requestResponseHeaders) {
            String headerName = header.getName();
            sb.append(headerName).append(":");
            if (allowedHttpHeaderNames.contains(headerName.toLowerCase(Locale.ROOT)) ||
                (allowedHeaderPattern != null && allowedHeaderPattern.matcher(headerName).find())) {
                sb.append(header.getValue());
            } else {
                sb.append(REDACTED_PLACEHOLDER);
            }
            sb.append(System.lineSeparator());
        }

        logger.info(sb.toString());
    }

    private void formatAllowableQueryParams(String queryString, ClientLogger logger) {
        if (CoreUtils.isNullOrEmpty(queryString)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        String[] queryParams = queryString.split("&");
        for (String queryParam : queryParams) {
            String[] queryPair = queryParam.split("=", 2);
            if (queryPair.length == 2) {
                String queryName = queryPair[0];
                if (allowedQueryParamNames.contains(queryName.toLowerCase(Locale.ROOT))) {
                    sb.append(queryParam);
                } else {
                    sb.append(queryPair[0]).append("=").append(REDACTED_PLACEHOLDER);
                }
            } else {
                sb.append(queryParam);
            }
            sb.append("&");
        }

        if (sb.length() > 0) {
            logger.info(sb.substring(0, sb.length() - 1));
        }
    }

    private Function<HttpResponse, Mono<HttpResponse>> logResponseDelegate(final ClientLogger logger,
        final URL url, final long startNs) {
        return (HttpResponse response) -> {
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            //
            String contentLengthString = response.getHeaderValue("Content-Length");
            String bodySize;
            if (contentLengthString == null || contentLengthString.isEmpty()) {
                bodySize = "unknown-length";
            } else {
                bodySize = contentLengthString + "-byte";
            }

            // HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(response.statusCode());
            if (logDetailLevel.shouldLogUrl()) {
                logger.info("<-- {} {} ({} ms, {} body)", response.getStatusCode(), url, tookMs, bodySize);
            }

            logHeaders(response.getHeaders(), logger);

            if (logDetailLevel.shouldLogBody()) {
                long contentLength = getContentLength(logger, response.getHeaders());
                final String contentTypeHeader = response.getHeaderValue("Content-Type");
                if (!"application/octet-stream".equalsIgnoreCase(contentTypeHeader)
                    && contentLength != 0 && contentLength < MAX_BODY_LOG_SIZE) {
                    final HttpResponse bufferedResponse = response.buffer();
                    return bufferedResponse.getBodyAsString().map(bodyStr -> {
                        bodyStr = prettyPrintIfNeeded(logger, contentTypeHeader, bodyStr);
                        logger.info("Response body:{}{}", System.lineSeparator(), bodyStr);
                        logger.info("<-- END HTTP");
                        return bufferedResponse;
                    }).switchIfEmpty(Mono.defer(() -> Mono.just(bufferedResponse)));
                } else {
                    logger.info("(body content not logged)");
                    logger.info("<-- END HTTP");
                }
            } else {
                logger.info("<-- END HTTP");
            }
            return Mono.just(response);
        };
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

    /*
     * Attempts to retrieve and parse the Content-Length header.
     */
    private long getContentLength(ClientLogger logger, HttpHeaders headers) {
        long contentLength = 0;
        String contentLengthHeader = headers.getValue("content-length");

        if (CoreUtils.isNullOrEmpty(contentLengthHeader)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthHeader);
        } catch (NumberFormatException | NullPointerException e) {
            logger.warning("Could not parse the HTTP header content-length: '{}'.", contentLengthHeader, e);
        }

        return contentLength;
    }
}
