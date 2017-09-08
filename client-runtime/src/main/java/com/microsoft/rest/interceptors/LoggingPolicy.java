/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.interceptors;

import com.google.common.io.CharStreams;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;
import rx.functions.Action1;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * An OkHttp interceptor that handles logging of HTTP requests and responses.
 */
public class LoggingPolicy implements RequestPolicy {
    private final RequestPolicy next;

    public static class Factory implements RequestPolicy.Factory {
        private final LogLevel logLevel;

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
    public Single<HttpResponse> sendAsync(HttpRequest request) {
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

        if (logLevel.shouldLogBody()) {
            // TODO: maximum content-length?
            // TODO: check MIME type?
            InputStream is = request.body().createInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            try {
                String bodyString = CharStreams.toString(reader);
                log(logger, String.format("%s-byte body:\n%s", request.body().contentLength(), bodyString));
                log(logger, "--> END " + request.httpMethod());
            } catch (IOException e) {
                log(logger, "Exception occurred when reading body: " + e.getMessage());
            }
        }

        final long startNs = System.nanoTime();
        return next.sendAsync(request).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                log(logger, "<-- HTTP FAILED: " + throwable);
            }
        }).doOnSuccess(new Action1<HttpResponse>() {
            @Override
            public void call(HttpResponse httpResponse) {
                long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
                logResponse(logger, httpResponse, tookMs);
            }
        });
    }

    private static void logResponse(Logger logger, HttpResponse response, long tookMs) {
        throw new NotImplementedException("logResponse");
//        long contentLength = response.body.contentLength();
//        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
//
//        // log URL
//        if (logLevel != LogLevel.NONE) {
//            log(logger, String.format("<-- %s %s %s (%s ms, %s body)", response.code(), response.message(), response.request().url(), tookMs, bodySize));
//        }
//
//        // log headers
//        if (logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY_AND_HEADERS) {
//            for (String header : response.headers().names()) {
//                log(logger, String.format("%s: %s", header, Joiner.on(", ").join(response.headers(header))));
//            }
//        }
//
//        // log body
//        if (logLevel == LogLevel.BODY || logLevel == LogLevel.BODY_AND_HEADERS) {
//            if (response.body() != null) {
//                BufferedSource source = responseBody.source();
//                source.request(Long.MAX_VALUE); // Buffer the entire body.
//                Buffer buffer = source.buffer();
//
//                Charset charset = Charset.forName("UTF8");
//                MediaType contentType = responseBody.contentType();
//                if (contentType != null) {
//                    try {
//                        charset = contentType.charset(charset);
//                    } catch (UnsupportedCharsetException e) {
//                        log(logger, "Couldn't decode the response body; charset is likely malformed.");
//                        log(logger, "<-- END HTTP");
//                        return response;
//                    }
//                }
//
//                boolean gzipped = response.header("content-encoding") != null && StringUtils.containsIgnoreCase(response.header("content-encoding"), "gzip");
//                if (!isPlaintext(buffer) && !gzipped) {
//                    log(logger, "<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
//                    return response;
//                }
//
//                if (contentLength != 0) {
//                    String content;
//                    if (gzipped) {
//                        content = CharStreams.toString(new InputStreamReader(new GZIPInputStream(buffer.clone().inputStream())));
//                    } else {
//                        content = buffer.clone().readString(charset);
//                    }
//                    if (logLevel.isPrettyJson()) {
//                        try {
//                            content = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readValue(content, JsonNode.class));
//                        } catch (Exception e) {
//                            // swallow, keep original content
//                        }
//                    }
//                    log(logger, String.format("%s-byte body:\n%s", buffer.size(), content));
//                }
//                log(logger, "<-- END HTTP");
//            }
//        }
//        return response;
    }
}
