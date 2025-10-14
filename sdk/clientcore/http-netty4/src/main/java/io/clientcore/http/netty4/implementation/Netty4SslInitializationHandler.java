// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;

/**
 * {@link ChannelInboundHandler} implementation that manages the SSL handshake process.
 */
public final class Netty4SslInitializationHandler extends ChannelInboundHandlerAdapter {
    private boolean sslHandshakeComplete;

    /**
     * Creates a new instance of {@link Netty4SslInitializationHandler}.
     */
    public Netty4SslInitializationHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Eagerly request the SSL handshake response to be read.
        ctx.read();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        if (!sslHandshakeComplete) {
            // SSL handshake hasn't completed, continue reading from the network until it completes.
            ctx.read();
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof SslHandshakeCompletionEvent) {
            sslHandshakeComplete = true;

            // Remove this handler as the SSL connection has been established as this doesn't need to exist anymore.
            ctx.pipeline().remove(this);

            SslHandshakeCompletionEvent handshake = (SslHandshakeCompletionEvent) evt;
            if (handshake.isSuccess()) {
                // SSL handshake completed successfully, notify the rest of the pipeline that the channel is active
                // for further processing.
                ctx.fireChannelActive();
            } else {
                // SSL handshake failed, notify the rest of the pipeline about the failure.
                ctx.fireExceptionCaught(handshake.cause());
            }
        }
        ctx.fireUserEventTriggered(evt);
    }
}
