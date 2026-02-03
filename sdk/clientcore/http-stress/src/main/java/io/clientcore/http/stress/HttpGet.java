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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance test for simple HTTP GET against test server.
 */
public class HttpGet extends ScenarioBase<StressOptions> {
    // there will be multiple instances of scenario
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(HttpGet.class);
    private static final ClientLogger LOGGER = new ClientLogger(HttpGet.class);
    private final HttpPipeline pipeline;
    private final URI uri;
    final ExecutorService executorService = Executors.newFixedThreadPool(options.getParallel());

    // This is almost-unique-id generator. We could use UUID, but it's a bit more expensive to use.
    private final AtomicLong clientRequestId = new AtomicLong(Instant.now().getEpochSecond());

    /**
     * Creates an instance of performance test.
     *
     * @param options stress test options
     */
    public HttpGet(StressOptions options) {
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
        HttpRequest request = createRequest();
        Response<BinaryData> response = pipeline.send(request);
        response.getValue().toBytes();
    }

    @Override
    public Mono<Void> runAsync() {
        return TELEMETRY_HELPER.instrumentRunAsync(runInternalAsync());
    }

    @Override
    public CompletableFuture<Void> runAsyncWithCompletableFuture() {
        return TELEMETRY_HELPER.instrumentRunAsyncWithCompletableFuture(runAsyncWithCompletableFutureInternal());
    }

    @Override
    public Runnable runAsyncWithExecutorService() {
        return TELEMETRY_HELPER.instrumentRunAsyncWithRunnable(runAsyncWithExecutorServiceInternal());
    }

    @Override
    public Runnable runAsyncWithVirtualThread() {
        return TELEMETRY_HELPER.instrumentRunAsyncWithRunnable(runAsyncWithVirtualThreadInternal());
    }

    private Mono<Void> runInternalAsync() {
        return Mono.usingWhen(Mono.fromCallable(() -> pipeline.send(createRequest())), response -> {
            response.getValue().toBytes();
            return Mono.empty();
        }, response -> Mono.fromRunnable(response::close));
    }

    // Method to run using CompletableFuture
    private CompletableFuture<Void> runAsyncWithCompletableFutureInternal() {
        return CompletableFuture.supplyAsync(() -> {
            try (Response<BinaryData> response = pipeline.send(createRequest())) {
                response.getValue().toBytes();
            }
            return null;
        }, executorService);
    }

    // Method to run using ExecutorService
    private Runnable runAsyncWithExecutorServiceInternal() {
        return () -> {
            try (Response<BinaryData> response = pipeline.send(createRequest())) {
                response.getValue().toBytes();
            }
        };
    }

    // Method to run using Virtual Threads
    private Runnable runAsyncWithVirtualThreadInternal() {
        return () -> {
            try (Response<BinaryData> response = pipeline.send(createRequest())) {
                response.getValue().toBytes();
            }
        };
    }

    private HttpRequest createRequest() {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri);
        request.getHeaders().set(HttpHeaderName.USER_AGENT, "clientcore-stress");
        request.getHeaders()
            .set(HttpHeaderName.fromString("x-client-id"), String.valueOf(clientRequestId.incrementAndGet()));
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
