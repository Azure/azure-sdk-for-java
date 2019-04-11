// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http.policy;

import com.azure.common.http.HttpHeader;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.implementation.util.FluxUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        String callerMethod;
        if (!data.isPresent() || data.get() == null) {
            callerMethod = "";
        } else {
            callerMethod = (String) data.get();
        }
        //
        final Logger logger = LoggerFactory.getLogger(callerMethod);
        final long startNs = System.nanoTime();
        //
        Mono<Void> logRequest = logRequest(logger, context.httpRequest());
        Function<HttpResponse, Mono<HttpResponse>> logResponseDelegate = logResponseDelegate(logger, context.httpRequest().url(), startNs);
        //
        return logRequest.then(next.process()).flatMap(logResponseDelegate)
                .doOnError(throwable -> log(logger, "<-- HTTP FAILED: " + throwable));
    }

    private Mono<Void> logRequest(final Logger logger, final HttpRequest request) {
        if (detailLevel.shouldLogURL()) {
            log(logger, String.format("--> %s %s", request.httpMethod(), request.url()));
        }

        if (detailLevel.shouldLogHeaders()) {
            for (HttpHeader header : request.headers()) {
                log(logger, header.toString());
            }
        }
        //
        Mono<Void> reqBodyLoggingMono = Mono.empty();
        //
        if (detailLevel.shouldLogBody()) {
            if (request.body() == null) {
                log(logger, "(empty body)");
                log(logger, "--> END " + request.httpMethod());
            } else {
                boolean isHumanReadableContentType = !"application/octet-stream".equalsIgnoreCase(request.headers().value("Content-Type"));
                final long contentLength = getContentLength(request.headers());

                if (contentLength < MAX_BODY_LOG_SIZE && isHumanReadableContentType) {
                    try {
                        Mono<byte[]> collectedBytes = FluxUtil.collectBytesInByteBufStream(request.body(), true);
                        reqBodyLoggingMono = collectedBytes.flatMap(bytes -> {
                            String bodyString = new String(bytes, StandardCharsets.UTF_8);
                            bodyString = prettyPrintIfNeeded(logger, request.headers().value("Content-Type"), bodyString);
                            log(logger, String.format("%s-byte body:%n%s", contentLength, bodyString));
                            log(logger, "--> END " + request.httpMethod());
                            return Mono.empty();
                        });
                    } catch (Exception e) {
                        reqBodyLoggingMono = Mono.error(e);
                    }
                } else {
                    log(logger, contentLength + "-byte body: (content not logged)");
                    log(logger, "--> END " + request.httpMethod());
                }
            }
        }
        return reqBodyLoggingMono;
    }

    private Function<HttpResponse, Mono<HttpResponse>> logResponseDelegate(final Logger logger, final URL url, final long startNs) {
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
                log(logger, String.format("<-- %s %s %s (%s ms, %s body)", response.statusCode(), responseStatus.reasonPhrase(), url, tookMs, bodySize));
            }

            if (detailLevel.shouldLogHeaders()) {
                for (HttpHeader header : response.headers()) {
                    log(logger, header.toString());
                }
            }

            if (detailLevel.shouldLogBody()) {
                long contentLength = getContentLength(response.headers());
                final String contentTypeHeader = response.headerValue("Content-Type");
                if ((contentTypeHeader == null || !"application/octet-stream".equalsIgnoreCase(contentTypeHeader))
                        && contentLength != 0 && contentLength < MAX_BODY_LOG_SIZE) {
                    final HttpResponse bufferedResponse = response.buffer();
                    return bufferedResponse.bodyAsString().map(bodyStr -> {
                        bodyStr = prettyPrintIfNeeded(logger, contentTypeHeader, bodyStr);
                        log(logger, "Response body:\n" + bodyStr);
                        log(logger, "<-- END HTTP");
                        return bufferedResponse;
                    });
                } else {
                    log(logger, "(body content not logged)");
                    log(logger, "<-- END HTTP");
                }
            } else {
                log(logger, "<-- END HTTP");
            }
            return Mono.just(response);
        };
    }

    private String prettyPrintIfNeeded(Logger logger, String contentType, String body) {
        String result = body;
        if (prettyPrintJSON && contentType != null && (contentType.startsWith("application/json") || contentType.startsWith("text/json"))) {
            try {
                final Object deserialized = PRETTY_PRINTER.readTree(body);
                result = PRETTY_PRINTER.writeValueAsString(deserialized);
            } catch (Exception e) {
                log(logger, "Failed to pretty print JSON: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * Process the log using an SLF4j logger and an HTTP message.
     *
     * @param logger the SLF4j logger with the context of the request
     * @param s      the message for logging
     */
    private void log(Logger logger, String s) {
        logger.info(s);
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
