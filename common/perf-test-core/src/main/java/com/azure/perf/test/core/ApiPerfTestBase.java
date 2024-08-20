// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.vertx.VertxAsyncHttpClientBuilder;
import com.azure.core.http.vertx.VertxAsyncHttpClientProvider;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import okhttp3.OkHttpClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;

import static com.azure.perf.test.core.PerfStressOptions.HttpClientType.JDK;
import static com.azure.perf.test.core.PerfStressOptions.HttpClientType.NETTY;
import static com.azure.perf.test.core.PerfStressOptions.HttpClientType.OKHTTP;
import static com.azure.perf.test.core.PerfStressOptions.HttpClientType.VERTX;

/**
 * The Base Performance Test class for API based Perf Tests.
 *
 * @param <TOptions> the performance test options to use while running the test.
 */
public abstract class ApiPerfTestBase<TOptions extends PerfStressOptions> extends PerfTestBase<TOptions> {
    private final reactor.netty.http.client.HttpClient recordPlaybackHttpClient;
    private final URI testProxy;
    private final TestProxyPolicy testProxyPolicy;
    private String recordingId;
    private long completedOperations;

    // Derived classes should use the configureClientBuilder() method by default.  If a ClientBuilder does not
    // follow the standard convention, it can be configured manually using these fields.
    protected HttpClient httpClient;
    protected final Iterable<HttpPipelinePolicy> policies;

    /**
     * Creates an instance of the Http Based Performance test.
     *
     * @param options the performance test options to use while running the test.
     * @throws IllegalStateException if an errors is encountered with building ssl context.
     */
    public ApiPerfTestBase(TOptions options) {
        super(options);

        httpClient = createHttpClient(options);
        if (options.getTestProxies() != null && !options.getTestProxies().isEmpty()) {
            recordPlaybackHttpClient = createRecordPlaybackClient(options);
            testProxy = options.getTestProxies().get(parallelIndex % options.getTestProxies().size());
            testProxyPolicy = new TestProxyPolicy(testProxy);
            policies = Collections.singletonList(testProxyPolicy);
        } else {
            recordPlaybackHttpClient = null;
            testProxy = null;
            testProxyPolicy = null;
            policies = null;
        }
    }

