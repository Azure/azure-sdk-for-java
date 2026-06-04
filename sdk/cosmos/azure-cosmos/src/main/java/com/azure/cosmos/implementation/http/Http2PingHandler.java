// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2PingFrame;
import io.netty.handler.codec.http2.Http2PingFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manual HTTP/2 PING keepalive handler installed on the parent (TCP) channel.
 * <p>
 * Sends PING frames when the connection is idle for longer than the configured interval,
 * preventing L7 middleboxes (NAT gateways, firewalls, load balancers) from silently
 * reaping the connection. Modeled after Rust SDK's hyper-based HTTP/2 PING approach.
 * <p>
 * When a PING ACK is not received within the configured timeout, the handler closes
 * the connection. The connection pool will then create a fresh replacement. This is
 * aligned with the Rust SDK where hyper kills connections on PING timeout and the
 * shard health sweep replaces them.
 * <p>
 * Why manual instead of reactor-netty native {@code pingAckTimeout}? Native PING requires
 * built-in {@code maxIdleTime} handling, which is bypassed when a custom
 * {@code evictionPredicate} is configured (reactor-netty 1.2.13).
 * <p>
 * Note: the Cosmos DB Proxy (Standard Gateway, ThinClient) uses nghttp2 which auto-ACKs
 * PINGs per RFC 9113 §6.7. The SQLx Mux uses a custom frame parser that currently rejects
 * PING with PROTOCOL_ERROR — but SQLx does not negotiate H2 via ALPN, so clients never
 * speak H2 to it.
 */
