// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.policy;

import com.typespec.core.http.ContentType;
import com.typespec.core.http.HttpHeader;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpPipelineCallContext;
import com.typespec.core.http.HttpPipelineNextPolicy;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.policy.HttpPipelinePolicy;
import com.typespec.core.test.TestMode;
import com.typespec.core.test.implementation.TestingHelpers;
import com.typespec.core.test.models.NetworkCallError;
import com.typespec.core.test.models.NetworkCallRecord;
import com.typespec.core.test.models.RecordedData;
import com.typespec.core.test.models.RecordingRedactor;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
    private static final int DEFAULT_BUFFER_LENGTH = 8192;
    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String X_MS_ENCRYPTION_KEY_SHA256 = "x-ms-encryption-key-sha256";
    private static final String STATUS_CODE = "StatusCode";
    private static final String BODY = "Body";
    private static final String SIG = "sig";

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
        if (TestingHelpers.getTestMode() != TestMode.RECORD) {
            return next.process();
        }

        final NetworkCallRecord networkCallRecord = new NetworkCallRecord();
        Map<String, String> headers = new HashMap<>();

        captureRequestHeaders(context.getHttpRequest().getHeaders(), headers, HttpHeaderName.X_MS_CLIENT_REQUEST_ID,
            HttpHeaderName.CONTENT_TYPE, X_MS_VERSION, HttpHeaderName.USER_AGENT);

        networkCallRecord.setHeaders(headers);
        networkCallRecord.setMethod(context.getHttpRequest().getHttpMethod().toString());

        // Remove sensitive information such as SAS token signatures from the recording.
        UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        redactedAccountName(urlBuilder);
        if (urlBuilder.getQuery().containsKey("sig")) {
            urlBuilder.setQueryParameter(SIG, "REDACTED");
        }
        String uriString = urlBuilder.toString();
        networkCallRecord.setUri(uriString.endsWith("?") ? uriString.substring(0, uriString.length() - 2) : uriString);

        return next.process()
            .doOnError(throwable -> {
                networkCallRecord.setException(new NetworkCallError(throwable));
                recordedData.addNetworkCall(networkCallRecord);
                throw logger.logExceptionAsWarning(Exceptions.propagate(throwable));
            }).flatMap(httpResponse -> extractResponseData(httpResponse, redactor, logger)
                .map(responseAndSessionRecordData -> {
                    Map<String, String> sessionRecordData = responseAndSessionRecordData.getT2();
                    networkCallRecord.setResponse(sessionRecordData);
                    String body = sessionRecordData.get(BODY);

                    // Remove pre-added header if this is a waiting or redirection
                    if (body != null && body.contains("<Status>InProgress</Status>")
                        || Integer.parseInt(sessionRecordData.get(STATUS_CODE)) == HttpURLConnection.HTTP_MOVED_TEMP) {
                        logger.info("Waiting for a response or redirection.");
                    } else {
                        recordedData.addNetworkCall(networkCallRecord);
                    }

                    return responseAndSessionRecordData.getT1();
                }));
    }

    private static void redactedAccountName(UrlBuilder urlBuilder) {
        String originalHost = urlBuilder.getHost();
        int indexOf = originalHost.indexOf(".");
        if (indexOf == -1) {
            urlBuilder.setHost("REDACTED");
        } else {
            urlBuilder.setHost("REDACTED" + originalHost.substring(indexOf));
        }
    }

    private static void captureRequestHeaders(HttpHeaders requestHeaders, Map<String, String> captureHeaders,
        HttpHeaderName... headerNames) {
        for (HttpHeaderName headerName : headerNames) {
            if (requestHeaders.getValue(headerName) != null) {
                captureHeaders.put(headerName.getCaseInsensitiveName(), requestHeaders.getValue(headerName));
            }
        }
    }

    private static Mono<Tuple2<HttpResponse, Map<String, String>>> extractResponseData(final HttpResponse response,
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

        String contentType = response.getHeaderValue(HttpHeaderName.CONTENT_TYPE);
        String contentLengthHeader = response.getHeaderValue(HttpHeaderName.CONTENT_LENGTH);

        if (!CoreUtils.isNullOrEmpty(contentLengthHeader) && Long.parseLong(contentLengthHeader) == 0) {
            return Mono.just(Tuples.of(response, responseData));
        }

        final HttpResponse bufferedResponse = response.buffer();
        return FluxUtil.collectBytesInByteBufferStream(bufferedResponse.getBody())
            .map(bytes -> {
                if (contentType == null) {
                    String content = new String(bytes, StandardCharsets.UTF_8);
                    responseData.put(CONTENT_LENGTH, Integer.toString(content.length()));
                    responseData.put(BODY, content);
                } else if (ContentType.APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentType)
                    || "avro/binary".equalsIgnoreCase(contentType)) {
                    responseData.put(BODY, Base64.getEncoder().encodeToString(bytes));
                } else if (contentType.contains("json")
                    || response.getHeaderValue(HttpHeaderName.CONTENT_ENCODING) == null) {
                    responseData.put(BODY, redactor.redact(CoreUtils.bomAwareToString(bytes,
                        response.getHeaderValue(HttpHeaderName.CONTENT_TYPE))));
                } else {
                    String content;
                    if ("gzip".equalsIgnoreCase(response.getHeaderValue(HttpHeaderName.CONTENT_ENCODING))) {
                        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
                             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[DEFAULT_BUFFER_LENGTH];
                            int position = 0;
                            int bytesRead = gis.read(buffer, position, buffer.length);

                            while (bytesRead != -1) {
                                output.write(buffer, 0, bytesRead);
                                position += bytesRead;
                                bytesRead = gis.read(buffer, 0, buffer.length);
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
                }

                return Tuples.of(bufferedResponse, responseData);
            })
            .switchIfEmpty(Mono.fromSupplier(() -> Tuples.of(bufferedResponse, responseData)));
    }
}
