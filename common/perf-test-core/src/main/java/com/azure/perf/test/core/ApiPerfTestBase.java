// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * The Base Performance Test class for API based Perf Tests.
 * @param <TOptions> the performance test options to use while running the test.
 */
public abstract class ApiPerfTestBase<TOptions extends PerfStressOptions> extends PerfTestBase<TOptions> {
    private final reactor.netty.http.client.HttpClient recordPlaybackHttpClient;
    private final URI testProxy;
    private final TestProxyPolicy testProxyPolicy;
    private String recordingId;
    private long completedOperations;


    // Derived classes should use the ConfigureClientBuilder() method by default.  If a ClientBuilder does not
    // follow the standard convention, it can be configured manually using these fields.
    protected final HttpClient httpClient;
    protected final Iterable<HttpPipelinePolicy> policies;

    /**
     * Creates an instance of the Http Based Performance test.
     * @param options the performance test options to use while running the test.
     * @throws IllegalStateException if an errors is encountered with building ssl context.
     */
    public ApiPerfTestBase(TOptions options) {
        super(options);
        final SslContext sslContext;
        if (options.isInsecure()) {
            try {
                sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            } catch (SSLException e) {
                throw new IllegalStateException(e);
            }

            reactor.netty.http.client.HttpClient nettyHttpClient = reactor.netty.http.client.HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

            httpClient = new NettyAsyncHttpClientBuilder(nettyHttpClient).build();
        } else {
            sslContext = null;
            httpClient = null;
        }

        if (options.getTestProxies() != null && !options.getTestProxies().isEmpty()) {
            if (options.isInsecure()) {
                recordPlaybackHttpClient = reactor.netty.http.client.HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            } else {
                recordPlaybackHttpClient = reactor.netty.http.client.HttpClient.create();
            }

            testProxy = options.getTestProxies().get(parallelIndex % options.getTestProxies().size());
            testProxyPolicy = new TestProxyPolicy(testProxy);
            policies = Arrays.asList(testProxyPolicy);
        } else {
            recordPlaybackHttpClient = null;
            testProxy = null;
            testProxyPolicy = null;
            policies = null;
        }
    }

    /**
     * Attempts to configure a ClientBuilder using reflection.  If a ClientBuilder does not follow the standard convention,
     * it can be configured manually using the "httpClient" and "policies" fields.
     * @param clientBuilder The client builder.
     * @throws IllegalStateException If reflective access to get httpClient or addPolicy methods fail.
     */
    protected void configureClientBuilder(Object clientBuilder) {
        if (httpClient != null || policies != null) {
            Class<?> clientBuilderClass = clientBuilder.getClass();

            try {
                if (httpClient != null) {
                    Method httpClientMethod = clientBuilderClass.getMethod("httpClient", HttpClient.class);
                    httpClientMethod.invoke(clientBuilder, httpClient);
                }

                if (policies != null) {
                    Method addPolicyMethod = clientBuilderClass.getMethod("addPolicy", HttpPipelinePolicy.class);
                    for (HttpPipelinePolicy policy : policies) {
                        addPolicyMethod.invoke(clientBuilder, policy);
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void runAll(long endNanoTime) {
        completedOperations = 0;
        lastCompletionNanoTime = 0;
        long startNanoTime = System.nanoTime();
        while (System.nanoTime() < endNanoTime) {
            completedOperations += runTest();
            lastCompletionNanoTime = System.nanoTime() - startNanoTime;
        }
    }

    @Override
    public Mono<Void> runAllAsync(long endNanoTime) {
        completedOperations = 0;
        lastCompletionNanoTime = 0;
        long startNanoTime = System.nanoTime();

        return Flux.just(1)
            .repeat()
            .flatMap(i -> runTestAsync(), 1)
            .doOnNext(v -> {
                completedOperations += v;
                lastCompletionNanoTime = System.nanoTime() - startNanoTime;
            })
            .takeWhile(i -> System.nanoTime() < endNanoTime)
            .then();
    }

    /**
     * Indicates how many operations were completed in a single run of the test.
     * Good to be used for batch operations.
     *
     * @return the number of successful operations completed.
     */
    abstract int runTest();

    /**
     * Indicates how many operations were completed in a single run of the async test.
     * Good to be used for batch operations.
     *
     * @return the number of successful operations completed.
     */
    abstract Mono<Integer> runTestAsync();

    /**
     * Stops playback tests.
     * @return An empty {@link Mono}.
     */
    public Mono<Void> stopPlaybackAsync() {
        return recordPlaybackHttpClient
            .headers(h -> {
                // The Recording id to track the recording session on the Test Proxy Server.
                h.set("x-recording-id", recordingId);
                // Indicates Test Proxy Server to purge the cached recording.
                h.set("x-purge-inmemory-recording", Boolean.toString(true));
            })
            .post()
            .uri(testProxy.resolve("/playback/stop"))
            .response()
            .doOnSuccess(response -> {
                testProxyPolicy.setMode(null);
                testProxyPolicy.setRecordingId(null);
            })
            .then();
    }

    private Mono<Void> startRecordingAsync() {
        return Mono.defer(() -> recordPlaybackHttpClient
            .post()
            .uri(testProxy.resolve("/record/start"))
            .response()
            .doOnNext(response -> {
                recordingId = response.responseHeaders().get("x-recording-id");
            }).then());
    }

    private Mono<Void> stopRecordingAsync() {
        return Mono.defer(() -> recordPlaybackHttpClient
            .headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(testProxy.resolve("/record/stop"))
            .response()
            .then());
    }

    private Mono<Void> startPlaybackAsync() {
        return Mono.defer(() -> recordPlaybackHttpClient
            .headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(testProxy.resolve("/playback/start"))
            .response()
            .doOnNext(response -> {
                recordingId = response.responseHeaders().get("x-recording-id");
            }).then());
    }


    /**
     * Records responses and starts tests in playback mode.
     * @return
     */
    @Override
    Mono<Void> postSetupAsync() {
        if (testProxyPolicy != null) {

            // Make one call to Run() before starting recording, to avoid capturing one-time setup like authorization requests.
            return runSyncOrAsync()
            .then(startRecordingAsync())
            .then(Mono.defer(() -> {
                    testProxyPolicy.setRecordingId(recordingId);
                    testProxyPolicy.setMode("record");
                    return Mono.empty();
                }))
            .then(runSyncOrAsync())
            .then(stopRecordingAsync())
            .then(startPlaybackAsync())
            .then(Mono.defer(() -> {
                    testProxyPolicy.setRecordingId(recordingId);
                    testProxyPolicy.setMode("playback");
                    return Mono.empty();
                }));
        }
        return Mono.empty();
    }

    private Mono<Void> runSyncOrAsync() {
        return Mono.defer(() -> {
            if (options.isSync()) {
                return Mono.fromFuture(CompletableFuture.supplyAsync(() -> runTest())).then();
            } else {
                return runTestAsync().then();
            }
        });
    }

    @Override
    public long getCompletedOperations() {
        return completedOperations;
    }
}
