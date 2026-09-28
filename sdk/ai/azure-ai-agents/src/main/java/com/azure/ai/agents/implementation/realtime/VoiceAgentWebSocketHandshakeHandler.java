// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Observes the HTTP response used for a WebSocket upgrade and reports rejected handshakes.
 */
public final class VoiceAgentWebSocketHandshakeHandler extends ChannelInboundHandlerAdapter {
    private final Consumer<Throwable> errorConsumer;
    private ByteArrayOutputStream responseBody;
    private HttpResponse rejectedResponse;

    /**
     * Creates a handshake response observer.
     *
     * @param errorConsumer consumer invoked when the server rejects the upgrade.
     */
    public VoiceAgentWebSocketHandshakeHandler(Consumer<Throwable> errorConsumer) {
        this.errorConsumer = Objects.requireNonNull(errorConsumer, "'errorConsumer' cannot be null.");
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws IOException {
        if (message instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) message;
            if (!HttpResponseStatus.SWITCHING_PROTOCOLS.equals(response.status())) {
                rejectedResponse = response;
            }
        }
        if (rejectedResponse != null && message instanceof HttpContent) {
            HttpContent content = (HttpContent) message;
            ByteBuf byteBuf = content.content();
            if (byteBuf != null && byteBuf.isReadable()) {
                if (responseBody == null) {
                    responseBody = new ByteArrayOutputStream();
                }
                byteBuf.readBytes(responseBody, byteBuf.readableBytes());
            }
            if (message instanceof LastHttpContent) {
                byte[] body = responseBody == null ? new byte[0] : responseBody.toByteArray();
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(rejectedResponse.protocolVersion(),
                    rejectedResponse.status(), Unpooled.wrappedBuffer(body));
                response.headers().set(rejectedResponse.headers());
                errorConsumer
                    .accept(new WebSocketClientHandshakeException("Voice-agent WebSocket handshake failed.", response));
                context.close();
            }
        }
        context.fireChannelRead(message);
    }
}
