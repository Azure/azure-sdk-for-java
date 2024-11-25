// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.Http2AllocationStrategy;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * A generic interface for sending HTTP requests and getting responses.
 */
public interface HttpClient {

    /**
     * Send the provided request asynchronously.
     *
     * @param request The HTTP request to send
     * @return A {@link Mono} that emits response asynchronously
     */
    Mono<HttpResponse> send(HttpRequest request);

    /**
     * Send the provided request asynchronously.
     *
     * @param request The HTTP request to send
     * @param responseTimeout response timeout value for this http request
     * @return A {@link Mono} that emits response asynchronously
     */
    Mono<HttpResponse> send(HttpRequest request, Duration responseTimeout);

    /**
     * Create HttpClient with FixedChannelPool {@link HttpClientConfig}
     *
     * @return the HttpClient
     */
    static HttpClient createFixed(HttpClientConfig httpClientConfig) {
        if (httpClientConfig.getConfigs() == null) {
            throw new IllegalArgumentException("HttpClientConfig is null");
        }

        ConnectionProvider.Builder fixedConnectionProviderBuilder = ConnectionProvider
            .builder(httpClientConfig.getConnectionPoolName());
        fixedConnectionProviderBuilder.maxConnections(httpClientConfig.getMaxPoolSize());
        fixedConnectionProviderBuilder.pendingAcquireTimeout(httpClientConfig.getConnectionAcquireTimeout());
        fixedConnectionProviderBuilder.maxIdleTime(httpClientConfig.getMaxIdleConnectionTimeout());

        // TODO[Http2]: config Http2AllocationStrategy (maxConnections & maxConcurrentStreams)
        if (httpClientConfig.getHttp2ConnectionConfig().isEnabled()) {
            fixedConnectionProviderBuilder.allocationStrategy(
                Http2AllocationStrategy.builder()
                    .minConnections(httpClientConfig.getHttp2ConnectionConfig().getMinConnectionPoolSize())
                    .maxConnections(httpClientConfig.getHttp2ConnectionConfig().getMaxConnectionPoolSize())
                    .maxConcurrentStreams(httpClientConfig.getHttp2ConnectionConfig().getMaxConcurrentStreams())
                    .build()
            );
        }

        return ReactorNettyClient.createWithConnectionProvider(fixedConnectionProviderBuilder.build(),
            httpClientConfig);
    }

    /**
     * Create HttpClient with un-pooled connection {@link HttpClientConfig}
     *
     * @return the HttpClient
     */
    static HttpClient create(HttpClientConfig httpClientConfig) {
        if (httpClientConfig.getConfigs() == null) {
            throw new IllegalArgumentException("HttpClientConfig is null");
        }

        return ReactorNettyClient.create(httpClientConfig);
    }

    /**
     * Shutdown the Http Client and clean up resources
     */
    void shutdown();
}
