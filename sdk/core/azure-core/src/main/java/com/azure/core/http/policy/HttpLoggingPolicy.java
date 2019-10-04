// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.implementation.util.FluxUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * The Pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicy implements HttpPipelinePolicy {
    private static final ObjectMapper PRETTY_PRINTER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final HttpLogDetailLevel detailLevel;
    private final boolean prettyPrintJSON;
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;

    /**
     * Creates an HttpLoggingPolicy with the given log level.
     *
     * @param detailLevel The HTTP logging detail level.
     */
    public HttpLoggingPolicy(HttpLogDetailLevel detailLevel) {
        this(detailLevel, false);
    }

    /**
     * Creates an HttpLoggingPolicy with the given log level and pretty printing setting.
     *
     * @param detailLevel The HTTP logging detail level.
     * @param prettyPrintJSON If true, pretty prints JSON message bodies when logging.
     *     If the detailLevel does not include body logging, this flag does nothing.
     */
    public HttpLoggingPolicy(HttpLogDetailLevel detailLevel, boolean prettyPrintJSON) {
        this.detailLevel = detailLevel;
        this.prettyPrintJSON = prettyPrintJSON;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        //
        Optional<Object> data = context.getData("caller-method");
        String callerMethod = (String) data.orElse("");
        //
        final ClientLogger logger = new ClientLogger(callerMethod);
        final long startNs = System.nanoTime();
        //
        Mono<Void> logRequest = logRequest(logger, context.getHttpRequest());
        Function<HttpResponse, Mono<HttpResponse>> logResponseDelegate =
            logResponseDelegate(logger, context.getHttpRequest().getUrl(), startNs);
        //
        return logRequest.then(next.process()).flatMap(logResponseDelegate)
            .doOnError(throwable -> logger.warning("<-- HTTP FAILED: ", throwable));
    }

    private Mono<Void> logRequest(final ClientLogger logger, final HttpRequest request) {
        if (detailLevel.shouldLogUrl()) {
            logger.info("--> {} {}", request.getHttpMethod(), request.getUrl());
        }

        if (detailLevel.shouldLogHeaders()) {
            for (HttpHeader header : request.getHeaders()) {
                logger.info(header.toString());
            }
        }
        //
        Mono<Void> reqBodyLoggingMono = Mono.empty();
        //
        if (detailLevel.shouldLogBody()) {
            if (request.getBody() == null) {
                logger.info("(empty body)");
                logger.info("--> END {}", request.getHttpMethod());
            } else {
                boolean isHumanReadableContentType =
                    !"application/octet-stream".equalsIgnoreCase(request.getHeaders().getValue("Content-Type"));
                final long contentLength = getContentLength(logger, request.getHeaders());

                if (contentLength < MAX_BODY_LOG_SIZE && isHumanReadableContentType) {
                    try {
                        Mono<byte[]> collectedBytes = FluxUtil.collectBytesInByteBufferStream(request.getBody());
                        reqBodyLoggingMono = collectedBytes.flatMap(bytes -> {
                            String bodyString = new String(bytes, StandardCharsets.UTF_8);
                            bodyString = prettyPrintIfNeeded(
                                logger,
                                request.getHeaders().getValue("Content-Type"),
                                bodyString);
                            logger.info("{}-byte body:%n{}", contentLength, bodyString);
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

    private Function<HttpResponse, Mono<HttpResponse>> logResponseDelegate(final ClientLogger logger, final URL url,
                                                                           final long startNs) {
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

            //            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(response.statusCode());
            if (detailLevel.shouldLogUrl()) {
                logger.info("<-- {} {} ({} ms, {} body)", response.getStatusCode(), url, tookMs, bodySize);
            }

            if (detailLevel.shouldLogHeaders()) {
                for (HttpHeader header : response.getHeaders()) {
                    logger.info(header.toString());
                }
            }

            if (detailLevel.shouldLogBody()) {
                long contentLength = getContentLength(logger, response.getHeaders());
                final String contentTypeHeader = response.getHeaderValue("Content-Type");
                if (!"application/octet-stream".equalsIgnoreCase(contentTypeHeader)
                    && contentLength != 0 && contentLength < MAX_BODY_LOG_SIZE) {
                    final HttpResponse bufferedResponse = response.buffer();
                    return bufferedResponse.getBodyAsString().map(bodyStr -> {
                        bodyStr = prettyPrintIfNeeded(logger, contentTypeHeader, bodyStr);
                        logger.info("Response body:\n{}", bodyStr);
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

    private long getContentLength(ClientLogger logger, HttpHeaders headers) {
        long contentLength = 0;
        try {
            contentLength = Long.parseLong(headers.getValue("content-length"));
        } catch (NumberFormatException | NullPointerException ignored) {
            logger.warning("Http logging cannot parse the content-length: " + headers.getValue("content-length"));
        }

        return contentLength;
    }
}
