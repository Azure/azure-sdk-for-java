// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * HTTP client that plays back {@link NetworkCallRecord NetworkCallRecords}.
 */
public final class PlaybackClient implements HttpClient {
    private final Logger logger = LoggerFactory.getLogger(PlaybackClient.class);
    private final AtomicInteger count = new AtomicInteger(0);
    private final Map<String, String> textReplacementRules;
    private final RecordedData recordedData;

    /**
     * Creates a PlaybackClient that replays network calls from {@code recordedData} and replaces
     * {@link NetworkCallRecord#response() response text} for any rules specified in {@code textReplacementRules}.
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
        return Mono.defer(() -> playbackHttpResponse(request));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpClient proxy(Supplier<ProxyOptions> supplier) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpClient wiretap(boolean b) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpClient port(int i) {
        return this;
    }

    private Mono<HttpResponse> playbackHttpResponse(final HttpRequest request) {
        final String incomingUrl = applyReplacementRule(request.url().toString());
        final String incomingMethod = request.httpMethod().toString();

        final String matchingUrl = removeHost(incomingUrl);

        NetworkCallRecord networkCallRecord = recordedData.findFirstAndRemoveNetworkCall(record ->
            record.method().equalsIgnoreCase(incomingMethod) && removeHost(record.uri()).equalsIgnoreCase(matchingUrl));

        count.incrementAndGet();

        if (networkCallRecord == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("NOT FOUND - Method: {} URL: {}", incomingMethod, incomingUrl);
                logger.warn("Records requested: {}.", count);
            }

            return Mono.error(new IllegalStateException("==> Unexpected request: " + incomingMethod + " " + incomingUrl));
        }

        int recordStatusCode = Integer.parseInt(networkCallRecord.response().get("StatusCode"));
        HttpHeaders headers = new HttpHeaders();

        for (Map.Entry<String, String> pair : networkCallRecord.response().entrySet()) {
            if (!pair.getKey().equals("StatusCode") && !pair.getKey().equals("Body") && !pair.getKey().equals("Content-Length")) {
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
        byte[] bytes = new byte[0];

        if (rawBody != null) {
            for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                if (rule.getValue() != null) {
                    rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                }
            }

            bytes = rawBody.getBytes(StandardCharsets.UTF_8);
            headers.put("Content-Length", String.valueOf(bytes.length));
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
        URI uri = URI.create(url);
        return String.format("%s?%s", uri.getPath(), uri.getQuery());
    }
}
