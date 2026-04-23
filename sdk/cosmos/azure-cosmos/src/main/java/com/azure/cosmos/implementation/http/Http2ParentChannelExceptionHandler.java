// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2FrameCodec;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler for the HTTP/2 parent (TCP) channel pipeline.
 * <p>
 * In HTTP/2, reactor-netty multiplexes streams on a shared parent TCP connection.
 * Child stream channels have {@code ChannelOperationsHandler} which catches exceptions
 * and fails the active subscriber (matching HTTP/1.1 behavior). However, the parent
 * channel has no such handler — exceptions propagate to Netty's {@code TailContext}
 * which logs them as WARN ("An exceptionCaught() event was fired, and it reached at
 * the tail of the pipeline").
 * <p>
 * This handler consumes {@link Exception}s on the parent channel and uses connection
 * state to decide the log level:
 * <ul>
 *   <li><b>DEBUG</b> — when {@code activeStreams == 0} OR {@code !channelActive}.
 *       No in-flight requests are affected (e.g., TCP RST from LB idle timeout,
 *       post-close cleanup).</li>
 *   <li><b>WARN</b> — when active streams exist on a live channel, or when the
 *       active stream count cannot be determined. The exception may affect
 *       in-flight requests and is worth alerting on.</li>
 * </ul>
 * <p>
 * {@link Error} types (e.g., {@code OutOfMemoryError}) are never consumed — they
 * propagate to {@code TailContext} for standard Netty handling.
 * <p>
 * The handler does NOT close the channel or alter connection lifecycle — reactor-netty
 * and the connection pool's eviction predicate ({@code !channel.isActive()}) handle that
 * independently.
 *
 * @see ReactorNettyClient#configureChannelPipelineHandlers()
 */
@ChannelHandler.Sharable
final class Http2ParentChannelExceptionHandler extends ChannelInboundHandlerAdapter {

    static final Http2ParentChannelExceptionHandler INSTANCE = new Http2ParentChannelExceptionHandler();

    static final String HANDLER_NAME = "cosmosH2ParentExceptionHandler";

    private static final Logger logger = LoggerFactory.getLogger(Http2ParentChannelExceptionHandler.class);

    private Http2ParentChannelExceptionHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Do not consume JVM-level errors (OOM, StackOverflow, etc.) — let them
        // propagate to TailContext for standard Netty handling.
        if (cause instanceof Error) {
            ctx.fireExceptionCaught(cause);
            return;
        }

        Integer activeStreams = getActiveStreamCount(ctx);
        boolean channelActive = ctx.channel().isActive();

        if ((activeStreams != null && activeStreams == 0) || !channelActive) {
            // No active streams OR channel already inactive — exception is noise
            // (e.g., TCP RST from LB idle timeout, post-close cleanup).
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Exception on HTTP/2 parent connection"
                        + " [channel=" + ctx.channel()
                        + ", activeStreams=" + activeStreams
                        + ", channelActive=" + channelActive
                        + ", clientVmId=" + ClientTelemetry.getCachedMachineId() + "]",
                    cause);
            }
        } else {
            // Active streams on a live channel, or stream count unknown (null) —
            // exception may affect in-flight requests.
            logger.warn(
                "Exception on HTTP/2 parent connection"
                    + " [channel=" + ctx.channel()
                    + ", activeStreams=" + activeStreams
                    + ", channelActive=" + channelActive
                    + ", clientVmId=" + ClientTelemetry.getCachedMachineId() + "]",
                cause);
        }
    }

    private static Integer getActiveStreamCount(ChannelHandlerContext ctx) {
        try {
            Http2FrameCodec codec = ctx.pipeline().get(Http2FrameCodec.class);
            if (codec != null) {
                return codec.connection().numActiveStreams();
            }
        } catch (Exception e) {
            logger.debug("Failed to retrieve active stream count from Http2FrameCodec", e);
        }
        return null;
    }
}
