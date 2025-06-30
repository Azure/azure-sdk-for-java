// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.utils.IOExceptionCheckedConsumer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * {@link ChannelInboundHandler} that eagerly consumes the network response body using a {@link ByteBuf}
 * {@link Consumer}.
 */
public final class Netty4EagerConsumeChannelHandler extends ChannelInboundHandlerAdapter {
    private final CountDownLatch latch;
    private final IOExceptionCheckedConsumer<ByteBuf> byteBufConsumer;
    private final boolean isHttp2;

    private boolean lastRead;
    private Throwable exception;

    /**
     * Creates a new instance of {@link Netty4EagerConsumeChannelHandler}.
     *
     * @param latch The latch to count down when the response is fully read, or an exception occurs.
     * @param byteBufConsumer The consumer to process the {@link ByteBuf ByteBufs} as they are read.
     * @param isHttp2 Flag indicating whether the handler is used for HTTP/2 or not.
     */
    public Netty4EagerConsumeChannelHandler(CountDownLatch latch, IOExceptionCheckedConsumer<ByteBuf> byteBufConsumer,
        boolean isHttp2) {
        this.latch = latch;
        this.byteBufConsumer = byteBufConsumer;
        this.isHttp2 = isHttp2;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            ByteBuf buf = (msg instanceof ByteBufHolder) ? ((ByteBufHolder) msg).content() : (ByteBuf) msg;

            if (buf != null && buf.isReadable()) {
                byteBufConsumer.accept(buf);
            }

            if (isHttp2) {
                lastRead = msg instanceof Http2DataFrame && ((Http2DataFrame) msg).isEndStream();
            } else {
                lastRead = msg instanceof LastHttpContent;
            }
            ctx.fireChannelRead(msg);

        } catch (IOException | RuntimeException ex) {
            ReferenceCountUtil.release(msg);
            ctx.fireExceptionCaught(ex);
            cleanup(ctx);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.fireChannelReadComplete();
        if (lastRead) {
            cleanup(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.exception = cause;
        ctx.fireExceptionCaught(cause);
        cleanup(ctx);
    }

    Throwable channelException() {
        return exception;
    }

    // TODO (alzimmer): Are the latch countdowns needed for unregistering and inactivity?
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        cleanup(ctx);
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        cleanup(ctx);
        ctx.fireChannelInactive();
    }

    private void cleanup(ChannelHandlerContext ctx) {
        if (ctx.pipeline().get(Netty4EagerConsumeChannelHandler.class) != null) {
            ctx.pipeline().remove(this);
        }

        latch.countDown();
    }
}
