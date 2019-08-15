// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.test.models.NetworkCallException;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * HTTP Pipeline policy that keeps track of each HTTP request and response that flows through the pipeline. Data is
 * recorded into {@link RecordedData}.
 */
public class RecordNetworkCallPolicy implements HttpPipelinePolicy {
    private static final int DEFAULT_BUFFER_LENGTH = 1024;
    private final Logger logger = LoggerFactory.getLogger(RecordNetworkCallPolicy.class);
    private final RecordedData recordedData;
    private final Pattern StorageUserDelegationKeyRedactionPattern = Pattern.compile("<UserDelegationKey>.*<Value>(.*)</Value>.*</UserDelegationKey>");

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

        // Remove sensitive information such as SAS token signatures from the recording.
        UrlBuilder urlBuilder = UrlBuilder.parse(context.httpRequest().url());
        if (urlBuilder.query().containsKey("sig")) {
            urlBuilder.setQueryParameter("sig", "REDACTED");
        }
        networkCallRecord.uri(urlBuilder.toString().replaceAll("\\?$", ""));

        return next.process()
            .doOnError(throwable -> {
                networkCallRecord.exception(new NetworkCallException(throwable));
                recordedData.addNetworkCall(networkCallRecord);
                throw Exceptions.propagate(throwable);
            }).flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                return extractResponseData(bufferedResponse).map(responseData -> {
                    networkCallRecord.response(responseData);
                    String body = responseData.get("Body");

                    // Remove pre-added header if this is a waiting or redirection
                    if (body != null && body.contains("<Status>InProgress</Status>")
                        || Integer.parseInt(responseData.get("StatusCode")) == HttpResponseStatus.TEMPORARY_REDIRECT.code()) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Waiting for a response or redirection.");
                        }
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

        String contentType = response.headerValue("Content-Type");
        if (contentType == null) {
            return Mono.just(responseData);
        } else if (contentType.contains("octet-stream")) {
            return response.bodyAsByteArray().switchIfEmpty(Mono.just(new byte[0])).map(bytes -> {
                if (bytes.length == 0) {
                    return responseData;
                }

                responseData.put("Body", Arrays.toString(bytes));
                return responseData;
            });
        } else if (contentType.contains("json") || response.headerValue("Content-Encoding") == null) {
            return response.bodyAsString(StandardCharsets.UTF_8).switchIfEmpty(Mono.just("")).map(content -> {
                Matcher matcher = StorageUserDelegationKeyRedactionPattern.matcher(content);
                if (matcher.matches()) {
                    String test = matcher.replaceAll(Base64.encode("REDACTED".getBytes(StandardCharsets.UTF_8)));
                }

                responseData.put("Body", content);
                return responseData;
            });
        } else {
            return response.bodyAsByteArray().switchIfEmpty(Mono.just(new byte[0])).map(bytes -> {
                if (bytes.length == 0) {
                    return responseData;
                }

                String content;
                if ("gzip".equals(response.headerValue("Content-Encoding"))) {
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
                        throw Exceptions.propagate(e);
                    }
                } else {
                    content = new String(bytes, StandardCharsets.UTF_8);
                }

                responseData.remove("Content-Encoding");
                responseData.put("Content-Length", Integer.toString(content.length()));

                responseData.put("Body", content);
                return responseData;
            });
        }
    }
}
