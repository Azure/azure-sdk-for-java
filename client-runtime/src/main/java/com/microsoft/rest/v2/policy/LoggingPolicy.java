/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.google.common.base.Charsets;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * An OkHttp interceptor that handles logging of HTTP requests and responses.
 */
public final class LoggingPolicy implements RequestPolicy {
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private final RequestPolicy next;

    /**
     * Factory for creating LoggingPolicy instances in a chain.
     */
    public static class Factory implements RequestPolicy.Factory {
        private final LogLevel logLevel;

        /**
         * Creates a LoggingPolicy.Factory with the given log level.
         * @param logLevel The log level.
         */
        public Factory(LogLevel logLevel) {
            this.logLevel = logLevel;
        }

        @Override
        public RequestPolicy create(RequestPolicy next, RequestPolicy.Options options) {
            return new LoggingPolicy(logLevel, next);
        }
    }

    private final LogLevel logLevel;

    /**
     * Creates an interceptor with a LogLevel enum.
     * @param logLevel the level of traffic to log
     */
    private LoggingPolicy(LogLevel logLevel, RequestPolicy next) {
        this.logLevel = logLevel;
        this.next = next;
    }

    /**
     * Process the log using an SLF4j logger and an HTTP message.
     * @param logger the SLF4j logger with the context of the request
     * @param s the message for logging
     */
    private void log(Logger logger, String s) {
        logger.info(s);
    }

    @Override
    public Single<HttpResponse> sendAsync(final HttpRequest request) {
        String context = request.callerMethod();
        if (context == null) {
            context = "";
        }
        final Logger logger = LoggerFactory.getLogger(context);

        if (logLevel.shouldLogURL()) {
            log(logger, String.format("--> %s %s", request.httpMethod(), request.url()));
        }

        if (logLevel.shouldLogHeaders()) {
            for (HttpHeader header : request.headers()) {
                log(logger, header.toString());
            }
        }

        Completable bodyLoggingOrNothing = Completable.complete();
        if (logLevel.shouldLogBody()) {
            if (request.body() == null) {
                log(logger, "(empty body)");
                log(logger, "--> END " + request.httpMethod());
            } else {
                boolean isHumanReadableContentType = !"application/octet-stream".equalsIgnoreCase(request.body().contentType());
                if (request.body().contentLength() < MAX_BODY_LOG_SIZE && isHumanReadableContentType) {
                    try {
                        Single<byte[]> collectedBytes = FlowableUtil.collectBytes(request.body().buffer().content());

                        // FIXME: stalls out
                        bodyLoggingOrNothing = collectedBytes.flatMapCompletable(new Function<byte[], CompletableSource>() {
                            @Override
                            public CompletableSource apply(byte[] bytes) throws Exception {
                                String bodyString = new String(bytes, Charsets.UTF_8);
                                log(logger, String.format("%s-byte body:\n%s", request.body().contentLength(), bodyString));
                                log(logger, "--> END " + request.httpMethod());
                                return Completable.complete();
                            }
                        });
                    } catch (IOException e) {
                        bodyLoggingOrNothing = Completable.error(e);
                    }
                } else {
                    log(logger, request.body().contentLength() + "-byte body: (content not logged)");
                    log(logger, "--> END " + request.httpMethod());
                }
            }
        }

        final long startNs = System.nanoTime();
        return bodyLoggingOrNothing.andThen(next.sendAsync(request)).flatMap(new Function<HttpResponse, Single<HttpResponse>>() {
            @Override
            public Single<HttpResponse> apply(HttpResponse httpResponse) {
                long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
                return logResponse(logger, httpResponse, request.url(), tookMs);
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                log(logger, "<-- HTTP FAILED: " + throwable);
            }
        });
    }

    private Single<HttpResponse> logResponse(final Logger logger, final HttpResponse response, String url, long tookMs) {
        String contentLengthString = response.headerValue("Content-Type");
        String bodySize;
        if (contentLengthString == null || contentLengthString.isEmpty()) {
            bodySize = "unknown-length";
        } else {
            bodySize = contentLengthString + "-byte";
        }

        HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(response.statusCode());
        if (logLevel.shouldLogURL()) {
            log(logger, String.format("<-- %s %s %s (%s ms, %s body)", response.statusCode(), responseStatus.reasonPhrase(), url, tookMs, bodySize));
        }

        if (logLevel.shouldLogHeaders()) {
            for (HttpHeader header : response.headers()) {
                log(logger, header.toString());
            }
        }

        if (logLevel.shouldLogBody()) {
            // FIXME: content length can be null
            long contentLength = Long.parseLong(contentLengthString);
            String contentTypeHeader = response.headerValue("Content-Type");
            if ((contentTypeHeader == null || !"application/octet-stream".equalsIgnoreCase(contentTypeHeader))
                    && contentLength < MAX_BODY_LOG_SIZE) {
                final HttpResponse bufferedResponse = response.buffer();
                return bufferedResponse.bodyAsStringAsync().map(new Function<String, HttpResponse>() {
                    @Override
                    public HttpResponse apply(String s) {
                        log(logger, s);
                        log(logger, "<-- END HTTP");
                        return bufferedResponse;
                    }
                });
            } else {
                log(logger, "Not logging response body because the Content-Type is " + contentTypeHeader);
                log(logger, "<-- END HTTP");
            }
        } else {
            log(logger, "<-- END HTTP");
        }

        return Single.just(response);
    }

    /**
     * Describes the level of HTTP traffic to log.
     */
    public enum LogLevel {
        /**
         * Logging is turned off.
         */
        NONE,

        /**
         * Logs only URLs, HTTP methods, and time to finish the request.
         */
        BASIC,

        /**
         * Logs everything in BASIC, plus all the request and response headers.
         */
        HEADERS,

        /**
         * Logs everything in BASIC, plus all the request and response body.
         * Note that only payloads in plain text or plan text encoded in GZIP
         * will be logged.
         */
        BODY,

        /**
         * Logs everything in HEADERS and BODY.
         */
        BODY_AND_HEADERS;

        // FIXME: this flag is not currently honored
        private boolean prettyJson = false;

        /**
         * @return if the JSON payloads will be prettified when log level is set
         * to BODY or BODY_AND_HEADERS. Default is false.
         */
        public boolean isPrettyJson() {
            return prettyJson;
        }

        /**
         * Specifies whether to log prettified JSON.
         * @param prettyJson true if JSON paylods are prettified.
         * @return the enum object
         */
        public LogLevel withPrettyJson(boolean prettyJson) {
            this.prettyJson = prettyJson;
            return this;
        }

        /**
         * @return a value indicating whether a request's URL should be logged.
         */
        public boolean shouldLogURL() {
            return this != NONE;
        }

        /**
         * @return a value indicating whether HTTP message headers should be logged.
         */
        public boolean shouldLogHeaders() {
            return this == HEADERS || this == BODY_AND_HEADERS;
        }

        /**
         * @return a value indicating whether HTTP message bodies should be logged.
         */
        public boolean shouldLogBody() {
            return this == BODY || this == BODY_AND_HEADERS;
        }
    }
}
