// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.DefaultHttp2PingFrame;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2PingFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
 */
public class Http2PingHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(Http2PingHandler.class);

    private static final String HANDLER_NAME = "cosmos.http2PingHandler";

    static final AttributeKey<Boolean> PING_HANDLER_INSTALLED =
        AttributeKey.valueOf("cosmos.conn.pingHandlerInstalled");

    /**
     * Channel attribute set to {@code true} when a PING ACK is not received before the
     * next PING would be due. Cleared when a successful ACK arrives. External components
     * (eviction predicates, health checks) can read this attribute to detect broken connections.
     */
    public static final AttributeKey<Boolean> PING_HEALTH_DEGRADED =
        AttributeKey.valueOf("cosmos.conn.pingHealthDegraded");

    // Global (process-wide) counters across all handler instances — used by tests
    private static final AtomicInteger globalPingsSent = new AtomicInteger(0);
    private static final AtomicInteger globalPingAcksReceived = new AtomicInteger(0);

    private final long pingIntervalNanos;
    private final long pingTimeoutNanos;
    private final int failureThreshold;
    private long lastActivityNanos;
    private long pingOutstandingSinceNanos; // nanoTime when PING was sent; 0 = no outstanding PING
    private int consecutiveFailures;        // incremented on timeout, reset on ACK
    private ScheduledFuture<?> pingTask;
    private final AtomicInteger pingsSent = new AtomicInteger(0);
    private final AtomicInteger pingAcksReceived = new AtomicInteger(0);

    /**
     * @param pingIntervalSeconds  interval in seconds; when idle longer than this, a PING is sent
     * @param pingTimeoutSeconds   timeout in seconds per PING attempt
     * @param failureThreshold     consecutive timeouts before closing the connection
     */
    public Http2PingHandler(int pingIntervalSeconds, int pingTimeoutSeconds, int failureThreshold) {
        this.pingIntervalNanos = TimeUnit.SECONDS.toNanos(pingIntervalSeconds);
        this.pingTimeoutNanos = TimeUnit.SECONDS.toNanos(pingTimeoutSeconds);
        this.failureThreshold = Math.max(1, failureThreshold);
        this.lastActivityNanos = System.nanoTime();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // Schedule periodic check — runs on the channel's event loop (single-threaded, no sync needed)
        // Check at interval/2 (min 500ms) to bound worst-case PING send delay to ~1.5× interval.
        long checkIntervalMs = Math.max(500, TimeUnit.NANOSECONDS.toMillis(pingIntervalNanos) / 2);
        this.pingTask = ctx.executor().scheduleAtFixedRate(
            () -> maybeSendPing(ctx),
            checkIntervalMs,
            checkIntervalMs,
            TimeUnit.MILLISECONDS);

        if (logger.isDebugEnabled()) {
            logger.debug("Http2PingHandler installed on channel {}, interval={}s, timeout={}s, checkEvery={}ms",
                ctx.channel().id().asShortText(),
                TimeUnit.NANOSECONDS.toSeconds(pingIntervalNanos),
                TimeUnit.NANOSECONDS.toSeconds(pingTimeoutNanos),
                checkIntervalMs);
        }
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
        if (msg instanceof Http2PingFrame && ((Http2PingFrame) msg).ack()) {
            pingAcksReceived.incrementAndGet();
            globalPingAcksReceived.incrementAndGet();
            pingOutstandingSinceNanos = 0;
            consecutiveFailures = 0;
            // Connection proved responsive — clear degraded flag if it was set
            if (ctx.channel().hasAttr(PING_HEALTH_DEGRADED)) {
                ctx.channel().attr(PING_HEALTH_DEGRADED).set(null);
                if (logger.isDebugEnabled()) {
                    logger.debug("PING ACK received, connection {} marked healthy",
                        ctx.channel().id().asShortText());
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        lastActivityNanos = System.nanoTime();
        super.write(ctx, msg, promise);
    }

    private void maybeSendPing(ChannelHandlerContext ctx) {
        if (!ctx.channel().isActive() || !Configs.isHttp2PingHealthEnabled()) {
            cancelPingTask();
            return;
        }

        // If a previous PING is still outstanding, check whether it has timed out
        if (pingOutstandingSinceNanos != 0) {
            long waitingNanos = System.nanoTime() - pingOutstandingSinceNanos;
            if (waitingNanos >= pingTimeoutNanos) {
                consecutiveFailures++;
                pingOutstandingSinceNanos = 0; // unblock next PING attempt

                if (consecutiveFailures >= failureThreshold) {
                    // Threshold reached — connection is broken.
                    ctx.channel().attr(PING_HEALTH_DEGRADED).set(Boolean.TRUE);
                    logger.warn("PING ACK not received for {} consecutive attempts on channel {} — closing connection",
                        consecutiveFailures, ctx.channel().id().asShortText());
                    cancelPingTask();
                    ctx.close();
                } else {
                    logger.warn("PING ACK timeout on channel {} (attempt {}/{}) — will retry",
                        ctx.channel().id().asShortText(), consecutiveFailures, failureThreshold);
                }
            }
            // Don't send another PING while one is outstanding
            return;
        }

        // Don't send if there are active streams — request/response traffic is already
        // keeping the connection alive, so a PING would be pure noise-neighbour overhead.
        Http2FrameCodec codec = ctx.pipeline().get(Http2FrameCodec.class);
        if (codec != null && codec.connection().numActiveStreams() > 0) {
            // Active streams reset the idle baseline so the first idle check after
            // all streams close measures from *now*, not from the last frame.
            lastActivityNanos = System.nanoTime();
            return;
        }

        long idleNanos = System.nanoTime() - lastActivityNanos;
        if (idleNanos >= pingIntervalNanos) {
            int count = pingsSent.incrementAndGet();
            globalPingsSent.incrementAndGet();
            pingOutstandingSinceNanos = System.nanoTime();
            ctx.writeAndFlush(new DefaultHttp2PingFrame(count))
                .addListener(f -> {
                    if (f.isSuccess()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("PING #{} sent on channel {}", count, ctx.channel().id().asShortText());
                        }
                    } else {
                        pingOutstandingSinceNanos = 0; // unblock next attempt on send failure
                        logger.warn("PING #{} failed on channel {}: {}",
                            count, ctx.channel().id().asShortText(),
                            f.cause() != null ? f.cause().getMessage() : "unknown");
                    }
                });
            // Reset activity so we don't send another PING immediately
            lastActivityNanos = System.nanoTime();
        }
    }

    private void cancelPingTask() {
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
    }

    public int getPingsSent() {
        return pingsSent.get();
    }

    public int getPingAcksReceived() {
        return pingAcksReceived.get();
    }

    /**
     * Returns the total number of PINGs sent across all handler instances (process-wide).
     */
    public static int getGlobalPingsSent() {
        return globalPingsSent.get();
    }

    /**
     * Returns the total number of PING ACKs received across all handler instances (process-wide).
     */
    public static int getGlobalPingAcksReceived() {
        return globalPingAcksReceived.get();
    }

    /**
     * Returns {@code true} if the given channel (or its parent) has been marked as
     * unhealthy due to a missed PING ACK.
     */
    public static boolean isConnectionHealthDegraded(Channel channel) {
        Channel parent = channel.parent() != null ? channel.parent() : channel;
        return Boolean.TRUE.equals(parent.hasAttr(PING_HEALTH_DEGRADED)
            ? parent.attr(PING_HEALTH_DEGRADED).get()
            : null);
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
    public static void installIfAbsent(Channel channel, int pingIntervalSeconds, int pingTimeoutSeconds, int failureThreshold) {
        // When called from the first doOnConnected, channel IS the parent H2 channel.
        // When called from a stream doOnConnected, channel.parent() is the parent.
        Channel parent = channel.parent() != null ? channel.parent() : channel;
        if (!parent.hasAttr(PING_HANDLER_INSTALLED)) {
            parent.attr(PING_HANDLER_INSTALLED).set(Boolean.TRUE);
            try {
                parent.pipeline().addLast(HANDLER_NAME, new Http2PingHandler(pingIntervalSeconds, pingTimeoutSeconds, failureThreshold));
            } catch (IllegalArgumentException ignored) {
                // Duplicate — race between concurrent streams, benign
            }
        }
    }
}