public class Http2PingHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(Http2PingHandler.class);

    private static final String HANDLER_NAME = "cosmos.http2PingHandler";

    private final long pingIntervalNanos;
    private final long pingTimeoutNanos;
    private final int failureThreshold;

    // Mutable fields below are accessed only from the channel's EventLoop thread
    // (handlerAdded, channelRead, scheduled task, writeAndFlush listener), so no
    // synchronization or volatile is needed.
    //
    // nanoTime of the last connection-level activity (inbound frame or PING send).
    // Note: H2 stream frames (HEADERS/DATA -- i.e. actual HTTP responses) are
    // dispatched by Http2MultiplexHandler to child channels and never surface on
    // the parent pipeline, so this does NOT track request/response traffic; it
    // tracks PING ACKs, SETTINGS, GOAWAY, and our own PING sends.
    private long lastActivityNanos;
    private long pingOutstandingSinceNanos;  // nanoTime when PING was sent; 0 = no outstanding PING
    private int consecutiveFailures;         // incremented on timeout, reset on ACK
    private int pingsSent;
    private ScheduledFuture<?> pingTask;

    /**
     * @param pingIntervalSeconds  interval in seconds; when idle longer than this, a PING is sent
     * @param pingTimeoutSeconds   timeout in seconds per PING attempt
     * @param failureThreshold     consecutive timeouts before closing the connection
     */
    public Http2PingHandler(int pingIntervalSeconds, int pingTimeoutSeconds, int failureThreshold) {
        this.pingIntervalNanos = TimeUnit.SECONDS.toNanos(Math.max(1, pingIntervalSeconds));
        this.pingTimeoutNanos = TimeUnit.SECONDS.toNanos(Math.max(1, pingTimeoutSeconds));
        this.failureThreshold = Math.max(1, failureThreshold);
        this.lastActivityNanos = System.nanoTime();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // Schedule periodic check -- runs on the channel's event loop (single-threaded, no sync needed)
        // Check at interval/2 (min 500ms) to bound worst-case PING send delay to ~1.5× interval.
        long checkIntervalMs = Math.max(500, TimeUnit.NANOSECONDS.toMillis(pingIntervalNanos) / 2);
        this.pingTask = ctx.executor().scheduleAtFixedRate(
            () -> maybeSendPing(ctx),
            checkIntervalMs,
            checkIntervalMs,
            TimeUnit.MILLISECONDS);

        logger.debug("Http2PingHandler installed on channel {}, interval={}s, timeout={}s, checkEvery={}ms",
            ctx.channel(),
            TimeUnit.NANOSECONDS.toSeconds(pingIntervalNanos),
            TimeUnit.NANOSECONDS.toSeconds(pingTimeoutNanos),
            checkIntervalMs);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        cancelPingTask();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cancelPingTask();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        lastActivityNanos = System.nanoTime();
        if (msg instanceof Http2PingFrame) {
            Http2PingFrame ping = (Http2PingFrame) msg;
            // RFC 9113 §6.7: PING ACK echoes the 8-byte payload we sent.
            // Two guards prevent a late ACK from masking ongoing degradation:
            //   1) pingOutstandingSinceNanos != 0 -- after timeout fires we clear
            //      this to 0; a late ACK arriving before the next PING is sent
            //      still has ping.content() == pingsSent (pingsSent isn't bumped
            //      until the next send), so the payload alone is insufficient.
            //   2) ping.content() == pingsSent -- guards the window AFTER the next
            //      PING is sent (pingsSent has advanced), so a late ACK for the
            //      previous PING no longer matches.
            if (ping.ack() && pingOutstandingSinceNanos != 0 && ping.content() == pingsSent) {
                pingOutstandingSinceNanos = 0;
                consecutiveFailures = 0;
            }
        }
        super.channelRead(ctx, msg);
    }

    private void maybeSendPing(ChannelHandlerContext ctx) {
        // Channel-dead is a hard stop -- cancel the timer.
        if (!ctx.channel().isActive()) {
            cancelPingTask();
            return;
        }
        // Soft gate re-checked per tick: the global kill-switch
        // Configs.isHttp2PingHealthEnabled() (system property). Flipping it false
        // makes this tick a no-op but KEEPS the timer alive, so toggling it back
        // to true automatically resumes PINGing on the same connection (no need
        // to wait for connection recycling).
        if (!Configs.isHttp2PingHealthEnabled()) {
            return;
        }

        // If a previous PING is still outstanding, check whether it has timed out
        if (pingOutstandingSinceNanos != 0) {
            long waitingNanos = System.nanoTime() - pingOutstandingSinceNanos;
            if (waitingNanos >= pingTimeoutNanos) {
                consecutiveFailures++;
                pingOutstandingSinceNanos = 0; // unblock next PING attempt

                if (consecutiveFailures >= failureThreshold) {
                    // Threshold reached -- connection is broken.
                    logger.info("PING ACK not received for {} consecutive attempts on channel {} -- closing connection",
                        consecutiveFailures, ctx.channel());
                    cancelPingTask();
                    ctx.close();
                } else {
                    logger.debug("PING ACK timeout on channel {} (attempt {}/{}) -- will retry",
                        ctx.channel(), consecutiveFailures, failureThreshold);
                }
            }
            // Don't send another PING while one is outstanding
            return;
        }

        long idleNanos = System.nanoTime() - lastActivityNanos;
        if (idleNanos >= pingIntervalNanos) {
            int count = ++pingsSent;
            pingOutstandingSinceNanos = System.nanoTime();
            ctx.writeAndFlush(new DefaultHttp2PingFrame(count))
                .addListener(f -> {
                    if (f.isSuccess()) {
                        logger.debug("PING #{} sent on channel {}", count, ctx.channel());
                    } else {
                        // Listener runs on the same event loop as the scheduled task,
                        // so mutating pingOutstandingSinceNanos is thread-safe.
                        pingOutstandingSinceNanos = 0; // unblock next attempt on send failure
                        logger.debug("PING #{} send failed on channel {}: {}",
                            count, ctx.channel(),
                            f.cause() != null ? f.cause().getMessage() : "unknown");
                    }
                });
            // Reset activity timestamp so we don't send another PING immediately
            lastActivityNanos = System.nanoTime();
        }
    }

    private void cancelPingTask() {
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
    }

    /**
     * Returns {@code true} when the manual HTTP/2 PING keepalive should be active for
     * a client built with the given {@link Http2ConnectionConfig}. This is the single
     * source of truth shared by the transport install site (see
     * {@link ReactorNettyClient}) and the user-agent flag emission (see
     * {@code RxDocumentClientImpl}) so the two cannot drift.
     * <p>
     * All three gates must pass:
     * <ul>
     *   <li>The global kill-switch {@link Configs#isHttp2PingHealthEnabled()} is true.</li>
     *   <li>{@link Configs#getHttp2PingIntervalInSeconds()} is &gt; 0 (a non-positive
     *       interval disables the handler at install time).</li>
     *   <li>HTTP/2 is effectively enabled for this client
     *       (either via {@link Configs#isHttp2Enabled()} or the per-client
     *       {@link Http2ConnectionConfig} override).</li>
     * </ul>
     */
    public static boolean isPingHealthEffectivelyEnabled(Http2ConnectionConfig http2Cfg) {
        if (!Configs.isHttp2PingHealthEnabled()) {
            return false;
        }
        if (Configs.getHttp2PingIntervalInSeconds() <= 0) {
            return false;
        }
        return ImplementationBridgeHelpers.Http2ConnectionConfigHelper
            .getHttp2ConnectionConfigAccessor()
            .isEffectivelyEnabled(http2Cfg);
    }

    /**
     * Installs the PING handler on the parent H2 channel if not already installed.
     * Safe to call from doOnConnected (which fires per-stream for H2).
     *
     * @param channel the channel from doOnConnected (may be stream or parent)
     * @param pingIntervalSeconds PING interval in seconds
     * @param pingTimeoutSeconds  PING ACK timeout in seconds
     * @param failureThreshold    consecutive timeouts before closing
     */
    public static void installIfAbsent(Channel channel, int pingIntervalSeconds, int pingTimeoutSeconds,
                                       int failureThreshold) {
        Channel parent = channel.parent() != null ? channel.parent() : channel;
        if (parent.pipeline().get(HANDLER_NAME) == null) {
            try {
                parent.pipeline().addLast(HANDLER_NAME,
                    new Http2PingHandler(pingIntervalSeconds, pingTimeoutSeconds, failureThreshold));
            } catch (IllegalArgumentException ignored) {
                // Duplicate -- race between concurrent streams, benign
            }
        }
    }
}
