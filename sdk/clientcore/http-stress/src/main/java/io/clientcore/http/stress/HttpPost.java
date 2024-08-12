// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.stress;

import com.azure.perf.test.core.PerfStressOptions;
import io.clientcore.core.http.client.DefaultHttpClientBuilder;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpLoggingPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.http.jdk.httpclient.JdkHttpClientProvider;
import io.clientcore.http.okhttp3.OkHttpHttpClientProvider;
import io.clientcore.http.stress.util.TelemetryHelper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import reactor.core.publisher.Mono;

/**
 * Performance test for simple HTTP GET against test server.
 */
public class HttpPost extends ScenarioBase<StressOptions> {
    // there will be multiple instances of scenario
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(HttpPost.class);
    private static final ClientLogger LOGGER = new ClientLogger(HttpPost.class);
    private final HttpPipeline pipeline;
    private final URL url;

    // This is almost-unique-id generator. We could use UUID, but it's a bit more expensive to use.
    private final AtomicLong clientRequestId = new AtomicLong(Instant.now().getEpochSecond());

    /**
     * Creates an instance of performance test.
     *
     * @param options stress test options
     */
    public HttpPost(StressOptions options) {
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
        return TELEMETRY_HELPER.instrumentRunAsync(runInternalAsync());
    }

    private Mono<Void> runInternalAsync() {
        return Mono.fromCallable(() -> pipeline.send(createRequest()))
            .doOnNext(response -> {
                try {
                    System.out.println("Response status code: " + response.getStatusCode());
                } finally {
                    try {
                        response.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            })
            .then();
    }

    private HttpRequest createRequest() {
        String body = "{\"id\": \"1\", \"name\": \"test\"}";
        HttpRequest request = new HttpRequest(HttpMethod.POST, url).setBody(BinaryData.fromString(body));
        request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(body.length()));
        request.getHeaders().set(HttpHeaderName.USER_AGENT, "azsdk-java-clientcore-stress");
        request.getHeaders()
            .set(HttpHeaderName.fromString("x-client-id"), String.valueOf(clientRequestId.incrementAndGet()));
        request.getHeaders().set(HttpHeaderName.fromString("Content-Type"), "application/json");
        return request;
    }

    private HttpPipelineBuilder getPipelineBuilder() {
        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS);

        HttpPipelineBuilder builder = new HttpPipelineBuilder()
            .policies(new HttpRetryPolicy(), new HttpLoggingPolicy(logOptions));

        if (options.getHttpClient() == PerfStressOptions.HttpClientType.OKHTTP) {
            builder.httpClient(new OkHttpHttpClientProvider().getSharedInstance());
        } else if (options.getHttpClient() == PerfStressOptions.HttpClientType.JDK) {
            builder.httpClient(new JdkHttpClientProvider().getSharedInstance());
        } else {
            builder.httpClient(new DefaultHttpClientBuilder().build());
        }
        return builder;
    }
}
