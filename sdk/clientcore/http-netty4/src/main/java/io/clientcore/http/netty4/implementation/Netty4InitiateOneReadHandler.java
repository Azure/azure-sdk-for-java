// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * {@link ChannelInboundHandler} that initiates one read request any time data is needed. Even though it is a single
 * read request it may result in multiple {@link #channelRead(ChannelHandlerContext, Object)} invocations. Consumers of
 * this handler need to support multiple channelRead, if this isn't done data may be lost.
 */
public final class Netty4InitiateOneReadHandler extends ChannelInboundHandlerAdapter {
    private final CountDownLatch latch;
    private final Consumer<ByteBuf> byteBufConsumer;

    private boolean lastRead;

    /**
     * Creates a new instance of {@link Netty4InitiateOneReadHandler}.
     * <p>
     * The passed {@link CountDownLatch} will count down when {@link #channelReadComplete(ChannelHandlerContext)} fires.
     * This indicates that the single read operation completed, it doesn't necessarily mean that the channel was fully
     * read.
     *
     * @param latch The latch to count down when the channel read completes.
     * @param byteBufConsumer The consumer to process the {@link ByteBuf ByteBufs} as they are read.
     */
    public Netty4InitiateOneReadHandler(CountDownLatch latch, Consumer<ByteBuf> byteBufConsumer) {
        this.latch = latch;
        this.byteBufConsumer = byteBufConsumer;
    }

    @Override
    public boolean isSharable() {
        return false;
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
            byteBufConsumer.accept(buf);
        }

        lastRead = msg instanceof LastHttpContent;
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.fireChannelReadComplete();
        latch.countDown();
        if (lastRead) {
            ctx.close();
        }
    }

    boolean isChannelConsumed() {
        return lastRead;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        latch.countDown();
        ctx.fireExceptionCaught(cause);
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
