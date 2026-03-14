// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2PingFrame;
import io.netty.handler.codec.http2.Http2PingFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HTTP/2 PING-based health checker for parent HTTP/2 connections.
 * <p>
 * Installed on the parent TCP channel (not child H2 streams). Periodically sends
 * HTTP/2 PING frames and tracks last ACK timestamp. The connection pool's
 * eviction predicate reads {@link #LAST_PING_ACK_NANOS} to determine liveness.
 * <p>
 * Lifecycle: one instance per parent H2 connection. The handler is guarded by
 * {@link #HANDLER_INSTALLED} to prevent duplicate installation when multiple
 * child streams are opened on the same parent.
 */
public class Http2PingHealthHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(Http2PingHealthHandler.class);

    /**
     * Nano timestamp of the last PING ACK received on this parent channel.
     * Updated ONLY when an Http2PingFrame with ack=true arrives — NOT on arbitrary reads.
     * Http2FrameCodec propagates PING ACK frames to downstream handlers (addLast position).
     * When the network is blackholed, no PING ACKs arrive, this goes stale, eviction triggers.
     */
    public static final AttributeKey<Long> LAST_PING_ACK_NANOS =
        AttributeKey.valueOf("cosmos.h2.lastPingAckNanos");

    /**
     * Guard attribute to prevent duplicate handler installation on the same parent channel.
     */
    static final AttributeKey<Boolean> HANDLER_INSTALLED =
        AttributeKey.valueOf("cosmos.h2.pingHealthInstalled");

    static final String HANDLER_NAME = "cosmos.h2PingHealth";

    private final long pingIntervalMs;
    private final long pingContent;
    private ScheduledFuture<?> pingTask;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * @param pingIntervalMs interval between PING frames in milliseconds
     */
    public Http2PingHealthHandler(long pingIntervalMs) {
        this.pingIntervalMs = pingIntervalMs;
        // Fixed PING payload — readable as "cosmos" in hex
        this.pingContent = 0xC0_5D_B0_01L;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // Seed the last-ack timestamp so the eviction predicate doesn't immediately
        // consider a brand-new connection as "PING-stale"
        ctx.channel().attr(LAST_PING_ACK_NANOS).set(System.nanoTime());

        // The handler is installed from a child stream's doOnConnected, so the parent
        // channel is already active — channelActive() won't fire. Start the schedule now.
        if (ctx.channel().isActive()) {
            schedulePing(ctx);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Fallback: if handler is added before the channel becomes active (unlikely
        // with the current parent-install pattern, but correct for completeness)
        schedulePing(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cancelPing();
        super.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        cancelPing();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2PingFrame) {
            Http2PingFrame pingFrame = (Http2PingFrame) msg;
            if (pingFrame.ack()) {
                ctx.channel().attr(LAST_PING_ACK_NANOS).set(System.nanoTime());
                if (logger.isDebugEnabled()) {
                    logger.debug("HTTP/2 PING ACK received on channel {}",
                        ctx.channel().id().asShortText());
                }
            }
        }
        // Always propagate — don't consume frames
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Http2PingHealthHandler error on channel {}: {}",
            ctx.channel().id().asShortText(), cause.getMessage());
        ctx.fireExceptionCaught(cause);
    }

    private void schedulePing(ChannelHandlerContext ctx) {
        if (closed.get() || !ctx.channel().isActive() || this.pingTask != null) {
            return;
        }
        // Use the channel's event loop to avoid threading issues
        this.pingTask = ctx.channel().eventLoop().scheduleAtFixedRate(
            () -> sendPing(ctx),
            pingIntervalMs,
            pingIntervalMs,
            TimeUnit.MILLISECONDS
        );
    }

    private void sendPing(ChannelHandlerContext ctx) {
        if (!ctx.channel().isActive()) {
            cancelPing();
            return;
        }
        DefaultHttp2PingFrame pingFrame = new DefaultHttp2PingFrame(pingContent, false);
        // Use channel().writeAndFlush() — NOT ctx.writeAndFlush().
        // Our handler is at addLast (after Http2FrameCodec). ctx.writeAndFlush() sends outbound
        // from our position toward the network, BYPASSING the codec (frames aren't encoded).
        // channel().writeAndFlush() starts from the pipeline tail, going through ALL handlers
        // including Http2FrameCodec which encodes the PingFrame to HTTP/2 binary wire format.
        ctx.channel().writeAndFlush(pingFrame).addListener(future -> {
            if (!future.isSuccess()) {
                logger.debug("HTTP/2 PING send failed on channel {}: {}",
                    ctx.channel().id().asShortText(),
                    future.cause() != null ? future.cause().getMessage() : "unknown");
            } else if (logger.isTraceEnabled()) {
                logger.trace("HTTP/2 PING sent on channel {}", ctx.channel().id().asShortText());
            }
        });
    }

    private void cancelPing() {
        if (closed.compareAndSet(false, true) && this.pingTask != null) {
            this.pingTask.cancel(false);
        }
    }

    /**
     * Installs this handler on the parent H2 channel if not already installed.
     * Safe to call from any child stream's doOnConnected callback — will navigate
     * to the parent channel and install exactly once.
     *
     * @param childChannel the child H2 stream channel from doOnConnected
     * @param pingIntervalMs PING interval in milliseconds
     */
    public static void installOnParentIfAbsent(Channel channel, long pingIntervalMs) {
        // In reactor-netty H2 mode, doOnConnected fires for the PARENT TCP channel
        // (not child streams). channel.parent() is null because we're already on the parent.
        // For child streams (if they ever fire), navigate to parent.
        Channel targetChannel = channel.parent() != null ? channel.parent() : channel;

        // Atomic check-and-set to prevent duplicate installation from concurrent doOnConnected callbacks
        if (Boolean.TRUE.equals(targetChannel.attr(HANDLER_INSTALLED).getAndSet(Boolean.TRUE))) {
            return; // already installed
        }

        targetChannel.pipeline().addLast(HANDLER_NAME, new Http2PingHealthHandler(pingIntervalMs));

        if (logger.isDebugEnabled()) {
            logger.debug("Installed Http2PingHealthHandler on channel {} with {}ms interval",
                targetChannel.id().asShortText(), pingIntervalMs);
        }
    }
}