    private static HttpClient createHttpClient(PerfStressOptions options) {
        PerfStressOptions.HttpClientType httpClientType = options.getHttpClient();
        Class<? extends HttpClientProvider> httpClientProvider = null;
        if (httpClientType.equals(NETTY)) {
            if (options.isInsecure()) {
                try {
                    SslContext sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();

                    reactor.netty.http.client.HttpClient nettyHttpClient =
                        reactor.netty.http.client.HttpClient.create()
                            .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

                    return new NettyAsyncHttpClientBuilder(nettyHttpClient).build();
                } catch (SSLException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                httpClientProvider = NettyAsyncHttpClientProvider.class;
            }
        } else if (httpClientType.equals(OKHTTP)) {
            if (options.isInsecure()) {
                try {
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(
                        null, InsecureTrustManagerFactory.INSTANCE.getTrustManagers(), new SecureRandom());
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.getSocketFactory(),
                            (X509TrustManager) InsecureTrustManagerFactory.INSTANCE.getTrustManagers()[0])
                        .build();
                    return new OkHttpAsyncHttpClientBuilder(okHttpClient).build();
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                httpClientProvider = OkHttpAsyncClientProvider.class;
            }
        } else if (httpClientType.equals(JDK)) {
            if (options.isInsecure()) {
                // can't configure JDK HttpClient for insecure mode with source set to Java 8
                throw new UnsupportedOperationException("Can't configure JDK HttpClient for insecure mode.");
            } else {
                // we want to support friendly name for jdk, but can't use JdkHttpClientProvider on Java 8
                httpClientType = PerfStressOptions.HttpClientType.fromString("com.azure.core.http.jdk.httpclient.JdkHttpClientProvider");
            }
        } else if (httpClientType.equals(VERTX)) {
            if (options.isInsecure()) {
                io.vertx.core.http.HttpClientOptions vertxOptions = new io.vertx.core.http.HttpClientOptions()
                    .setSsl(true)
                    .setTrustAll(true);
                return new VertxAsyncHttpClientBuilder().httpClientOptions(vertxOptions).build();
            } else {
                httpClientProvider = VertxAsyncHttpClientProvider.class;
            }
        }

        if (httpClientProvider == null) {
            httpClientProvider = getHttpClientProvider(httpClientType);
        }

        try {
            return httpClientProvider.getDeclaredConstructor().newInstance().createInstance();
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not create HttpClient from given provider: " + httpClientType, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends HttpClientProvider> getHttpClientProvider(PerfStressOptions.HttpClientType httpClientType) {
        String providerClassName = httpClientType.toString();
        try {
            Class<?> provider = Class.forName(providerClassName, false, ApiPerfTestBase.class.getClassLoader());
            if (HttpClientProvider.class.isAssignableFrom(provider)) {
                return (Class<? extends HttpClientProvider>) provider;
            } else {
                throw new IllegalArgumentException("Http client type does not match HttpClientProvider implementation: " + providerClassName);
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Http client provider type is not found: " + providerClassName, e);
        }
    }

    private static reactor.netty.http.client.HttpClient createRecordPlaybackClient(PerfStressOptions options) {
        if (options.isInsecure()) {
            try {
                SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
                return reactor.netty.http.client.HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            } catch (SSLException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return reactor.netty.http.client.HttpClient.create();
        }
    }

    /**
     * Attempts to configure a ClientBuilder using reflection.  If a ClientBuilder does not follow the standard
     * convention, it can be configured manually using the "httpClient" and "policies" fields.
     *
     * @param clientBuilder The client builder.
     * @throws IllegalStateException If reflective access to get httpClient or addPolicy methods fail.
     */
    protected void configureClientBuilder(HttpTrait<?> clientBuilder) {
        if (httpClient != null) {
            clientBuilder.httpClient(httpClient);
        }

        if (policies != null) {
            for (HttpPipelinePolicy policy : policies) {
                clientBuilder.addPolicy(policy);
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

        return Flux.generate(sink -> {
                if (System.nanoTime() < endNanoTime) {
                    sink.next(1);
                } else {
                    sink.complete();
                }
            })
            .flatMap(ignored -> runTestAsync(), 1)
            .doOnNext(result -> {
                completedOperations += result;
                lastCompletionNanoTime = System.nanoTime() - startNanoTime;
            })
            .then();
    }

    /**
     * Indicates how many operations were completed in a single run of the test. Good to be used for batch operations.
     *
     * @return the number of successful operations completed.
     */
    abstract int runTest();

    /**
     * Indicates how many operations were completed in a single run of the async test. Good to be used for batch
     * operations.
     *
     * @return the number of successful operations completed.
     */
    abstract Mono<Integer> runTestAsync();

    /**
     * Stops playback tests.
     *
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

    /**
     * Stops playback tests.
     */
    public void stopPlayback() {
        recordPlaybackHttpClient
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
            }).block();
    }

    private Mono<Void> startRecordingAsync() {
        return Mono.defer(() -> recordPlaybackHttpClient
            .post()
            .uri(testProxy.resolve("/record/start"))
            .response()
            .doOnNext(response -> recordingId = response.responseHeaders().get("x-recording-id")).then());
    }

    private void startRecording() {
        recordPlaybackHttpClient.post()
            .uri(testProxy.resolve("/record/start"))
            .response()
            .doOnNext(response -> recordingId = response.responseHeaders().get("x-recording-id")).block();
    }

    private Mono<Void> stopRecordingAsync() {
        return Mono.defer(() -> recordPlaybackHttpClient
            .headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(testProxy.resolve("/record/stop"))
            .response()
            .then());
    }

    private void stopRecording() {
        recordPlaybackHttpClient.headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(testProxy.resolve("/record/stop"))
            .response()
            .block();
    }

    private Mono<Void> startPlaybackAsync() {
        return Mono.defer(() -> recordPlaybackHttpClient
            .headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(testProxy.resolve("/playback/start"))
            .response()
            .doOnNext(response -> recordingId = response.responseHeaders().get("x-recording-id")).then());
    }

    private void startPlayback() {
        recordPlaybackHttpClient
            .headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(testProxy.resolve("/playback/start"))
            .response()
            .doOnNext(response -> recordingId = response.responseHeaders().get("x-recording-id")).block();
    }

    /**
     * Records responses and starts tests in playback mode.
     *
     * @return An empty {@link Mono}.
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

    @Override
    void postSetup() {
        if (testProxyPolicy != null) {
            // Make one call to Run() before starting recording, to avoid capturing one-time setup like authorization
            // requests.
            runSync();
            startRecording();
            testProxyPolicy.setRecordingId(recordingId);
            testProxyPolicy.setMode("record");
            runSync();
            stopRecording();
            startPlayback();
            testProxyPolicy.setRecordingId(recordingId);
            testProxyPolicy.setMode("playback");
        }
    }

    private Mono<Void> runSyncOrAsync() {
        return Mono.defer(() -> runTestAsync().then());
    }

    private void runSync() {
        runTest();
    }

    @Override
    public long getCompletedOperations() {
        return completedOperations;
    }
}
