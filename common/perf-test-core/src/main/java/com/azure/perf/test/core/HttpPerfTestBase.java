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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Base Performance Test class for Http based Perf Tests.
 * @param <TOptions> the performance test options to use while running the test.
 */
public abstract class HttpPerfTestBase<TOptions extends PerfStressOptions> extends PerfTestBase<TOptions> {
    private final reactor.netty.http.client.HttpClient recordPlaybackHttpClient;
    private final URI testProxy;
    private final TestProxyPolicy testProxyPolicy;
    private String recordingId;

    protected final TOptions options;

    // Derived classes should use the ConfigureClientBuilder() method by default.  If a ClientBuilder does not
    // follow the standard convention, it can be configured manually using these fields.
    protected final HttpClient httpClient;
    protected final Iterable<HttpPipelinePolicy> policies;

    private static final AtomicInteger GLOBAL_PARALLEL_INDEX = new AtomicInteger();
    protected final int parallelIndex;

    /**
     * Creates an instance of the Http Based Performance test.
     * @param options the performance test options to use while running the test.
     */
    public HttpPerfTestBase(TOptions options) {
        super(options);
        this.options = options;
        this.parallelIndex = GLOBAL_PARALLEL_INDEX.getAndIncrement();

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
                completedOperations +=v;
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
    @Override
    public Mono<Void> stopPlaybackAsync() {
        return recordPlaybackHttpClient
            .headers(h -> {
                h.set("x-recording-id", recordingId);
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
        return recordPlaybackHttpClient
            .post()
            .uri(testProxy.resolve("/record/start"))
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
            .uri(testProxy.resolve("/record/stop"))
            .response()
            .then();
    }

    private Mono<Void> startPlaybackAsync() {
        return recordPlaybackHttpClient
            .headers(h -> h.set("x-recording-id", recordingId))
            .post()
            .uri(testProxy.resolve("/playback/start"))
            .response()
            .doOnNext(response -> {
                recordingId = response.responseHeaders().get("x-recording-id");
            })
            .then();
    }


    /**
     * Records responses and starts tests in playback mode.
     */
    @Override
    public void postSetup() {
        if (testProxyPolicy != null) {

            // Make one call to Run() before starting recording, to avoid capturing one-time setup like authorization requests.
            runSyncOrAsync();

            startRecordingAsync().block();

            testProxyPolicy.setRecordingId(recordingId);
            testProxyPolicy.setMode("record");

            runSyncOrAsync();
            stopRecordingAsync().block();
            startPlaybackAsync().block();

            testProxyPolicy.setRecordingId(recordingId);
            testProxyPolicy.setMode("playback");
        }
    }

    private void runSyncOrAsync() {
        if (options.isSync()) {
            runTest();
        } else {
            runTestAsync().block();
        }
    }
}
