package com.azure.common.http;

import com.azure.common.InterceptorManager;
import com.azure.common.models.NetworkCallRecord;
import com.azure.common.models.RecordedData;
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
    private final Logger logger = LoggerFactory.getLogger(InterceptorManager.class);
    private final AtomicInteger count = new AtomicInteger(0);
    private final Map<String, String> textReplacementRules;
    private final RecordedData recordedData;

    public PlaybackClient(RecordedData recordedData, Map<String, String> textReplacementRules) {
        Objects.requireNonNull(recordedData);

        this.recordedData = recordedData;
        this.textReplacementRules = textReplacementRules == null
            ? new HashMap<>()
            : textReplacementRules;
    }

    @Override
    public Mono<HttpResponse> send(final HttpRequest request) {
        return Mono.defer(() -> playbackHttpResponse(request));
    }

    @Override
    public HttpClient proxy(Supplier<ProxyOptions> supplier) {
        return this;
    }

    @Override
    public HttpClient wiretap(boolean b) {
        return this;
    }

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
            logger.warn("NOT FOUND - Method: {} URL: {}", incomingMethod, incomingUrl);
            logger.warn("Records requested: {}.", count);

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
                headers.set(pair.getKey(), rawHeader);
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
            headers.set("Content-Length", String.valueOf(bytes.length));
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
