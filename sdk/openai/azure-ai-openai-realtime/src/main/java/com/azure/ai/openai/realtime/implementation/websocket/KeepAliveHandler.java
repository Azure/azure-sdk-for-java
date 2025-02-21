// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

/**
 * Dedicated handler for server-side ping messages.
 */
public final class KeepAliveHandler extends ChannelDuplexHandler {
    private static final ClientLogger LOGGER = new ClientLogger(KeepAliveHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw LOGGER.logExceptionAsError(new IllegalStateException("Unexpected FullHttpResponse (getStatus="
                + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')'));
        }
        Channel ch = ctx.channel();

        LOGGER.atVerbose().log("Processing message: ");
        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof PingWebSocketFrame) {
            // Ping, reply Pong
            LOGGER.atVerbose().log(() -> "Received PingWebSocketFrame");
            LOGGER.atVerbose().log(() -> "Sending PongWebSocketFrame");
            ch.writeAndFlush(new PongWebSocketFrame(frame.copy().content()));
        } else if (frame instanceof PongWebSocketFrame) {
            // Pong
            LOGGER.atVerbose().log(() -> "Received PongWebSocketFrame");
        } else {
            // Pass other frames down the pipeline
            // We only pass down the pipeline messages this handler doesn't process
            ctx.fireChannelRead(msg);
        }

    }
}
