/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.interceptors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.microsoft.rest.LogLevel;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * An OkHttp interceptor that handles logging of HTTP requests and responses.
 */
public class LoggingInterceptor implements Interceptor {
    private static final String LOGGING_HEADER = "x-ms-logging-context";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private LogLevel logLevel;

    /**
     * Creates an interceptor with a LogLevel enum.
     * @param logLevel the level of traffic to log
     */
    public LoggingInterceptor(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Process the log using an SLF4j logger and an HTTP message.
     * @param logger the SLF4j logger with the context of the request
     * @param s the message for logging
     */
    protected void log(Logger logger, String s) {
        logger.info(s);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // get logger
        Request request = chain.request();
        String context = request.header(LOGGING_HEADER);
        if (context == null) {
            context = "";
        }
        Logger logger = LoggerFactory.getLogger(context);

        // log URL
        if (logLevel != LogLevel.NONE) {
            log(logger, String.format("--> %s %s", request.method(), request.url()));
        }
        // log headers
        if (logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY_AND_HEADERS) {
            for (String header : request.headers().names()) {
                if (!LOGGING_HEADER.equals(header)) {
                    log(logger, String.format("%s: %s", header, Joiner.on(", ").join(request.headers(header))));
                }
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
                    log(logger, String.format("%s-byte body:\n%s", request.body().contentLength(), content));
                    log(logger, "--> END " + request.method());
                } else {
                    log(logger, "--> END " + request.method() + " (binary "
                            + request.body().contentLength() + "-byte body omitted)");
                }
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log(logger, "<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";

        // log URL
        if (logLevel != LogLevel.NONE) {
            log(logger, String.format("<-- %s %s %s (%s ms, %s body)", response.code(), response.message(), response.request().url(), tookMs, bodySize));
        }

        // log headers
        if (logLevel == LogLevel.HEADERS || logLevel == LogLevel.BODY_AND_HEADERS) {
            for (String header : response.headers().names()) {
                log(logger, String.format("%s: %s", header, Joiner.on(", ").join(request.headers(header))));
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
                        log(logger, "Couldn't decode the response body; charset is likely malformed.");
                        log(logger, "<-- END HTTP");
                        return response;
                    }
                }

                boolean gzipped = response.header("content-encoding") != null && StringUtils.containsIgnoreCase(response.header("content-encoding"), "gzip");
                if (!isPlaintext(buffer) && !gzipped) {
                    log(logger, "<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                    return response;
                }

                if (contentLength != 0) {
                    String content;
                    if (gzipped) {
                        content = CharStreams.toString(new InputStreamReader(new GZIPInputStream(buffer.clone().inputStream())));
                    } else {
                        content = buffer.clone().readString(charset);
                    }
                    if (logLevel.isPrettyJson()) {
                        try {
                            content = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readValue(content, JsonNode.class));
                        } catch (Exception e) {
                            // swallow, keep original content
                        }
                    }
                    log(logger, String.format("%s-byte body:\n%s", buffer.size(), content));
                }
                log(logger, "<-- END HTTP");
            }
        }
        return response;
    }

    /**
     * @return the current logging level.
     */
    public LogLevel logLevel() {
        return logLevel;
    }

    /**
     * Sets the current logging level.
     * @param logLevel the new logging level
     * @return the interceptor
     */
    public LoggingInterceptor withLogLevel(LogLevel logLevel) {
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
