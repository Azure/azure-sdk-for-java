/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.LogLevel;
import com.microsoft.rest.v2.http.ByteArrayHttpRequestBody;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

/**
 * An OkHttp interceptor that handles logging of HTTP requests and responses.
 */
public final class LoggingPolicy implements RequestPolicy {
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
        public RequestPolicy create(RequestPolicy next) {
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

        if (logLevel.shouldLogBody() && request.body() != null) {
            // TODO: maximum content-length?
            // TODO: check MIME type?
            if (request.body() instanceof ByteArrayHttpRequestBody) {
                String bodyString = new String(((ByteArrayHttpRequestBody) request.body()).content());
                log(logger, String.format("%s-byte body:\n%s", request.body().contentLength(), bodyString));
                log(logger, "--> END " + request.httpMethod());
            }
        }

        final long startNs = System.nanoTime();
        return next.sendAsync(request).flatMap(new Func1<HttpResponse, Single<HttpResponse>>() {
            @Override
            public Single<HttpResponse> call(HttpResponse httpResponse) {
                long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
                return logResponse(logger, httpResponse, request.url(), tookMs);
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                log(logger, "<-- HTTP FAILED: " + throwable);
            }
        });
    }

    private Single<HttpResponse> logResponse(final Logger logger, final HttpResponse response, String url, long tookMs) {
        String bodySize;
        try {
            long contentLength = Long.parseLong(response.headerValue("Content-Length"));
            bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        } catch (NumberFormatException e) {
            bodySize = "unknown-length";
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
            String contentTypeHeader = response.headerValue("Content-Type");
            if ((contentTypeHeader == null
                    || "application/json".equals(contentTypeHeader))
                    || "application/xml".equals(contentTypeHeader)) {
                final HttpResponse bufferedResponse = response.buffer();
                return bufferedResponse.bodyAsStringAsync().map(new Func1<String, HttpResponse>() {
                    @Override
                    public HttpResponse call(String s) {
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
}
