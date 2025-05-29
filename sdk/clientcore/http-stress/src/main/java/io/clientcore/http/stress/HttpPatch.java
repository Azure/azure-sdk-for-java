// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.stress;

import com.azure.perf.test.core.PerfStressOptions;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.http.okhttp3.OkHttpHttpClientProvider;
import io.clientcore.http.stress.util.TelemetryHelper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance test for simple HTTP GET against test server.
 */
public class HttpPatch extends ScenarioBase<StressOptions> {
    // there will be multiple instances of scenario
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(HttpPatch.class);
    private static final ClientLogger LOGGER = new ClientLogger(HttpPatch.class);
    private final HttpPipeline pipeline;
    private final URI uri;

    // This is almost-unique-id generator. We could use UUID, but it's a bit more expensive to use.
    private final AtomicLong clientRequestId = new AtomicLong(Instant.now().getEpochSecond());

    /**
     * Creates an instance of performance test.
     * @param options stress test options
     */
    public HttpPatch(StressOptions options) {
        super(options, TELEMETRY_HELPER);
        pipeline = getPipelineBuilder().build();
        try {
            uri = new URI(options.getServiceEndpoint());
        } catch (URISyntaxException ex) {
            throw LOGGER.throwableAtError().log("'uri' must be a valid URI.", ex, IllegalArgumentException::new);
        }
    }

    @Override
    public void run() {
        TELEMETRY_HELPER.instrumentRun(this::runInternal);
    }

    private void runInternal() {
        // no need to handle exceptions here, they will be handled (and recorded) by the telemetry helper
        try (Response<BinaryData> response = pipeline.send(createRequest())) {
            int responseCode = response.getStatusCode();
            assert responseCode == 200 : "Unexpected response code: " + responseCode;
            response.getValue().close();
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.error(new UnsupportedOperationException("Not implemented"));
    }

    private HttpRequest createRequest() {
        String body = "{\"id\": \"1\", \"name\": \"test\"}";
        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.PATCH).setUri(uri).setBody(BinaryData.fromString(body));
        request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(body.length()));
        request.getHeaders().set(HttpHeaderName.USER_AGENT, "clientcore-stress");
        request.getHeaders()
            .set(HttpHeaderName.fromString("x-client-id"), String.valueOf(clientRequestId.incrementAndGet()));
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        request.getHeaders().set(HttpHeaderName.ACCEPT, "application/json");
        return request;
    }

    private HttpPipelineBuilder getPipelineBuilder() {
        HttpPipelineBuilder builder = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy())
            .addPolicy(new HttpInstrumentationPolicy(
                new HttpInstrumentationOptions().setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS)));

        if (options.getHttpClient() == PerfStressOptions.HttpClientType.OKHTTP) {
            builder.httpClient(new OkHttpHttpClientProvider().getSharedInstance());
        } else {
            builder.httpClient(new JdkHttpClientBuilder().build());
        }
        return builder;
    }
}
