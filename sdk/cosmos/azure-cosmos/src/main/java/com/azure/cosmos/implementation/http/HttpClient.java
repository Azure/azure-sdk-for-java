// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.Http2AllocationStrategy;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.function.BooleanSupplier;

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
        Integer customPendingAcquireMaxCount = httpClientConfig.getPendingAcquireMaxCount();
        if (customPendingAcquireMaxCount != null) {
            fixedConnectionProviderBuilder.pendingAcquireMaxCount(customPendingAcquireMaxCount);
        }
        fixedConnectionProviderBuilder.pendingAcquireTimeout(httpClientConfig.getConnectionAcquireTimeout());
        fixedConnectionProviderBuilder.maxIdleTime(httpClientConfig.getMaxIdleConnectionTimeout());
        if (Configs.isNettyHttpClientMetricsEnabled()) {
            fixedConnectionProviderBuilder.metrics(true);
        }

        ImplementationBridgeHelpers.Http2ConnectionConfigHelper.Http2ConnectionConfigAccessor http2CfgAccessor =
            ImplementationBridgeHelpers.Http2ConnectionConfigHelper.getHttp2ConnectionConfigAccessor();
        Http2ConnectionConfig http2Cfg = httpClientConfig.getHttp2ConnectionConfig();
        if (http2CfgAccessor.isEffectivelyEnabled(http2Cfg)) {
            fixedConnectionProviderBuilder.allocationStrategy(
                Http2AllocationStrategy.builder()
                    .minConnections(http2CfgAccessor.getEffectiveMinConnectionPoolSize(http2Cfg))
                    .maxConnections(http2CfgAccessor.getEffectiveMaxConnectionPoolSize(http2Cfg))
                    .maxConcurrentStreams(http2CfgAccessor.getEffectiveMaxConcurrentStreams(http2Cfg))
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

    /**
     * Sets a late-bound supplier that gates installation of HTTP/2 PING keepalive
     * to channels where the supplier returns {@code true}. The supplier is invoked
     * inside {@code doOnConnected} for the parent (TCP) channel; when it returns
     * {@code false}, no PING handler is installed.
     *
     * <p>The supplier must be a bound method reference that does <strong>not</strong>
     * capture references to the owning {@code CosmosAsyncClient} /
     * {@code RxDocumentClientImpl}. See
     * {@code GlobalEndpointManager#getHasThinClientReadLocationsRef()} for the
     * rationale; the recommended pattern is {@code atomicBoolean::get}.
     *
     * <p>If never set, PING installation falls back to the prior behavior gated only
     * by {@link Configs#isHttp2PingHealthEnabled()}.
     *
     * <p>Default no-op so non-Netty implementations and tests need not override.
     */
    default void setHttp2PingScopeSupplier(BooleanSupplier supplier) {
        // no-op by default; ReactorNettyClient overrides.
    }
}
