// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.util.ArrayList;
import javax.net.ssl.SSLException;
import reactor.core.publisher.Mono;

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
    protected final TOptions options;

    protected final HttpClient httpClient;
    protected final Iterable<HttpPipelinePolicy> policies;

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     */
    public PerfStressTest(TOptions options) {
        this.options = options;

        if (options.isInsecure()) {
            SslContext sslContext;
            try {
                sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            }
            catch (SSLException e) {
                throw new RuntimeException(e);
            }

            reactor.netty.http.client.HttpClient nettyHttpClient = reactor.netty.http.client.HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            
            httpClient = new NettyAsyncHttpClientBuilder(nettyHttpClient).build();
        }
        else {
            httpClient = null;
        }

        if (options.getTestProxy() != null) {
            policies = new ArrayList<HttpPipelinePolicy>();
        }
        else {
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

    /**
     * Runs the performance test.
     */
    public abstract void run();

    /**
     * Runs the performance test asynchronously.
     * @return An empty {@link Mono}
     */
    public abstract Mono<Void> runAsync();

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
}
