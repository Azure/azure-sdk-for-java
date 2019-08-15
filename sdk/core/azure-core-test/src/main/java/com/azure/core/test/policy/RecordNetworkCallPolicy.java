// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.util.logging.ClientLogger;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * HTTP Pipeline policy that keeps track of each HTTP request and response that flows through the pipeline.
 * Data is recorded into {@link RecordedData}.
 */
public class RecordNetworkCallPolicy implements HttpPipelinePolicy {
    private static final int DEFAULT_BUFFER_LENGTH = 1024;

    private final ClientLogger logger = new ClientLogger(RecordNetworkCallPolicy.class);
    private final RecordedData recordedData;

    /**
     * Creates a policy that records network calls into {@code recordedData}.
     *
     * @param recordedData The record to persist network calls into.
     */
    public RecordNetworkCallPolicy(RecordedData recordedData) {
        Objects.requireNonNull(recordedData);
        this.recordedData = recordedData;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final NetworkCallRecord networkCallRecord = new NetworkCallRecord();
        Map<String, String> headers = new HashMap<>();

        if (context.httpRequest().headers().value("Content-Type") != null) {
            headers.put("Content-Type", context.httpRequest().headers().value("Content-Type"));
        }
        if (context.httpRequest().headers().value("x-ms-version") != null) {
            headers.put("x-ms-version", context.httpRequest().headers().value("x-ms-version"));
        }
        if (context.httpRequest().headers().value("User-Agent") != null) {
            headers.put("User-Agent", context.httpRequest().headers().value("User-Agent"));
        }

        networkCallRecord.headers(headers);
        networkCallRecord.method(context.httpRequest().httpMethod().toString());
        networkCallRecord.uri(context.httpRequest().url().toString().replaceAll("\\?$", ""));

        return next.process().flatMap(httpResponse -> {
            final HttpResponse bufferedResponse = httpResponse.buffer();

            return extractResponseData(bufferedResponse).map(responseData -> {
                networkCallRecord.response(responseData);
                String body = responseData.get("Body");

                // Remove pre-added header if this is a waiting or redirection
                if (body != null && body.contains("<Status>InProgress</Status>")
                    || Integer.parseInt(responseData.get("StatusCode")) == HttpResponseStatus.TEMPORARY_REDIRECT.code()) {
                    logger.info("Waiting for a response or redirection.");
                } else {
                    recordedData.addNetworkCall(networkCallRecord);
                }

                return bufferedResponse;
            });
        });
    }

    private Mono<Map<String, String>> extractResponseData(final HttpResponse response) {
        final Map<String, String> responseData = new HashMap<>();
        responseData.put("StatusCode", Integer.toString(response.statusCode()));

        boolean addedRetryAfter = false;
        for (HttpHeader header : response.headers()) {
            String headerValueToStore = header.value();

            if (header.name().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            }
            responseData.put(header.name(), headerValueToStore);
        }

        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        String contentType = response.headerValue("content-type");
        if (contentType == null) {
            return Mono.just(responseData);
        } else if (contentType.contains("json") || response.headerValue("content-encoding") == null) {
            return response.bodyAsString().switchIfEmpty(Mono.just("")).map(content -> {
                responseData.put("Body", content);
                return responseData;
            });
        } else {
            return response.bodyAsByteArray().map(bytes -> {
                String content;
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_LENGTH];
                    int position = 0;
                    int bytesRead = gis.read(buffer, position, buffer.length);

                    while (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead);
                        position += bytesRead;
                        bytesRead = gis.read(buffer, position, buffer.length);
                    }

                    content = new String(output.toByteArray(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw logger.logExceptionAsError(Exceptions.propagate(e));
                }

                responseData.remove("content-encoding");
                responseData.put("content-length", Integer.toString(content.length()));

                responseData.put("body", content);
                return responseData;
            });
        }
    }
}
