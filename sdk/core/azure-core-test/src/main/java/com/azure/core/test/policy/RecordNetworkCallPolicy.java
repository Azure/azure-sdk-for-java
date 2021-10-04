// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.test.models.NetworkCallError;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.models.RecordingRedactor;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

/**
 * HTTP Pipeline policy that keeps track of each HTTP request and response that flows through the pipeline. Data is
 * recorded into {@link RecordedData}.
 */
public class RecordNetworkCallPolicy implements HttpPipelinePolicy {
    private static final int DEFAULT_BUFFER_LENGTH = 1024;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String X_MS_CLIENT_REQUEST_ID = "x-ms-client-request-id";
    private static final String X_MS_ENCRYPTION_KEY_SHA256 = "x-ms-encryption-key-sha256";
    private static final String X_MS_VERSION = "x-ms-version";
    private static final String USER_AGENT = "User-Agent";
    private static final String STATUS_CODE = "StatusCode";
    private static final String BODY = "Body";
    private static final String SIG = "sig";

    private static final TestMode TEST_MODE = TestingHelpers.getTestMode();

    private final ClientLogger logger = new ClientLogger(RecordNetworkCallPolicy.class);
    private final RecordedData recordedData;
    private final RecordingRedactor redactor;

    /**
     * Creates a policy that records network calls into {@code recordedData}.
     *
     * @param recordedData The record to persist network calls into.
     */
    public RecordNetworkCallPolicy(RecordedData recordedData) {
        this(recordedData, Collections.emptyList());
    }

    /**
     * Creates a policy that records network calls into {@code recordedData} by redacting sensitive information by
     * applying the provided redactor functions.
     * @param recordedData The record to persist network calls into.
     * @param redactors The custom redactor functions to apply to redact sensitive information from recorded data.
     */
    public RecordNetworkCallPolicy(RecordedData recordedData, List<Function<String, String>> redactors) {
        this.recordedData = recordedData;
        redactor = new RecordingRedactor(redactors);

    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // If TEST_MODE isn't RECORD do not record.
        if (TEST_MODE != TestMode.RECORD) {
            return next.process();
        }

        final NetworkCallRecord networkCallRecord = new NetworkCallRecord();
        Map<String, String> headers = new HashMap<>();

        captureRequestHeaders(context.getHttpRequest().getHeaders(), headers,
            X_MS_CLIENT_REQUEST_ID,
            CONTENT_TYPE,
            X_MS_VERSION,
            USER_AGENT);

        networkCallRecord.setHeaders(headers);
        networkCallRecord.setMethod(context.getHttpRequest().getHttpMethod().toString());

        // Remove sensitive information such as SAS token signatures from the recording.
        UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        redactedAccountName(urlBuilder);
        if (urlBuilder.getQuery().containsKey(SIG)) {
            urlBuilder.setQueryParameter(SIG, "REDACTED");
        }
        String uriString = urlBuilder.toString();
        networkCallRecord.setUri(uriString.endsWith("?") ? uriString.substring(0, uriString.length() - 2) : uriString);

        return next.process()
            .doOnError(throwable -> {
                networkCallRecord.setException(new NetworkCallError(throwable));
                recordedData.addNetworkCall(networkCallRecord);
                throw logger.logExceptionAsWarning(Exceptions.propagate(throwable));
            }).flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                return extractResponseData(bufferedResponse, redactor, logger).map(responseData -> {
                    networkCallRecord.setResponse(responseData);
                    String body = responseData.get(BODY);

                    // Remove pre-added header if this is a waiting or redirection
                    if (body != null && body.contains("<Status>InProgress</Status>")
                        || Integer.parseInt(responseData.get(STATUS_CODE)) == HttpURLConnection.HTTP_MOVED_TEMP) {
                        logger.info("Waiting for a response or redirection.");
                    } else {
                        recordedData.addNetworkCall(networkCallRecord);
                    }

                    return bufferedResponse;
                });
            });
    }

    private static void redactedAccountName(UrlBuilder urlBuilder) {
        String[] hostParts = urlBuilder.getHost().split("\\.");
        hostParts[0] = "REDACTED";

        urlBuilder.setHost(String.join(".", hostParts));
    }

    private static void captureRequestHeaders(HttpHeaders requestHeaders, Map<String, String> captureHeaders,
        String... headerNames) {
        for (String headerName : headerNames) {
            if (requestHeaders.getValue(headerName) != null) {
                captureHeaders.put(headerName, requestHeaders.getValue(headerName));
            }
        }
    }

    private static Mono<Map<String, String>> extractResponseData(final HttpResponse response,
        final RecordingRedactor redactor, final ClientLogger logger) {
        final Map<String, String> responseData = new HashMap<>();
        responseData.put(STATUS_CODE, Integer.toString(response.getStatusCode()));

        boolean addedRetryAfter = false;
        for (HttpHeader header : response.getHeaders()) {
            String headerValueToStore = header.getValue();

            if (header.getName().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            } else if (header.getName().equalsIgnoreCase(X_MS_ENCRYPTION_KEY_SHA256)) {
                // The encryption key is sensitive information so capture it with a hidden value.
                headerValueToStore = "REDACTED";
            }

            responseData.put(header.getName(), headerValueToStore);
        }

        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        String contentType = response.getHeaderValue(CONTENT_TYPE);
        if (contentType == null) {
            return response.getBodyAsByteArray().switchIfEmpty(Mono.just(new byte[0])).map(bytes -> {
                if (bytes.length == 0) {
                    return responseData;
                }

                String content = new String(bytes, StandardCharsets.UTF_8);
                responseData.put(CONTENT_LENGTH, Integer.toString(content.length()));
                responseData.put(BODY, content);
                return responseData;
            });
        } else if (contentType.equalsIgnoreCase(ContentType.APPLICATION_OCTET_STREAM)
            || contentType.equalsIgnoreCase("avro/binary")) {
            return response.getBodyAsByteArray().switchIfEmpty(Mono.just(new byte[0])).map(bytes -> {
                if (bytes.length == 0) {
                    return responseData;
                }

                responseData.put(BODY, Base64.getEncoder().encodeToString(bytes));
                return responseData;
            });
        } else if (contentType.contains("json") || response.getHeaderValue(CONTENT_ENCODING) == null) {
            return response.getBodyAsString(StandardCharsets.UTF_8).switchIfEmpty(Mono.just("")).map(content -> {
                responseData.put(BODY, redactor.redact(content));
                return responseData;
            });
        } else {
            return response.getBodyAsByteArray().switchIfEmpty(Mono.just(new byte[0])).map(bytes -> {
                if (bytes.length == 0) {
                    return responseData;
                }

                String content;
                if ("gzip".equalsIgnoreCase(response.getHeaderValue(CONTENT_ENCODING))) {
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

                        content = output.toString("UTF-8");
                    } catch (IOException e) {
                        throw logger.logExceptionAsWarning(Exceptions.propagate(e));
                    }
                } else {
                    content = new String(bytes, StandardCharsets.UTF_8);
                }

                responseData.remove(CONTENT_ENCODING);
                responseData.put(CONTENT_LENGTH, Integer.toString(content.length()));

                responseData.put(BODY, content);
                return responseData;
            });
        }
    }
}
