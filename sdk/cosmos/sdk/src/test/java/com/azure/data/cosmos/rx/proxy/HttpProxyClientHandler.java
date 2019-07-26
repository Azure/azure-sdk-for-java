// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle data from client.
 *
 */
public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(HttpProxyClientHandler.class);
    private final String id;
    private Channel clientChannel;
    private Channel remoteChannel;
    private HttpProxyClientHeader header ;
    public HttpProxyClientHandler(String id) {
        this.id = id;
        header = new HttpProxyClientHeader();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clientChannel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (header.isComplete()) {
            remoteChannel.writeAndFlush(msg); // just forward
            return;
        }

        ByteBuf in = (ByteBuf) msg;
        header.digest(in);

        if (!header.isComplete()) {
            in.release();
            return;
        }

        logger.info(id + " {}", header);
        clientChannel.config().setAutoRead(false); // disable AutoRead until remote connection is ready

        if (header.isHttps()) { // if https, respond 200 to create tunnel
            clientChannel.writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes()));
        }

        Bootstrap b = new Bootstrap();
        b.group(clientChannel.eventLoop()) // use the same EventLoop
                .channel(clientChannel.getClass())
                .handler(new HttpProxyRemoteHandler(id, clientChannel));
        ChannelFuture f = b.connect(header.getHost(), header.getPort());
        remoteChannel = f.channel();

        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                clientChannel.config().setAutoRead(true); // connection is ready, enable AutoRead
                if (!header.isHttps()) { // forward header and remaining bytes
                    remoteChannel.write(header.getByteBuf());
                }

                remoteChannel.writeAndFlush(in);
            } else {
                in.release();
                clientChannel.close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        flushAndClose(remoteChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error(id + " error occured", e);
        flushAndClose(clientChannel);
    }

    private void flushAndClose(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
