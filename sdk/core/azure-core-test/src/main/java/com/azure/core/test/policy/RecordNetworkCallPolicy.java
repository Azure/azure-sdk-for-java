// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.test.models.NetworkCallError;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * HTTP Pipeline policy that keeps track of each HTTP request and response that flows through the pipeline. Data is
 * recorded into {@link RecordedData}.
 */
public class RecordNetworkCallPolicy implements HttpPipelinePolicy {
    private static final int DEFAULT_BUFFER_LENGTH = 1024;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String X_MS_VERSION = "x-ms-version";
    private static final String USER_AGENT = "User-Agent";
    private static final String STATUS_CODE = "StatusCode";
    private static final String BODY = "Body";
    private static final String SIG = "sig";

    private static final Pattern DELEGATIONKEY_KEY_PATTERN = Pattern.compile("(?:<Value>)(.*)(?:</Value>)");
    private static final Pattern DELEGATIONKEY_CLIENTID_PATTERN = Pattern.compile("(?:<SignedOid>)(.*)(?:</SignedOid>)");
    private static final Pattern DELEGATIONKEY_TENANTID_PATTERN = Pattern.compile("(?:<SignedTid>)(.*)(?:</SignedTid>)");

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

        if (context.httpRequest().headers().value(CONTENT_TYPE) != null) {
            headers.put(CONTENT_TYPE, context.httpRequest().headers().value(CONTENT_TYPE));
        }
        if (context.httpRequest().headers().value(X_MS_VERSION) != null) {
            headers.put(X_MS_VERSION, context.httpRequest().headers().value(X_MS_VERSION));
        }
        if (context.httpRequest().headers().value(USER_AGENT) != null) {
            headers.put(USER_AGENT, context.httpRequest().headers().value(USER_AGENT));
        }

        networkCallRecord.headers(headers);
        networkCallRecord.method(context.httpRequest().httpMethod().toString());

        // Remove sensitive information such as SAS token signatures from the recording.
        UrlBuilder urlBuilder = UrlBuilder.parse(context.httpRequest().url());
        if (urlBuilder.query().containsKey(SIG)) {
            urlBuilder.setQueryParameter(SIG, "REDACTED");
        }
        networkCallRecord.uri(urlBuilder.toString().replaceAll("\\?$", ""));

        return next.process()
            .doOnError(throwable -> {
                //handle the case when exception thrown after the interceptorManager
                networkCallRecord.exception(new NetworkCallError(throwable));
                recordedData.addNetworkCall(networkCallRecord);
                constructExceptionType(networkCallRecord);
            }).flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                return extractResponseData(bufferedResponse).map(responseData -> {
                    networkCallRecord.response(responseData);
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

    private Mono<Map<String, String>> extractResponseData(final HttpResponse response) {
        final Map<String, String> responseData = new HashMap<>();
        responseData.put(STATUS_CODE, Integer.toString(response.statusCode()));

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

        String contentType = response.headerValue(CONTENT_TYPE);
        if (contentType == null) {
            return Mono.just(responseData);
        } else if (contentType.equalsIgnoreCase("application/octet-stream")) {
            return response.bodyAsByteArray().switchIfEmpty(Mono.just(new byte[0])).map(bytes -> {
                if (bytes.length == 0) {
                    return responseData;
                }

                responseData.put(BODY, Arrays.toString(bytes));
                return responseData;
            });
        } else if (contentType.contains("json") || response.headerValue(CONTENT_ENCODING) == null) {
            return response.bodyAsString(StandardCharsets.UTF_8).switchIfEmpty(Mono.just("")).map(content -> {
                responseData.put(BODY, redactUserDelegationKey(content));
                return responseData;
            });
        } else {
            return response.bodyAsByteArray().switchIfEmpty(Mono.just(new byte[0])).map(bytes -> {
                if (bytes.length == 0) {
                    return responseData;
                }

                String content;
                if ("gzip".equalsIgnoreCase(response.headerValue(CONTENT_ENCODING))) {
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
                        throw logger.logExceptionAsWarning(Exceptions.propagate(e));
                    }
                } else {
                    content = new String(bytes, StandardCharsets.UTF_8);
                }

                responseData.remove(CONTENT_ENCODING);
                responseData.put("Content-Length", Integer.toString(content.length()));

                responseData.put(BODY, content);
                return responseData;
            });
        }
    }

    private String redactUserDelegationKey(String content) {
        if (!content.contains("UserDelegationKey")) {
            return content;
        }

        content = redactionReplacement(content, DELEGATIONKEY_KEY_PATTERN.matcher(content), Base64.getEncoder().encodeToString("REDACTED".getBytes(StandardCharsets.UTF_8)));
        content = redactionReplacement(content, DELEGATIONKEY_CLIENTID_PATTERN.matcher(content), UUID.randomUUID().toString());
        content = redactionReplacement(content, DELEGATIONKEY_TENANTID_PATTERN.matcher(content), UUID.randomUUID().toString());

        return content;
    }

    private String redactionReplacement(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            content = content.replace(matcher.group(1), replacement);
        }

        return content;
    }

    private Throwable constructExceptionType(final NetworkCallRecord networkCallRecord) {
        try {
            if (networkCallRecord.exception() != null) {
                if (networkCallRecord.exception().argTypes() == null) {
                    return (Exception) networkCallRecord.exception().className().getConstructor().newInstance();
                }
                return (Exception) networkCallRecord.exception().className().getConstructor(
                    networkCallRecord.exception().argTypes()).newInstance(networkCallRecord.exception().argValues());
            }
        } catch (Exception e) {
            logger.logExceptionAsError(new RuntimeException("Error construct the exception class. Details: "
                + e.getMessage()));
            return null;
        }
        return null;
    }
}
