// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.template.stress;


import com.azure.perf.test.core.PerfStressOptions;
import com.azure.sdk.template.stress.util.TelemetryHelper;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpLoggingPolicy;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.http.okhttp3.OkHttpHttpClientProvider;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance test for simple HTTP GET against test server.
 */
public class HttpGet extends ScenarioBase<StressOptions> {
    // there will be multiple instances of scenario
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(HttpGet.class);
    private static final ClientLogger LOGGER = new ClientLogger(HttpGet.class);
    private final HttpPipeline pipeline;
    private final URL url;

    // This is almost-unique-id generator. We could use UUID, but it's a bit more expensive to use.
    private final AtomicLong clientRequestId = new AtomicLong(Instant.now().getEpochSecond());

    /**
     * Creates an instance of performance test.
     * @param options stress test options
     */
    public HttpGet(StressOptions options) {
        super(options, TELEMETRY_HELPER);
        pipeline = getPipelineBuilder().build();
        try {
            url = new URL(options.getServiceEndpoint());
        } catch (MalformedURLException ex) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'url' must be a valid URL.", ex));
        }
    }

    @Override
    public void run() {
        TELEMETRY_HELPER.instrumentRun(this::runInternal);
    }

    private void runInternal() {
        // no need to handle exceptions here, they will be handled (and recorded) by the telemetry helper
        HttpRequest request = createRequest();
        try (Response<?> response = pipeline.send(request)) {
            response.getBody().toBytes();
        } catch (IOException e) {
            new UncheckedIOException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.error(new UnsupportedOperationException("Not implemented"));
    }

    private HttpRequest createRequest() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.getHeaders().set(HttpHeaderName.USER_AGENT, "azsdk-java-stress");
        request.getHeaders().set(HttpHeaderName.fromString("x-client-id"), String.valueOf(clientRequestId.incrementAndGet()));
        return request;
    }

    private HttpPipelineBuilder getPipelineBuilder() {
        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS);

        ArrayList<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new HttpRetryPolicy());
        policies.add(new HttpLoggingPolicy(logOptions));

        HttpPipelineBuilder builder = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]));
        if (options.getHttpClient() == PerfStressOptions.HttpClientType.OKHTTP) {
            builder.httpClient(new OkHttpHttpClientProvider().getNewInstance());
        }
        return builder;
    }
}
