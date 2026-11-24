// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Handle data from client.
 *
 */
public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {

    private static final ByteBuf TUNNEL_OK =
        Unpooled.unreleasableBuffer(
            Unpooled.copiedBuffer("HTTP/1.1 200 Connection Established\r\n\r\n", StandardCharsets.US_ASCII));

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
            if (remoteChannel != null && remoteChannel.isActive()) {
                remoteChannel.writeAndFlush(msg); // just forward
            } else {
                ReferenceCountUtil.safeRelease(msg);
                flushAndClose(clientChannel);
            }
            return;
        }

        ByteBuf in = (ByteBuf) msg;

        try {
            header.digest(in);
        } catch (Throwable t) {
            ReferenceCountUtil.safeRelease(in);
            flushAndClose(clientChannel);
            throw t;
        }

        if (!header.isComplete()) {
            ReferenceCountUtil.safeRelease(in);
            return;
        }

        logger.info(id + " {}", header);
        clientChannel.config().setAutoRead(false); // disable AutoRead until remote connection is ready

        if (header.isHttps()) { // if https, respond 200 to create tunnel
            clientChannel.writeAndFlush(TUNNEL_OK.duplicate());
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
                ReferenceCountUtil.safeRelease(in);
                // release header buffer if retained/allocated
                ReferenceCountUtil.safeRelease(header.getByteBuf());
                flushAndClose(remoteChannel);
                flushAndClose(clientChannel);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        flushAndClose(remoteChannel);
        remoteChannel = null;
        clientChannel = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.error(id + " error occurred", e);
        flushAndClose(remoteChannel);
        flushAndClose(clientChannel);
    }

    private void flushAndClose(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
