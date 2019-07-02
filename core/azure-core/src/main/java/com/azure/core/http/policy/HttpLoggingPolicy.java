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
import io.netty.handler.codec.http.HttpResponseStatus;
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
     *                        If the detailLevel does not include body logging, this flag does nothing.
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
        Mono<Void> logRequest = logRequest(logger, context.httpRequest());
        Function<HttpResponse, Mono<HttpResponse>> logResponseDelegate = logResponseDelegate(logger, context.httpRequest().url(), startNs);
        //
        return logRequest.then(next.process()).flatMap(logResponseDelegate)
                .doOnError(throwable -> logger.logAsWarning("<-- HTTP FAILED: ", throwable));
    }

    private Mono<Void> logRequest(final ClientLogger logger, final HttpRequest request) {
        if (detailLevel.shouldLogURL()) {
            logger.logAsInfo("--> {} {}", request.httpMethod(), request.url());
        }

        if (detailLevel.shouldLogHeaders()) {
            for (HttpHeader header : request.headers()) {
                logger.logAsInfo(header.toString());
            }
        }
        //
        Mono<Void> reqBodyLoggingMono = Mono.empty();
        //
        if (detailLevel.shouldLogBody()) {
            if (request.body() == null) {
                logger.logAsInfo("(empty body)");
                logger.logAsInfo("--> END {}", request.httpMethod());
            } else {
                boolean isHumanReadableContentType = !"application/octet-stream".equalsIgnoreCase(request.headers().value("Content-Type"));
                final long contentLength = getContentLength(request.headers());

                if (contentLength < MAX_BODY_LOG_SIZE && isHumanReadableContentType) {
                    try {
                        Mono<byte[]> collectedBytes = FluxUtil.collectBytesInByteBufStream(request.body(), true);
                        reqBodyLoggingMono = collectedBytes.flatMap(bytes -> {
                            String bodyString = new String(bytes, StandardCharsets.UTF_8);
                            bodyString = prettyPrintIfNeeded(logger, request.headers().value("Content-Type"), bodyString);
                            logger.logAsInfo("{}-byte body:%n{}", contentLength, bodyString);
                            logger.logAsInfo("--> END {}", request.httpMethod());
                            return Mono.empty();
                        });
                    } catch (Exception e) {
                        reqBodyLoggingMono = Mono.error(e);
                    }
                } else {
                    logger.logAsInfo("{}-byte body: (content not logged)", contentLength);
                    logger.logAsInfo("--> END {}", request.httpMethod());
                }
            }
        }
        return reqBodyLoggingMono;
    }

    private Function<HttpResponse, Mono<HttpResponse>> logResponseDelegate(final ClientLogger logger, final URL url, final long startNs) {
        return (HttpResponse response) -> {
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            //
            String contentLengthString = response.headerValue("Content-Length");
            String bodySize;
            if (contentLengthString == null || contentLengthString.isEmpty()) {
                bodySize = "unknown-length";
            } else {
                bodySize = contentLengthString + "-byte";
            }

            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(response.statusCode());
            if (detailLevel.shouldLogURL()) {
                logger.logAsInfo("<-- {} {} {} ({} ms, {} body)", response.statusCode(), responseStatus.reasonPhrase(), url, tookMs, bodySize);
            }

            if (detailLevel.shouldLogHeaders()) {
                for (HttpHeader header : response.headers()) {
                    logger.logAsInfo(header.toString());
                }
            }

            if (detailLevel.shouldLogBody()) {
                long contentLength = getContentLength(response.headers());
                final String contentTypeHeader = response.headerValue("Content-Type");
                if (!"application/octet-stream".equalsIgnoreCase(contentTypeHeader)
                        && contentLength != 0 && contentLength < MAX_BODY_LOG_SIZE) {
                    final HttpResponse bufferedResponse = response.buffer();
                    return bufferedResponse.bodyAsString().map(bodyStr -> {
                        bodyStr = prettyPrintIfNeeded(logger, contentTypeHeader, bodyStr);
                        logger.logAsInfo("Response body:\n{}", bodyStr);
                        logger.logAsInfo("<-- END HTTP");
                        return bufferedResponse;
                    });
                } else {
                    logger.logAsInfo("(body content not logged)");
                    logger.logAsInfo("<-- END HTTP");
                }
            } else {
                logger.logAsInfo("<-- END HTTP");
            }
            return Mono.just(response);
        };
    }

    private String prettyPrintIfNeeded(ClientLogger logger, String contentType, String body) {
        String result = body;
        if (prettyPrintJSON && contentType != null && (contentType.startsWith("application/json") || contentType.startsWith("text/json"))) {
            try {
                final Object deserialized = PRETTY_PRINTER.readTree(body);
                result = PRETTY_PRINTER.writeValueAsString(deserialized);
            } catch (Exception e) {
                logger.logAsWarning("Failed to pretty print JSON: {}", e.getMessage());
            }
        }
        return result;
    }

    private long getContentLength(HttpHeaders headers) {
        long contentLength = 0;
        try {
            contentLength = Long.parseLong(headers.value("content-length"));
        } catch (NumberFormatException | NullPointerException ignored) {
        }

        return contentLength;
    }
}
