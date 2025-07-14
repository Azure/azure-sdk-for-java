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
        ByteBuf buf = null;
        if (msg instanceof ByteBufHolder) {
            buf = ((ByteBufHolder) msg).content();
        } else if (msg instanceof ByteBuf) {
            buf = (ByteBuf) msg;
        }

        if (buf != null && buf.isReadable()) {
            try {
                byteBufConsumer.accept(buf);
            } catch (IOException | RuntimeException ex) {
                ReferenceCountUtil.release(buf);
                ctx.close();
                return;
            }
        }

        if (isHttp2) {
            lastRead = msg instanceof Http2DataFrame && ((Http2DataFrame) msg).isEndStream();
        } else {
            lastRead = msg instanceof LastHttpContent;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.fireChannelReadComplete();
        if (lastRead) {
            latch.countDown();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.exception = cause;
        latch.countDown();
        ctx.close();
    }

    Throwable channelException() {
        return exception;
    }

    // TODO (alzimmer): Are the latch countdowns needed for unregistering and inactivity?
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        latch.countDown();
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        latch.countDown();
        ctx.fireChannelInactive();
    }
}
