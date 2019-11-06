// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.UrlBuilder;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HTTP client that plays back {@link NetworkCallRecord NetworkCallRecords}.
 */
public final class PlaybackClient implements HttpClient {
    private static final String X_MS_CLIENT_REQUEST_ID = "x-ms-client-request-id";
    private static final String X_MS_ENCRYPTION_KEY_SHA256 = "x-ms-encryption-key-sha256";
    private final ClientLogger logger = new ClientLogger(PlaybackClient.class);
    private final AtomicInteger count = new AtomicInteger(0);
    private final Map<String, String> textReplacementRules;
    private final RecordedData recordedData;

    /**
     * Creates a PlaybackClient that replays network calls from {@code recordedData} and replaces {@link
     * NetworkCallRecord#getResponse() response text} for any rules specified in {@code textReplacementRules}.
     *
     * @param recordedData The data to playback.
     * @param textReplacementRules A set of rules to replace text in network call responses.
     */
    public PlaybackClient(RecordedData recordedData, Map<String, String> textReplacementRules) {
        Objects.requireNonNull(recordedData, "'recordedData' cannot be null.");

        this.recordedData = recordedData;
        this.textReplacementRules = textReplacementRules == null ? new HashMap<>() : textReplacementRules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> send(final HttpRequest request) {
        return Mono.defer(() -> playbackHttpResponse(request));
    }

    private Mono<HttpResponse> playbackHttpResponse(final HttpRequest request) {
        final String incomingUrl = applyReplacementRule(request.getUrl().toString());
        final String incomingMethod = request.getHttpMethod().toString();

        final String matchingUrl = removeHost(incomingUrl);

        NetworkCallRecord networkCallRecord = recordedData.findFirstAndRemoveNetworkCall(record ->
            record.getMethod().equalsIgnoreCase(incomingMethod) && removeHost(record.getUri()).equalsIgnoreCase(matchingUrl));

        count.incrementAndGet();

        if (networkCallRecord == null) {
            logger.warning("NOT FOUND - Method: {} URL: {}", incomingMethod, incomingUrl);
            logger.warning("Records requested: {}.", count);

            return Mono.error(new IllegalStateException("==> Unexpected request: " + incomingMethod + " " + incomingUrl));
        }

        if (networkCallRecord.getException() != null) {
            throw logger.logExceptionAsWarning(Exceptions.propagate(networkCallRecord.getException().get()));
        }

        // Overwrite the request header if any.
        if (networkCallRecord.getHeaders().containsKey(X_MS_CLIENT_REQUEST_ID)) {
            request.setHeader(X_MS_CLIENT_REQUEST_ID, networkCallRecord.getHeaders().get(X_MS_CLIENT_REQUEST_ID));
        }
        if (request.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256) != null) {
            networkCallRecord.getResponse().put(X_MS_ENCRYPTION_KEY_SHA256,
                request.getHeaders().getValue(X_MS_ENCRYPTION_KEY_SHA256));
        }

        int recordStatusCode = Integer.parseInt(networkCallRecord.getResponse().get("StatusCode"));
        HttpHeaders headers = new HttpHeaders();

        for (Map.Entry<String, String> pair : networkCallRecord.getResponse().entrySet()) {
            if (!pair.getKey().equals("StatusCode") && !pair.getKey().equals("Body")) {
                String rawHeader = pair.getValue();
                for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawHeader = rawHeader.replaceAll(rule.getKey(), rule.getValue());
                    }
                }
                headers.put(pair.getKey(), rawHeader);
            }
        }

        String rawBody = networkCallRecord.getResponse().get("Body");
        byte[] bytes = null;

        if (rawBody != null) {
            for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                if (rule.getValue() != null) {
                    rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                }
            }

            String contentType = networkCallRecord.getResponse().get("Content-Type");

            // octet-stream's are written to disk using Arrays.toString() which creates an output such as "[12, -1]".
            if (contentType != null && contentType.equalsIgnoreCase("application/octet-stream")) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                for (String piece : rawBody.substring(1, rawBody.length() - 1).split(", ")) {
                    outputStream.write(Byte.parseByte(piece));
                }

                bytes = outputStream.toByteArray();
            } else {
                bytes = rawBody.getBytes(StandardCharsets.UTF_8);
            }

            if (bytes.length > 0) {
                headers.put("Content-Length", String.valueOf(bytes.length));
            }
        }

        HttpResponse response = new MockHttpResponse(request, recordStatusCode, headers, bytes);
        return Mono.just(response);
    }

    private String applyReplacementRule(String text) {
        for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
            if (rule.getValue() != null) {
                text = text.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        return text;
    }

    private static String removeHost(String url) {
        UrlBuilder urlBuilder = UrlBuilder.parse(url);

        if (urlBuilder.getQuery().containsKey("sig")) {
            urlBuilder.setQueryParameter("sig", "REDACTED");
        }

        return String.format("%s%s", urlBuilder.getPath(), urlBuilder.getQueryString());
    }
}
