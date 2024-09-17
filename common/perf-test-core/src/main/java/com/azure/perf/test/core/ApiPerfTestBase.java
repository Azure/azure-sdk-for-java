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
import com.azure.core.util.logging.ClientLogger;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import okhttp3.OkHttpClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
    ClientLogger LOGGER = new ClientLogger(ApiPerfTestBase.class);
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

    public CompletableFuture<Void> runAllAsyncWithCompletableFuture(long endNanoTime) {
        completedOperations = 0;
        lastCompletionNanoTime = 0;
        long startNanoTime = System.nanoTime();
        Semaphore semaphore = new Semaphore(options.getParallel()); // Use configurable limit

        List<CompletableFuture<Void>> futures = new LinkedList<>();
        while (System.nanoTime() < endNanoTime) {
            try {
                semaphore.acquire();
                // Each runTestAsyncWithCompletableFuture() call runs independently
                CompletableFuture<Void> testFuture = runTestAsyncWithCompletableFuture()
                    .thenAccept(result -> {
                        completedOperations += result;
                        lastCompletionNanoTime = System.nanoTime() - startNanoTime;
                    })
                    .whenComplete((res, ex) -> semaphore.release());
                futures.add(testFuture);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        // Remove all completed CompletableFutures from the list
        futures.removeIf(CompletableFuture::isDone);
        // Combine all futures so we can wait for all to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public Runnable runAllAsyncWithExecutorService(long endNanoTime) {
        completedOperations = 0;
        lastCompletionNanoTime = 0;
        final ExecutorService executor = Executors.newFixedThreadPool(options.getParallel());

        return () -> {
            try {
                while (System.nanoTime() < endNanoTime) {
                    long startNanoTime = System.nanoTime();

                    try {
                        Runnable task = runTestAsyncWithExecutorService();
                        executor.submit(() -> {
                            task.run();
                            completedOperations++;
                            lastCompletionNanoTime = System.nanoTime() - startNanoTime;
                        }).get(); // Wait for the task to complete
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(options.getDuration(), TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    @Override
    public Runnable runAllAsyncWithVirtualThread(long endNanoTime) {
        completedOperations = 0;
        lastCompletionNanoTime = 0;

        ExecutorService virtualThreadExecutor;
        try {
            Method method = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            virtualThreadExecutor = (ExecutorService) method.invoke(null);
        } catch (Exception e) {
            // Skip virtual thread tests and report 0 completed operations rather than fallback
            return () -> {
                completedOperations = 0;
                lastCompletionNanoTime = 0;
            };
        }

        return () -> {
            while (System.nanoTime() < endNanoTime) {
                long startNanoTime = System.nanoTime();
                virtualThreadExecutor.execute(() -> {
                    try {
                        runTestAsyncWithVirtualThread();
                        completedOperations++;
                        lastCompletionNanoTime = System.nanoTime() - startNanoTime;
                    } catch (Exception e) {
                        LOGGER.logThrowableAsError(e);
                    }
                });
            }
            virtualThreadExecutor.shutdown();
        };
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
     * Indicates how many operations were completed in a single run of the async test using CompletableFuture.
     *
     * @return the number of successful operations completed.
     */
    CompletableFuture<Integer> runTestAsyncWithCompletableFuture() {
        throw new UnsupportedOperationException("runAllAsyncWithCompletableFuture is not supported.");
    }

    /**
     * Indicates how many operations were completed in a single run of the async test using ExecutorService.
     *
     * @return the number of successful operations completed.
     */
    Runnable runTestAsyncWithExecutorService() {
        throw new UnsupportedOperationException("runAllAsyncWithExecutorService is not supported.");
    }

    /**
     * Indicates how many operations were completed in a single run of the async test using Virtual Threads.
     */
    Runnable runTestAsyncWithVirtualThread() {
        throw new UnsupportedOperationException("runAllAsyncWithVirtualThread is not supported.");
    }

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
     *
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
            } else if (options.isCompletableFuture()) {
                return Mono.fromFuture(CompletableFuture.supplyAsync(() -> runTestAsyncWithCompletableFuture())).then();
            } else if (options.isExecutorService()) {
                return  Mono.fromRunnable(runTestAsyncWithExecutorService());
            } else if (options.isVirtualThread()) {
                return  Mono.fromRunnable(this::runTestAsyncWithVirtualThread);
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
