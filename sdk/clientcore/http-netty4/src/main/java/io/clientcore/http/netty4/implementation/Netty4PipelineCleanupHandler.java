// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.ALPN;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.CHUNKED_WRITER;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.EAGER_CONSUME;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_CODEC;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_RESPONSE;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROGRESS_AND_TIMEOUT;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.READ_ONE;

/**
 * A handler that cleans up the pipeline after a request-response cycle and releases
 * the channel back to the connection pool.
 */
public class Netty4PipelineCleanupHandler extends ChannelDuplexHandler {

    private final Netty4ConnectionPool connectionPool;
    private final AtomicBoolean cleanedUp = new AtomicBoolean(false);

    private static final List<String> HANDLERS_TO_REMOVE;

    static {
        List<String> handlers = new ArrayList<>();
        handlers.add(PROGRESS_AND_TIMEOUT);
        handlers.add(HTTP_RESPONSE);
        handlers.add(HTTP_CODEC);
        handlers.add(ALPN);
        handlers.add(CHUNKED_WRITER);
        handlers.add(EAGER_CONSUME);
        handlers.add(READ_ONE);
        HANDLERS_TO_REMOVE = Collections.unmodifiableList(handlers);
    }

    public Netty4PipelineCleanupHandler(Netty4ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //        if (msg instanceof LastHttpContent) {
        //            this.lastContentRead = true;
        //        } else if (msg instanceof Http2DataFrame) {
        //            this.lastContentRead = ((Http2DataFrame) msg).isEndStream();
        //        }

        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // First, let other handlers process the channelReadComplete event.
        ctx.fireChannelReadComplete();

        //        if (lastContentRead) {
        //            // Schedule cleanup to run after the current event processing is complete
        //            // This prevents modifying the pipeline while it's still being traversed
        //            ctx.channel().eventLoop().execute(() -> cleanup(ctx));
        //        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // An exception has occurred, which means the channel is likely in a bad state.
        // We handle this by closing the channel. This prevents it from being
        // returned to the connection pool.
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        ctx.channel().eventLoop().execute(() -> cleanup(ctx));
    }

    /**
     * Called externally when the request/response cycle is complete.
     * This should be called by the code that manages the request lifecycle,
     * not by detecting last content in the pipeline.
     */
    public void requestComplete(ChannelHandlerContext ctx) {
        cleanup(ctx);
    }

    private void cleanup(ChannelHandlerContext ctx) {
        if (!cleanedUp.compareAndSet(false, true)) {
            return;
        }

        ChannelPipeline pipeline = ctx.channel().pipeline();
        for (String handlerName : HANDLERS_TO_REMOVE) {
            if (pipeline.get(handlerName) != null) {
                pipeline.remove(handlerName);
            }
        }

        if (pipeline.get(Netty4PipelineCleanupHandler.class) != null) {
            pipeline.remove(this);
        }

        connectionPool.release(ctx.channel());
    }
}
