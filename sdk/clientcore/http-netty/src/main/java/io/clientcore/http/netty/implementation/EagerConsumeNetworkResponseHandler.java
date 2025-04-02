// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty.implementation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * {@link ChannelInboundHandlerAdapter} that eagerly consumes the network response body using a {@link ByteBuf}
 * {@link Consumer}.
 */
public final class EagerConsumeNetworkResponseHandler extends ChannelInboundHandlerAdapter {
    private final CountDownLatch latch;
    private final Consumer<ByteBuf> byteBufConsumer;

    private boolean lastRead;

    /**
     * Creates a new instance of {@link EagerConsumeNetworkResponseHandler}.
     *
     * @param latch The latch to count down when the response is fully read, or an exception occurs.
     * @param byteBufConsumer The consumer to process the {@link ByteBuf ByteBufs} as they are read.
     */
    public EagerConsumeNetworkResponseHandler(CountDownLatch latch, Consumer<ByteBuf> byteBufConsumer) {
        this.latch = latch;
        this.byteBufConsumer = byteBufConsumer;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // We're going to eagerly read the remaining data from the network now, enable autoRead.
        HttpClientCodec codec = ctx.pipeline().get(HttpClientCodec.class);
        if (codec != null) {
            // Disable singleDecode if the HttpClientCodec exists to allow it to read the entire response.
            codec.setSingleDecode(false);
        }
        ctx.channel().read();
        ctx.channel().config().setAutoRead(true);
        super.handlerAdded(ctx);
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
        if (lastRead) {
            latch.countDown();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        latch.countDown();
        ctx.fireExceptionCaught(cause);
    }

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

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }
}
