// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.DefaultHttp2PingFrame;
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
 * reaping the connection. Modeled after Go SDK's {@code ReadIdleTimeout} approach.
 * <p>
 * This handler does NOT close the connection on missed ACKs — connection eviction is
 * handled by the eviction predicate in {@link HttpClient#createFixed}. The handler's
 * sole purpose is keepalive traffic to reset middlebox idle timers.
 * <p>
 * Why manual instead of reactor-netty native {@code pingAckTimeout}? Native PING requires
 * built-in {@code maxIdleTime} handling, which is bypassed when a custom
 * {@code evictionPredicate} is configured (reactor-netty 1.2.13).
 */
public class Http2PingHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(Http2PingHandler.class);

    public static final String HANDLER_NAME = "cosmos.http2PingHandler";

    static final AttributeKey<Boolean> PING_HANDLER_INSTALLED =
        AttributeKey.valueOf("cosmos.conn.pingHandlerInstalled");

    private final long pingIntervalNanos;
    private volatile long lastActivityNanos;
    private ScheduledFuture<?> pingTask;
    private final AtomicInteger pingsSent = new AtomicInteger(0);
    private final AtomicInteger pingAcksReceived = new AtomicInteger(0);

    /**
     * @param pingIntervalSeconds interval in seconds; when idle longer than this, a PING is sent
     */
    public Http2PingHandler(int pingIntervalSeconds) {
        this.pingIntervalNanos = TimeUnit.SECONDS.toNanos(pingIntervalSeconds);
        this.lastActivityNanos = System.nanoTime();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // Schedule periodic check — runs on the channel's event loop (single-threaded, no sync needed)
        long checkIntervalMs = Math.max(1000, TimeUnit.NANOSECONDS.toMillis(pingIntervalNanos) / 2);
        this.pingTask = ctx.executor().scheduleAtFixedRate(
            () -> maybeSendPing(ctx),
            checkIntervalMs,
            checkIntervalMs,
            TimeUnit.MILLISECONDS);

        if (logger.isDebugEnabled()) {
            logger.debug("Http2PingHandler installed on channel {}, interval={}s, checkEvery={}ms",
                ctx.channel().id().asShortText(),
                TimeUnit.NANOSECONDS.toSeconds(pingIntervalNanos),
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
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        lastActivityNanos = System.nanoTime();
        super.write(ctx, msg, promise);
    }

    private void maybeSendPing(ChannelHandlerContext ctx) {
        if (!ctx.channel().isActive()) {
            cancelPingTask();
            return;
        }

        long idleNanos = System.nanoTime() - lastActivityNanos;
        if (idleNanos >= pingIntervalNanos) {
            int count = pingsSent.incrementAndGet();
            ctx.writeAndFlush(new DefaultHttp2PingFrame(count))
                .addListener(f -> {
                    if (f.isSuccess()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("PING #{} sent on channel {}", count, ctx.channel().id().asShortText());
                        }
                    } else {
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
     * Installs the PING handler on the parent H2 channel if not already installed.
     * Safe to call from doOnConnected (which fires per-stream for H2).
     *
     * @param channel the channel from doOnConnected (may be stream or parent)
     * @param pingIntervalSeconds PING interval in seconds
     */
    public static void installIfAbsent(Channel channel, int pingIntervalSeconds) {
        // When called from the first doOnConnected, channel IS the parent H2 channel.
        // When called from a stream doOnConnected, channel.parent() is the parent.
        Channel parent = channel.parent() != null ? channel.parent() : channel;
        if (!parent.hasAttr(PING_HANDLER_INSTALLED)) {
            parent.attr(PING_HANDLER_INSTALLED).set(Boolean.TRUE);
            try {
                parent.pipeline().addLast(HANDLER_NAME, new Http2PingHandler(pingIntervalSeconds));
            } catch (IllegalArgumentException ignored) {
                // Duplicate — race between concurrent streams, benign
            }
        }
    }
}
