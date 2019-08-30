// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.util.logging.ClientLogger;
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
    private final ClientLogger logger = new ClientLogger(PlaybackClient.class);
    private final AtomicInteger count = new AtomicInteger(0);
    private final Map<String, String> textReplacementRules;
    private final RecordedData recordedData;

    /**
     * Creates a PlaybackClient that replays network calls from {@code recordedData} and replaces {@link
     * NetworkCallRecord#response() response text} for any rules specified in {@code textReplacementRules}.
     *
     * @param recordedData The data to playback.
     * @param textReplacementRules A set of rules to replace text in network call responses.
     */
    public PlaybackClient(RecordedData recordedData, Map<String, String> textReplacementRules) {
        Objects.requireNonNull(recordedData);

        this.recordedData = recordedData;
        this.textReplacementRules = textReplacementRules == null ? new HashMap<>() : textReplacementRules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> send(final HttpRequest request) {
        return Mono.defer(() -> {
            try {
                return playbackHttpResponse(request);
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<HttpResponse> playbackHttpResponse(final HttpRequest request) throws Exception {
        final String incomingUrl = applyReplacementRule(request.url().toString());
        final String incomingMethod = request.httpMethod().toString();

        final String matchingUrl = removeHost(incomingUrl);

        NetworkCallRecord networkCallRecord = recordedData.findFirstAndRemoveNetworkCall(record ->
            record.method().equalsIgnoreCase(incomingMethod) && removeHost(record.uri()).equalsIgnoreCase(matchingUrl));

        count.incrementAndGet();

        if (networkCallRecord == null) {
            logger.warning("NOT FOUND - Method: {} URL: {}", incomingMethod, incomingUrl);
            logger.warning("Records requested: {}.", count);

            return Mono.error(new IllegalStateException("==> Unexpected request: " + incomingMethod + " " + incomingUrl));
        }

        // Deserialize the exception class when the exception is not null.
        if (networkCallRecord.exception() != null) {
            deserializeException(networkCallRecord);
        }

        int recordStatusCode = Integer.parseInt(networkCallRecord.response().get("StatusCode"));
        HttpHeaders headers = new HttpHeaders();

        for (Map.Entry<String, String> pair : networkCallRecord.response().entrySet()) {
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

        String rawBody = networkCallRecord.response().get("Body");
        byte[] bytes = null;

        if (rawBody != null) {
            for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                if (rule.getValue() != null) {
                    rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                }
            }

            String contentType = networkCallRecord.response().get("Content-Type");

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

        if (urlBuilder.query().containsKey("sig")) {
            urlBuilder.setQueryParameter("sig", "REDACTED");
        }

        return String.format("%s%s", urlBuilder.path(), urlBuilder.queryString());
    }

    private void deserializeException(final NetworkCallRecord networkCallRecord) throws Exception {
        if (networkCallRecord.exception().argTypes() == null) {
            throw (Exception) networkCallRecord.exception().className().getConstructor().newInstance();
        }
        throw (Exception) networkCallRecord.exception().className().getConstructor(
            networkCallRecord.exception().argTypes()).newInstance(networkCallRecord.exception().argValues());
    }
}
