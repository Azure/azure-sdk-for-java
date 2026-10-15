// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Per-child-stream HTTP/2 handler that translates a parent-TCP-channel close
 * driven by {@link Http2PingHandler} into a typed {@link Http2PingTimeoutChannelClosedException}.
 * <p>
 * Installed at the head of each H2 child-stream pipeline by
 * {@code ReactorNettyClient}'s {@code .observe(...)} hook on {@code STREAM_CONFIGURED}
 * (the per-child-stream lifecycle event). When the parent (TCP)
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
 * install site in {@code ReactorNettyClient} only runs at {@code STREAM_CONFIGURED}
 * (H2 child streams) and additionally gates on {@code ch.parent() != null}.
 */
@ChannelHandler.Sharable
final class Http2PingCloseRewrapHandler extends ChannelInboundHandlerAdapter {

    static final String HANDLER_NAME = "cosmos.http2PingCloseRewrap";

    static final Http2PingCloseRewrapHandler INSTANCE = new Http2PingCloseRewrapHandler();

    private Http2PingCloseRewrapHandler() {}

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
