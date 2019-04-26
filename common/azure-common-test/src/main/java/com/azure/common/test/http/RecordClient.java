package com.azure.common.test.http;

import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.ProxyOptions;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.test.InterceptorManager;
import com.azure.common.test.models.NetworkCallRecord;
import com.azure.common.test.models.RecordedData;
import com.azure.common.test.utils.SdkContext;
import io.netty.handler.codec.http.HttpResponseStatus;
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
import com.azure.common.http.HttpClient;
import com.azure.common.test.models.NetworkCallRecord;

/**
 * HTTP client that create a new {@link NetworkCallRecord NetworkCallRecords}.
 */
public final class RecordClient implements HttpPipelinePolicy {
    private final Logger logger = LoggerFactory.getLogger(RecordClient.class);
    //private final AtomicInteger count = new AtomicInteger(0);
    private final Map<String, String> textReplacementRules;
    private final RecordedData recordedData;

    /**
     * Creates a RecordClient that replays network calls from {@code recordedData} and replaces
     * {@link NetworkCallRecord#response() response text} for any rules specified in {@code textReplacementRules}.
     *
     * @param recordedData The data to record.
     * @param textReplacementRules A set of rules to replace text in network call responses.
     */
    public RecordClient(RecordedData recordedData, Map<String, String> textReplacementRules) {
        Objects.requireNonNull(recordedData);

        this.recordedData = recordedData;
        this.textReplacementRules = textReplacementRules == null ? new HashMap<>() : textReplacementRules;
    }
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            final NetworkCallRecord networkCallRecord = new NetworkCallRecord();
            networkCallRecord.headers(new HashMap<>());

            if (context.httpRequest().headers().value("Content-Type") != null) {
                networkCallRecord.headers().put("Content-Type", context.httpRequest().headers().value("Content-Type"));
            }

            if (context.httpRequest().headers().value("x-ms-version") != null) {
                networkCallRecord.headers().put("x-ms-version", context.httpRequest().headers().value("x-ms-version"));
            }

            if (context.httpRequest().headers().value("User-Agent") != null) {
                networkCallRecord.headers().put("User-Agent", context.httpRequest().headers().value("User-Agent"));
            }

            networkCallRecord.method(context.httpRequest().httpMethod().toString());
            networkCallRecord.uri(SdkContext.applyReplacementRule(
                context.httpRequest().url().toString().replaceAll("\\?$", ""), textReplacementRules));

            return next.process().flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();
                return SdkContext.extractResponseData(bufferedResponse, textReplacementRules).map(responseData -> {
                    networkCallRecord.response(responseData);
                    String body = networkCallRecord.response().get("Body");

                    // Remove pre-added header if this is a waiting or redirection
                    if (body != null && body.contains("<Status>InProgress</Status>")
                            || Integer.parseInt(networkCallRecord.response().get("StatusCode")) == HttpResponseStatus.TEMPORARY_REDIRECT.code()) {
                        logger.info("Waiting for a response or redirection.");
                    } else {
                        synchronized (recordedData.getNetworkCallRecords()) {
                            recordedData.getNetworkCallRecords().add(networkCallRecord);
                        }
                    }
                    return bufferedResponse;
                });
            });
        }
}
