// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Observes the HTTP response used for a WebSocket upgrade and reports rejected handshakes.
 */
public final class VoiceAgentWebSocketHandshakeHandler extends ChannelInboundHandlerAdapter {
    private final Consumer<Throwable> errorConsumer;

    /**
     * Creates a handshake response observer.
     *
     * @param errorConsumer consumer invoked when the server rejects the upgrade.
     */
    public VoiceAgentWebSocketHandshakeHandler(Consumer<Throwable> errorConsumer) {
        this.errorConsumer = Objects.requireNonNull(errorConsumer, "'errorConsumer' cannot be null.");
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) {
        if (message instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) message;
            if (!HttpResponseStatus.SWITCHING_PROTOCOLS.equals(response.status())) {
                errorConsumer
                    .accept(new WebSocketClientHandshakeException("Voice-agent WebSocket handshake failed.", response));
                context.close();
            }
        }
        context.fireChannelRead(message);
    }
}
