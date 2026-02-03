// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Handler that sends a ping frame to the server when the channel is idle to prevent keep-alive timeouts.
 */
public final class WebSocketPingHandler extends ChannelDuplexHandler {
    private static final ClientLogger LOGGER = new ClientLogger(WebSocketPingHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            LOGGER.atVerbose().log("Received IdleStateEvent");
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleStateEvent.ALL_IDLE_STATE_EVENT.state()) {
                LOGGER.atVerbose().log("Sending PingWebSocketFrame");
                ctx.writeAndFlush(new PingWebSocketFrame());
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
