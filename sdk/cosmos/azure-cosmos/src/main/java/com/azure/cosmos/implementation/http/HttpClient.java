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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

        int maxLifetimeSeconds = Configs.isHttpConnectionMaxLifetimeEnabled()
            ? Configs.getHttpConnectionMaxLifetimeInSeconds() : 0;
        if (maxLifetimeSeconds > 0) {
            long maxIdleTimeMs = httpClientConfig.getMaxIdleConnectionTimeout().toMillis();

            // Derive sweep interval: must be less than the smallest eviction threshold,
            // otherwise we overshoot (a connection sits past its threshold until the next sweep).
            // Use min(thresholds) / 2, clamped to [1s, 5s] to avoid excessive polling.
            long minThresholdSeconds = Long.MAX_VALUE;
            if (maxIdleTimeMs > 0) {
                minThresholdSeconds = Math.min(minThresholdSeconds, maxIdleTimeMs / 1000);
            }
            if (maxLifetimeSeconds > 0) {
                minThresholdSeconds = Math.min(minThresholdSeconds, maxLifetimeSeconds);
            }
            long sweepSeconds = Math.max(1, Math.min(5, minThresholdSeconds / 2));
            long sweepIntervalNanos = sweepSeconds * 1_000_000_000L;

            // Eviction rate limiter: at most 1 connection evicted per sweep cycle.
            // Prevents cliff eviction when multiple connections expire in the same window.
            // Dead channels (Phase 0) are exempt — they're already unusable.
            final AtomicInteger evictedThisCycle = new AtomicInteger(0);
            final AtomicLong cycleStartNanos = new AtomicLong(System.nanoTime());
            final int maxEvictionsPerCycle = 1;

            fixedConnectionProviderBuilder.evictionPredicate((connection, metadata) -> {
                // Phase 0: Dead channel — always evict (no rate limit, channel is already unusable)
                if (!connection.channel().isActive()) {
                    return true;
                }

                // Reset eviction counter on new sweep cycle
                long now = System.nanoTime();
                long cycleStart = cycleStartNanos.get();
                if (now - cycleStart > sweepIntervalNanos) {
                    cycleStartNanos.compareAndSet(cycleStart, now);
                    evictedThisCycle.set(0);
                }

                // Rate limit: skip eviction if we've already evicted enough this cycle
                if (evictedThisCycle.get() >= maxEvictionsPerCycle) {
                    return false;
                }

                // Phase 1: Idle timeout — must be in the predicate because a custom evictionPredicate
                // replaces reactor-netty's built-in maxIdleTime/maxLifeTime handling (1.2.13 docs:
                // "Otherwise only the custom eviction predicate is invoked").
                if (maxIdleTimeMs > 0 && metadata.idleTime() > maxIdleTimeMs) {
                    evictedThisCycle.incrementAndGet();
                    return true;
                }

                // NOTE: No PING-based eviction. Http2PingHealthHandler sends PING frames as
                // keepalive (prevents NAT/firewall idle reaping) but does NOT trigger eviction
                // on stale ACKs. Degraded connections are handled by the response timeout retry
                // path (6s/6s/10s escalation → cross-region failover).

                // Phase 2: Per-connection max lifetime with jitter — two-phase eviction.
                // CONNECTION_EXPIRY_NANOS is stamped once per connection in doOnConnected
                // (via Http2PingHealthHandler.stampConnectionExpiry) with baseMaxLifetime + random jitter.
                //
                // Two-phase: instead of evicting immediately (which RST_STREAMs active H2 streams),
                // mark the connection as pending eviction. On subsequent sweeps, evict when idle
                // or after the drain grace period expires.
                if (maxLifetimeSeconds > 0) {
                    Channel parentChannel = connection.channel();
                    Long expiryNanos = parentChannel.hasAttr(Http2PingHealthHandler.CONNECTION_EXPIRY_NANOS)
                        ? parentChannel.attr(Http2PingHealthHandler.CONNECTION_EXPIRY_NANOS).get()
                        : null;
                    if (expiryNanos != null && now > expiryNanos) {
                        // Check if already marked for pending eviction
                        Long pendingSince = parentChannel.hasAttr(Http2PingHealthHandler.PENDING_EVICTION_NANOS)
                            ? parentChannel.attr(Http2PingHealthHandler.PENDING_EVICTION_NANOS).get()
                            : null;

                        if (pendingSince == null) {
                            // First detection — mark as pending, don't evict yet
                            parentChannel.attr(Http2PingHealthHandler.PENDING_EVICTION_NANOS).set(now);
                            return false;
                        }

                        // Already pending — evict if idle or grace period (10s) expired
                        long drainGraceNanos = 10_000_000_000L; // 10 seconds
                        if (metadata.idleTime() > 0 || now - pendingSince > drainGraceNanos) {
                            evictedThisCycle.incrementAndGet();
                            return true;
                        }

                        return false; // active streams — wait for next sweep
                    }
                }

                return false;
            });

            fixedConnectionProviderBuilder.evictInBackground(Duration.ofSeconds(sweepSeconds));
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
