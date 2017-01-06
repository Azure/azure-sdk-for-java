/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest.interceptors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.microsoft.rest.LogLevel;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoggingInterceptor implements Interceptor {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private LogLevel logLevel;

    public LoggingInterceptor(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // get logger
        Request request = chain.request();
        String context = request.header("x-ms-logging-context");
        if (context == null) {
            context = getClass().getName();
        } else {
            request = request.newBuilder().removeHeader("x-ms-logging-context").build();
        }
        Logger logger = LoggerFactory.getLogger(context);

        if (logger.isInfoEnabled()) {
            // log URL
            if (logLevel != LogLevel.NONE) {
                logger.info("--> {} {}", request.method(), request.url());
            }
            // log headers
            if (logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY_AND_HEADERS) {
                for (Map.Entry<String, List<String>> header : request.headers().toMultimap().entrySet()) {
                    logger.info("{}: {}", header.getKey(), Joiner.on(", ").join(header.getValue()));
                }
            }
            // log body
            if (logLevel == LogLevel.BODY || logLevel == LogLevel.BODY_AND_HEADERS) {
                if (request.body() != null) {
                    Buffer buffer = new Buffer();
                    request.body().writeTo(buffer);

                    Charset charset = Charset.forName("UTF8");
                    MediaType contentType = request.body().contentType();
                    if (contentType != null) {
                        charset = contentType.charset(charset);
                    }

                    if (isPlaintext(buffer)) {
                        String content = buffer.clone().readString(charset);
                        if (logLevel.isPrettyJson()) {
                            try {
                                content = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readValue(content, JsonNode.class));
                            } catch (Exception e) {
                                // swallow, keep original content
                            }
                        }
                        logger.info("{}-byte body:\n{}", request.body().contentLength(), content);
                        logger.info("--> END " + request.method());
                    } else {
                        logger.info("--> END " + request.method() + " (binary "
                                + request.body().contentLength() + "-byte body omitted)");
                    }
                }
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.info("<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        if (logger.isInfoEnabled()) {
            ResponseBody responseBody = response.body();
            long contentLength = responseBody.contentLength();
            String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";

            // log URL
            if (logLevel != LogLevel.NONE) {
                logger.info("<-- {} {} {} ({} ms, {} body)", response.code(), response.message(), response.request().url(), tookMs, bodySize);
            }

            // log headers
            if (logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY_AND_HEADERS) {
                for (Map.Entry<String, List<String>> header : response.headers().toMultimap().entrySet()) {
                    logger.info("{}: {}", header.getKey(), Joiner.on(", ").join(header.getValue()));
                }
            }

            // log body
            if (logLevel == LogLevel.BODY || logLevel == LogLevel.BODY_AND_HEADERS) {
                if (response.body() != null) {
                    BufferedSource source = responseBody.source();
                    source.request(Long.MAX_VALUE); // Buffer the entire body.
                    Buffer buffer = source.buffer();

                    Charset charset = Charset.forName("UTF8");
                    MediaType contentType = responseBody.contentType();
                    if (contentType != null) {
                        try {
                            charset = contentType.charset(charset);
                        } catch (UnsupportedCharsetException e) {
                            logger.info("Couldn't decode the response body; charset is likely malformed.");
                            logger.info("<-- END HTTP");
                            return response;
                        }
                    }

                    if (!isPlaintext(buffer)) {
                        logger.info("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                        return response;
                    }

                    if (contentLength != 0) {
                        String content = buffer.clone().readString(charset);
                        if (logLevel.isPrettyJson()) {
                            try {
                                content = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readValue(content, JsonNode.class));
                            } catch (Exception e) {
                                // swallow, keep original content
                            }
                        }
                        logger.info("{}-byte body:\n{}", buffer.size(), content);
                    }
                    logger.info("<-- END HTTP");
                }
            }
        }
        return response;
    }

    public LogLevel logLevel() {
        return logLevel;
    }

    public LoggingInterceptor withlogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    private static boolean isPlaintext(Buffer buffer) throws EOFException {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false;
        }
    }
}
