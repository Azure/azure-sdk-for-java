// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import io.netty.channel.Channel;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.Http2AllocationStrategy;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

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

        int maxLifetimeSeconds = Configs.getHttpConnectionMaxLifetimeInSeconds();
        int pingAckTimeoutSeconds = Configs.getHttp2PingAckTimeoutInSeconds();
        if (maxLifetimeSeconds > 0 || pingAckTimeoutSeconds > 0) {
            long baseMaxLifeMs = maxLifetimeSeconds > 0 ? maxLifetimeSeconds * 1000L : 0;
            int jitterRangeSeconds = Configs.HTTP_CONNECTION_MAX_LIFETIME_JITTER_IN_SECONDS; // [1, jitterRange] seconds
            long maxIdleTimeMs = httpClientConfig.getMaxIdleConnectionTimeout().toMillis();
            long pingAckTimeoutNanos = pingAckTimeoutSeconds > 0 ? pingAckTimeoutSeconds * 1_000_000_000L : 0;

            fixedConnectionProviderBuilder.evictionPredicate((connection, metadata) -> {
                // Phase 0: Dead channel — always evict
                if (!connection.channel().isActive()) {
                    return true;
                }

                // Phase 1: Idle timeout — unchanged from default behavior
                if (maxIdleTimeMs > 0 && metadata.idleTime() > maxIdleTimeMs) {
                    return true;
                }

                // Phase 2: PING liveness — if PING ACK is stale, connection is silently degraded
                if (pingAckTimeoutNanos > 0) {
                    Channel parentChannel = connection.channel();
                    if (parentChannel.hasAttr(Http2PingHealthHandler.LAST_PING_ACK_NANOS)) {
                        long lastAckNanos = parentChannel.attr(Http2PingHealthHandler.LAST_PING_ACK_NANOS).get();
                        if (System.nanoTime() - lastAckNanos > pingAckTimeoutNanos) {
                            return true;
                        }
                    }
                }

                // Phase 3: Lifetime with jitter — connection exceeded its jittered max lifetime
                // Jitter is per-evaluation with 1s granularity in [1s, jitterRange]. ThreadLocalRandom
                // is lock-free and safe for concurrent eviction sweeps across event loops.
                if (baseMaxLifeMs > 0) {
                    int connJitterMs = ThreadLocalRandom.current().nextInt(1, jitterRangeSeconds + 1) * 1000;
                    return metadata.lifeTime() > (baseMaxLifeMs + connJitterMs);
                }

                return false;
            });

            fixedConnectionProviderBuilder.evictInBackground(Duration.ofSeconds(5));
        }

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
}
