// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-request HTTP/2 child-stream handler that translates a parent-TCP-channel close
 * driven by {@link Http2PingHandler} into a typed {@link Http2PingTimeoutChannelClosedException}.
 * <p>
 * Installed at the head of each H2 child-stream pipeline by
 * {@code ReactorNettyClient}'s {@code .doOnRequest(...)} hook. When the parent (TCP)
 * channel is closed by {@link Http2PingHandler} after consecutive PING-ACK timeouts
 * or consecutive PING-send failures, the H2 multiplex codec propagates
 * {@code channelInactive} to every child stream.
 * This handler fires first (head-of-pipeline), inspects the parent channel's
 * {@link Http2PingHandler#PING_TIMEOUT_CLOSED} attribute, and on match fires
 * {@code exceptionCaught} with the typed exception <em>before</em> the default close
 * path reaches reactor-netty's {@code HttpClientOperations}. That makes the in-flight
 * request's response {@code Mono} fail with the typed exception (which the rest of the
 * stack maps to {@code GATEWAY_HTTP2_PING_TIMEOUT_CHANNEL_CLOSED}) instead of a bare
 * {@code PrematureCloseException}, which would otherwise trigger region mark-down in
 * {@code ClientRetryPolicy}.
 * <p>
 * The handler is stateless and marked {@link ChannelHandler.Sharable}, so a single
 * JVM-wide {@link #INSTANCE} is reused across all H2 child streams.
 * <p>
 * For non-H2 channels (parent is {@code null}) this handler is never installed; the
 * install site in {@code ReactorNettyClient} gates on {@code ch.parent() != null}.
 */
@ChannelHandler.Sharable
final class Http2PingCloseRewrapHandler extends ChannelInboundHandlerAdapter {

    static final String HANDLER_NAME = "cosmos.http2PingCloseRewrap";

    static final Http2PingCloseRewrapHandler INSTANCE = new Http2PingCloseRewrapHandler();

    private Http2PingCloseRewrapHandler() {}

    /**
     * Records inbound H2 stream reads on the parent channel so the PING handler
     * can recognize a busy connection as non-idle and skip the PING send.
     * <p>
     * {@code channelReadComplete} fires at the end of each inbound read cycle on
     * the child channel -- so this method stamps the parent attribute exactly once
     * per batch of HEADERS/DATA frames demuxed by {@link io.netty.handler.codec.http2.Http2MultiplexHandler},
     * not once per frame. Under normal load this is plenty of granularity to
     * suppress unnecessary PINGs without becoming a hot-path allocator.
     * <p>
     * Edge cases:
     * <ul>
     *   <li><b>Backpressure / autoRead off</b>: {@link io.netty.handler.codec.http2.Http2MultiplexHandler} may
     *       buffer frames in the child's inbound queue and defer dispatch. In that
     *       case {@code channelReadComplete} may fire later than the wire-arrival
     *       time, but it WILL fire when the read cycle ends. Worst case: one benign
     *       extra PING during the brief buffering window.</li>
     *   <li><b>Non-H2 channels</b>: this handler is only installed on H2 child
     *       streams (where {@code ch.parent() != null}), so the parent reference
     *       above is non-null in normal operation. Defensive null-check kept for
     *       safety if the install topology ever changes.</li>
     * </ul>
     * Uses {@code accumulateAndGet(now, Math::max)} so concurrent updates from
     * sibling child streams remain monotonic (in practice child streams sharing a
     * parent run on the same event loop, so contention is nil; the monotonic
     * semantics document intent and future-proof against multi-event-loop
     * scenarios).
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Channel parent = ctx.channel().parent();
        if (parent != null) {
            AtomicLong holder = parent.attr(Http2PingHandler.LAST_CHILD_STREAM_READ_ACTIVITY_NANOS).get();
            if (holder != null) {
                holder.accumulateAndGet(System.nanoTime(), Math::max);
            }
        }
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel parent = ctx.channel().parent();
        if (parent != null && Boolean.TRUE.equals(parent.attr(Http2PingHandler.PING_TIMEOUT_CLOSED).get())) {
            // Fire BEFORE delegating to super.channelInactive so reactor-netty's
            // HttpClientOperations.exceptionCaught fails the response Mono with the
            // typed exception, beating its own onInboundClose(PrematureCloseException).
            ctx.fireExceptionCaught(new Http2PingTimeoutChannelClosedException(
                "HTTP/2 connection closed by PING keepalive after consecutive PING-ACK timeouts or PING-send failures.",
                null));
        }
        super.channelInactive(ctx);
    }
}
