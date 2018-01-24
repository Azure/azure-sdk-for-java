/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpHeaders;
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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Creates a RequestPolicy that handles logging of HTTP requests and responses.
 */
public class HttpLoggingPolicyFactory implements RequestPolicyFactory {
    private static final ObjectMapper PRETTY_PRINTER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final HttpLogDetailLevel detailLevel;
    private final boolean prettyPrintJSON;

    /**
     * Creates an HttpLoggingPolicyFactory with the given log level.
     *
     * @param detailLevel The HTTP logging detail level.
     */
    public HttpLoggingPolicyFactory(HttpLogDetailLevel detailLevel) {
        this(detailLevel, false);
    }

    /**
     * Creates an HttpLoggingPolicyFactory with the given log level and pretty printing setting.
     * @param detailLevel The HTTP logging detail level.
     * @param prettyPrintJSON If true, pretty prints JSON message bodies when logging.
     *                        If the detailLevel does not include body logging, this flag does nothing.
     */
    public HttpLoggingPolicyFactory(HttpLogDetailLevel detailLevel, boolean prettyPrintJSON) {
        this.detailLevel = detailLevel;
        this.prettyPrintJSON = prettyPrintJSON;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new LoggingPolicy(next);
    }

    private final class LoggingPolicy implements RequestPolicy {
        private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
        private final RequestPolicy next;

        private LoggingPolicy(RequestPolicy next) {
            this.next = next;
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

        @Override
        public Single<HttpResponse> sendAsync(final HttpRequest request) {
            String context = request.callerMethod();
            if (context == null) {
                context = "";
            }
            final Logger logger = LoggerFactory.getLogger(context);

            if (detailLevel.shouldLogURL()) {
                log(logger, String.format("--> %s %s", request.httpMethod(), request.url()));
            }

            if (detailLevel.shouldLogHeaders()) {
                for (HttpHeader header : request.headers()) {
                    log(logger, header.toString());
                }
            }

            Completable bodyLoggingTask = Completable.complete();
            if (detailLevel.shouldLogBody()) {
                if (request.body() == null) {
                    log(logger, "(empty body)");
                    log(logger, "--> END " + request.httpMethod());
                } else {
                    boolean isHumanReadableContentType = !"application/octet-stream".equalsIgnoreCase(request.headers().value("Content-Type"));
                    final long contentLength = getContentLength(request.headers());

                    if (contentLength < MAX_BODY_LOG_SIZE && isHumanReadableContentType) {
                        try {
                            Single<byte[]> collectedBytes = FlowableUtil.collectBytes(request.body());
                            bodyLoggingTask = collectedBytes.flatMapCompletable(new Function<byte[], CompletableSource>() {
                                @Override
                                public CompletableSource apply(byte[] bytes) throws Exception {
                                    String bodyString = new String(bytes, StandardCharsets.UTF_8);
                                    bodyString = prettyPrintIfNeeded(logger, request.headers().value("Content-Type"), bodyString);
                                    log(logger, String.format("%s-byte body:\n%s", contentLength, bodyString));
                                    log(logger, "--> END " + request.httpMethod());
                                    return Completable.complete();
                                }
                            });
                        } catch (Exception e) {
                            bodyLoggingTask = Completable.error(e);
                        }
                    } else {
                        log(logger, contentLength + "-byte body: (content not logged)");
                        log(logger, "--> END " + request.httpMethod());
                    }
                }
            }

            final long startNs = System.nanoTime();
            return bodyLoggingTask.andThen(next.sendAsync(request)).flatMap(new Function<HttpResponse, Single<HttpResponse>>() {
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

        private Single<HttpResponse> logResponse(final Logger logger, final HttpResponse response, URL url, long tookMs) {
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
                    return bufferedResponse.bodyAsStringAsync().map(new Function<String, HttpResponse>() {
                        @Override
                        public HttpResponse apply(String s) {
                            s = prettyPrintIfNeeded(logger, contentTypeHeader, s);
                            log(logger, "Response body:\n" + s);
                            log(logger, "<-- END HTTP");
                            return bufferedResponse;
                        }
                    });
                } else {
                    log(logger, "(body content not logged)");
                    log(logger, "<-- END HTTP");
                }
            } else {
                log(logger, "<-- END HTTP");
            }

            return Single.just(response);
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
    }
}


