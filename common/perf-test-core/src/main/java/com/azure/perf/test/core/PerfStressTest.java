// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;

import java.util.Arrays;
import java.util.ArrayList;
import javax.net.ssl.SSLException;

/**
 * Represents the abstraction of a Performance test class.
 *
 * <p>
 *     The performance test class needs to extend this class. The test class should override {@link PerfStressTest#run()}
 *     and {@link PerfStressTest#runAsync()} methods and the synchronous and asynchronous test logic respectively.
 *     To add any test setup and logic the test class should override {@link PerfStressTest#globalSetupAsync()}
 *     and {@link PerfStressTest#globalCleanupAsync()} methods .
 * </p>
 *
 *
 * @param <TOptions> the options configured for the test.
 */
public abstract class PerfStressTest<TOptions extends PerfStressOptions> {
    private final reactor.netty.http.client.HttpClient recordPlaybackHttpClient;

    protected final TOptions options;
    protected final HttpClient httpClient;
    protected final Iterable<HttpPipelinePolicy> policies;

    private final TestProxyPolicy testProxyPolicy;

    private String recordingId;

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     */
    public PerfStressTest(TOptions options) {
        this.options = options;

        final SslContext sslContext;

        if (options.isInsecure()) {
            try {
                sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            }
            catch (SSLException e) {
                throw new IllegalStateException(e);
            }

            reactor.netty.http.client.HttpClient nettyHttpClient = reactor.netty.http.client.HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            
            httpClient = new NettyAsyncHttpClientBuilder(nettyHttpClient).build();
        }
        else {
            sslContext = null;
            httpClient = null;
        }

        if (options.getTestProxy() != null) {
            if (options.isInsecure()) {
                recordPlaybackHttpClient = reactor.netty.http.client.HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            }
            else {
                recordPlaybackHttpClient = reactor.netty.http.client.HttpClient.create();
            }

            testProxyPolicy = new TestProxyPolicy(options.getTestProxy());
            policies = Arrays.asList(testProxyPolicy);
        }
        else {
            recordPlaybackHttpClient = null;
            testProxyPolicy = null;
            policies = null;
        }
    }

    /**
     * Runs the setup required prior to running the performance test.
     * @return An empty {@link Mono}
     */
    public Mono<Void> globalSetupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the setup required prior to running an individual thread in the performance test.
     * @return An empty {@link Mono}
     */
    public Mono<Void> setupAsync() {
        return Mono.empty();
    }

    public Mono<Void> recordAndStartPlaybackAsync() {
        return startRecordingAsync()
            .doOnSuccess(x -> {
                testProxyPolicy.setRecordingId(recordingId);
                testProxyPolicy.setMode("record");
            })
            .then(runSyncOrAsync())
            .then(stopRecordingAsync())
            .then(startPlaybackAsync())
            .doOnSuccess(x -> {
                testProxyPolicy.setRecordingId(recordingId);
                testProxyPolicy.setMode("playback");
            });
    }

    private Mono<Void> runSyncOrAsync() {
        if (options.isSync()) {
            return Mono.empty().then().doOnSuccess(x -> run());
        }
        else {
            return runAsync();
        }
    }

    /**
     * Runs the performance test.
     */
    public abstract void run();

    /**
     * Runs the performance test asynchronously.
     * @return An empty {@link Mono}
     */
    public abstract Mono<Void> runAsync();

    public Mono<Void> stopPlaybackAsync() {
        return recordPlaybackHttpClient
            .headers(h -> {
                h.set("x-recording-id", recordingId);
                h.set("x-purge-inmemory-recording", Boolean.toString(true));
            })
            .post()
            .uri(options.getTestProxy().resolve("/playback/stop"))
            .response()
            .doOnSuccess(response -> {
                testProxyPolicy.setMode(null);
                testProxyPolicy.setRecordingId(null);
            })
            .then();
    }

    /**
     * Runs the cleanup logic after an individual thread finishes in the performance test.
     * @return An empty {@link Mono}
     */
    public Mono<Void> cleanupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the cleanup logic after the performance test finishes.
     * @return An empty {@link Mono}
     */
    public Mono<Void> globalCleanupAsync() {
        return Mono.empty();
    }

    private Mono<Void> startRecordingAsync() {
        return recordPlaybackHttpClient
            .post()
            .uri(options.getTestProxy().resolve("/record/start"))
            .response()
            .doOnNext(response -> {
                recordingId = response.responseHeaders().get("x-recording-id");
            })
            .then();
    }

    private Mono<Void> stopRecordingAsync() {
        return recordPlaybackHttpClient
            .headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(options.getTestProxy().resolve("/record/stop"))
            .response()
            .then();
    }

    private Mono<Void> startPlaybackAsync() {
        return recordPlaybackHttpClient
            .headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(options.getTestProxy().resolve("/playback/start"))
            .response()
            .doOnNext(response -> {
                recordingId = response.responseHeaders().get("x-recording-id");
            })
            .then();
    }
}
