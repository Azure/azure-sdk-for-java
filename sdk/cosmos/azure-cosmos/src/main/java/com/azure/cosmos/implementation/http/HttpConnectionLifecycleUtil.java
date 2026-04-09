// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for HTTP connection lifecycle management.
 * <p>
 * Provides channel attributes and stamping logic for per-connection max lifetime
 * with jitter. The eviction predicate in {@link HttpClient#createFixed} reads these
 * attributes to determine when a connection should be closed.
 * <p>
 * PING keepalive is handled by custom {@link Http2PingHandler} installed on H2 parent
 * channels via {@code doOnConnected} in {@link ReactorNettyClient}.
 */
public final class HttpConnectionLifecycleUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpConnectionLifecycleUtil.class);

    /**
     * Per-connection expiry timestamp (nanos). Stamped once at connection creation with
     * baseMaxLifetime - per-connection jitter. The eviction predicate reads this to determine
     * if a connection has exceeded its jittered max lifetime. Per-connection jitter prevents
     * thundering-herd expiry when many connections are created around the same time.
     */
    public static final AttributeKey<Long> CONNECTION_EXPIRY_NANOS =
        AttributeKey.valueOf("cosmos.conn.connectionExpiryNanos");

    /**
     * Nano timestamp when a connection was marked for pending eviction (two-phase eviction).
     * Used by Phase 2 (lifetime) to allow active streams to drain before closing the connection.
     * The eviction predicate marks a connection as pending, then evicts it when idle or after
     * a grace period expires.
     */
    public static final AttributeKey<Long> PENDING_EVICTION_NANOS =
        AttributeKey.valueOf("cosmos.conn.pendingEvictionNanos");

    private HttpConnectionLifecycleUtil() {
        // utility class
    }

    /**
     * Stamps a per-connection expiry timestamp on the channel.
     * Independent of PING health — max lifetime works even if PING is disabled.
     * Safe to call multiple times; only the first call stamps the attribute.
     * <p>
     * Jitter is subtracted from the base lifetime to ensure the effective lifetime
     * never exceeds the configured max. Effective range: {@code [base - jitter, base]}.
     * Matches reactor-netty 1.3.4's {@code maxLifeTimeVariance} semantics.
     *
     * @param channel the channel from doOnConnected (parent H2 or child stream)
     * @param baseMaxLifetimeMs base max lifetime in milliseconds
     * @param jitterRangeMs max jitter in milliseconds (e.g., 30_000 for [0s, 30s] range)
     */
    public static void stampConnectionExpiry(Channel channel, long baseMaxLifetimeMs, long jitterRangeMs) {
        Channel targetChannel = channel.parent() != null ? channel.parent() : channel;

        if (!targetChannel.hasAttr(CONNECTION_EXPIRY_NANOS)) {
            long jitterMs = ThreadLocalRandom.current().nextLong(0, jitterRangeMs + 1);
            long expiryNanos = System.nanoTime() + (baseMaxLifetimeMs - jitterMs) * 1_000_000L;
            targetChannel.attr(CONNECTION_EXPIRY_NANOS).set(expiryNanos);

            if (logger.isDebugEnabled()) {
                logger.debug("Stamped connection expiry on channel {}: maxLifetime={}ms, jitter=-{}ms, effective={}ms",
                    targetChannel.id().asShortText(), baseMaxLifetimeMs, jitterMs, baseMaxLifetimeMs - jitterMs);
            }
        }
    }
}
