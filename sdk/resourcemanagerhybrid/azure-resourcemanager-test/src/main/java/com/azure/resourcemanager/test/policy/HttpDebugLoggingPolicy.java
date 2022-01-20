// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.test.policy;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The pipeline policy that handles logging of HTTP requests and responses.
 */
public class HttpDebugLoggingPolicy implements HttpPipelinePolicy {

    private static final ObjectMapper PRETTY_PRINTER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final String REDACTED_PLACEHOLDER = "REDACTED";
    private static final Set<String> DISALLOWED_HEADER_NAMES = new HashSet<>();
    private static final boolean PRETTY_PRINT_BODY = true;

    /**
     * Creates an HttpDebugLoggingPolicy with the given log configurations.
     */
    public HttpDebugLoggingPolicy() {
        DISALLOWED_HEADER_NAMES.add("authorization");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final Logger logger = LoggerFactory.getLogger((String) context.getData("caller-method").orElse(""));
        final long startNs = System.nanoTime();

        return logRequest(logger, context.getHttpRequest(), context.getData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT))
            .then(next.process())
            .flatMap(response -> logResponse(logger, response, startNs))
            .doOnError(throwable -> logger.warn("<-- HTTP FAILED: ", throwable));
    }

    private Mono<Void> logRequest(final Logger logger, final HttpRequest request,
                                  final Optional<Object> optionalRetryCount) {
        if (!logger.isInfoEnabled()) {
            return Mono.empty();
        }

        StringBuilder requestLogMessage = new StringBuilder();
        requestLogMessage.append("--> ")
            .append(request.getHttpMethod())
            .append(" ")
            .append(request.getUrl())
            .append(System.lineSeparator());

        optionalRetryCount.ifPresent(o -> requestLogMessage.append("Try count: ")
            .append(o)
            .append(System.lineSeparator()));

        addHeadersToLogMessage(logger, request.getHeaders(), requestLogMessage);

        if (request.getBody() == null) {
            requestLogMessage.append("(empty body)")
                .append(System.lineSeparator())
                .append("--> END ")
                .append(request.getHttpMethod())
                .append(System.lineSeparator());

            return logAndReturn(logger, requestLogMessage, null);
        }

        String contentType = request.getHeaders().getValue("Content-Type");
        long contentLength = getContentLength(logger, request.getHeaders());

        if (shouldBodyBeLogged(contentType, contentLength)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) contentLength);
            WritableByteChannel bodyContentChannel = Channels.newChannel(outputStream);

            // Add non-mutating operators to the data stream.
            request.setBody(
                request.getBody()
                    .flatMap(byteBuffer -> writeBufferToBodyStream(bodyContentChannel, byteBuffer))
                    .doFinally(ignored -> {
                        requestLogMessage.append(contentLength)
                            .append("-byte body:")
                            .append(System.lineSeparator())
                            .append(prettyPrintIfNeeded(logger, contentType,
                                convertStreamToString(outputStream, logger)))
                            .append(System.lineSeparator())
                            .append("--> END ")
                            .append(request.getHttpMethod())
                            .append(System.lineSeparator());

                        logger.info(requestLogMessage.toString());
                    }));

            return Mono.empty();
        } else {
            requestLogMessage.append(contentLength)
                .append("-byte body: (content not logged)")
                .append(System.lineSeparator())
                .append("--> END ")
                .append(request.getHttpMethod())
                .append(System.lineSeparator());

            return logAndReturn(logger, requestLogMessage, null);
        }
    }

    /*
     * Logs thr HTTP response.
     *
     * @param logger Logger used to log the response.
     * @param response HTTP response returned from Azure.
     * @param startNs Nanosecond representation of when the request was sent.
     * @return A Mono containing the HTTP response.
     */
    private Mono<HttpResponse> logResponse(final Logger logger, final HttpResponse response, long startNs) {
        if (!logger.isInfoEnabled()) {
            return Mono.just(response);
        }

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        String contentLengthString = response.getHeaderValue("Content-Length");
        String bodySize = (CoreUtils.isNullOrEmpty(contentLengthString))
            ? "unknown-length body"
            : contentLengthString + "-byte body";

        StringBuilder responseLogMessage = new StringBuilder();
        responseLogMessage.append("<-- ")
            .append(response.getStatusCode())
            .append(" ")
            .append(response.getRequest().getUrl())
            .append(" (")
            .append(tookMs)
            .append(" ms, ")
            .append(bodySize)
            .append(")")
            .append(System.lineSeparator());

        addHeadersToLogMessage(logger, response.getHeaders(), responseLogMessage);

        String contentTypeHeader = response.getHeaderValue("Content-Type");
        long contentLength = getContentLength(logger, response.getHeaders());

        if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
            HttpResponse bufferedResponse = response.buffer();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) contentLength);
            WritableByteChannel bodyContentChannel = Channels.newChannel(outputStream);
            return bufferedResponse.getBody()
                .flatMap(byteBuffer -> writeBufferToBodyStream(bodyContentChannel, byteBuffer))
                .doFinally(ignored -> {
                    responseLogMessage.append("Response body:")
                        .append(System.lineSeparator())
                        .append(prettyPrintIfNeeded(logger, contentTypeHeader,
                            convertStreamToString(outputStream, logger)))
                        .append(System.lineSeparator())
                        .append("<-- END HTTP");

                    logger.info(responseLogMessage.toString());
                }).then(Mono.just(bufferedResponse));
        } else {
            responseLogMessage.append("(body content not logged)")
                .append(System.lineSeparator())
                .append("<-- END HTTP");

            return logAndReturn(logger, responseLogMessage, response);
        }
    }

    private <T> Mono<T> logAndReturn(Logger logger, StringBuilder logMessageBuilder, T data) {
        logger.info(logMessageBuilder.toString());
        return Mono.justOrEmpty(data);
    }

    private void addHeadersToLogMessage(Logger logger, HttpHeaders headers, StringBuilder sb) {
        for (HttpHeader header : headers) {
            String headerName = header.getName();
            sb.append(headerName).append(":");
            if (!DISALLOWED_HEADER_NAMES.contains(headerName.toLowerCase(Locale.ROOT))) {
                sb.append(header.getValue());
            } else {
                sb.append(REDACTED_PLACEHOLDER);
            }
            sb.append(System.lineSeparator());
        }
    }

    private String prettyPrintIfNeeded(Logger logger, String contentType, String body) {
        String result = body;
        if (PRETTY_PRINT_BODY && contentType != null
            && (contentType.startsWith(ContentType.APPLICATION_JSON) || contentType.startsWith("text/json"))) {
            try {
                final Object deserialized = PRETTY_PRINTER.readTree(body);
                result = PRETTY_PRINTER.writeValueAsString(deserialized);
            } catch (Exception e) {
                logger.warn("Failed to pretty print JSON: {}", e.getMessage());
            }
        }
        return result;
    }

    private long getContentLength(Logger logger, HttpHeaders headers) {
        long contentLength = 0;

        String contentLengthString = headers.getValue("Content-Length");
        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException | NullPointerException e) {
            logger.warn("Could not parse the HTTP header content-length: '{}'.",
                headers.getValue("content-length"), e);
        }

        return contentLength;
    }

    private boolean shouldBodyBeLogged(String contentTypeHeader, long contentLength) {
        return !ContentType.APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
            && contentLength != 0;
    }

    private static String convertStreamToString(ByteArrayOutputStream stream, Logger logger) {
        try {
            return stream.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    private static Mono<ByteBuffer> writeBufferToBodyStream(WritableByteChannel channel, ByteBuffer byteBuffer) {
        try {
            channel.write(byteBuffer.duplicate());
            return Mono.just(byteBuffer);
        } catch (IOException ex) {
            return Mono.error(ex);
        }
    }
}
