// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle data from remote.
 *
 */
public class HttpProxyRemoteHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(HttpProxyRemoteHandler.class);
    private final String id;
    private Channel clientChannel;
    private Channel remoteChannel;

    public HttpProxyRemoteHandler(String id, Channel clientChannel) {
        this.id = id;
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.remoteChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (clientChannel != null && clientChannel.isActive()) {
            clientChannel.writeAndFlush(msg).addListener(f -> {
                if (!f.isSuccess()) {
                    // transport is broken; close both sides
                    flushAndClose(clientChannel);
                    flushAndClose(remoteChannel);
                }
            });
        } else {
            io.netty.util.ReferenceCountUtil.safeRelease(msg); // prevent leak
            flushAndClose(remoteChannel);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        flushAndClose(clientChannel);
        remoteChannel = null;
        clientChannel = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error(id + " error occurred", e);
        flushAndClose(remoteChannel);
        flushAndClose(clientChannel);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (remoteChannel != null) {
            boolean writable = clientChannel != null && clientChannel.isWritable();
            remoteChannel.config().setAutoRead(writable);
        }
        ctx.fireChannelWritabilityChanged();
    }

    private void flushAndClose(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
