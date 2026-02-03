// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.ALPN;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.CHUNKED_WRITER;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.EAGER_CONSUME;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_CODEC;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_RESPONSE;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROGRESS_AND_TIMEOUT;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.READ_ONE;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;

/**
 * A handler that cleans up the pipeline after a request-response cycle and releases
 * the channel back to the connection pool.
 */
public class Netty4PipelineCleanupHandler extends ChannelDuplexHandler {

    private final Netty4ConnectionPool connectionPool;
    private final AtomicReference<Throwable> errorReference;
    private final AtomicBoolean cleanedUp = new AtomicBoolean(false);
    private final Object pipelineOwnerToken;

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

    public Netty4PipelineCleanupHandler(Netty4ConnectionPool connectionPool, AtomicReference<Throwable> errorReference,
        Object pipelineOwnerToken) {
        this.connectionPool = connectionPool;
        this.errorReference = errorReference;
        this.pipelineOwnerToken = pipelineOwnerToken;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        setOrSuppressError(errorReference, cause);
        cleanup(ctx, true);
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        cleanup(ctx, true);
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof Netty4PipelineCleanupEvent) {
            cleanup(ctx, false);
            return;
        }
        ctx.fireUserEventTriggered(evt);
    }

    public void cleanup(ChannelHandlerContext ctx, boolean closeChannel) {
        // Check if this handler is still the rightful owner of the pipeline.
        // If the tokens don't match, a new request has taken over this channel,
        // so this stale cleanup handler must not do anything.
        if (ctx.channel().attr(Netty4ConnectionPool.PIPELINE_OWNER_TOKEN).get() != pipelineOwnerToken) {
            return;
        }

        if (!cleanedUp.compareAndSet(false, true)) {
            return;
        }

        ReentrantLock lock = ctx.channel().attr(Netty4ConnectionPool.CHANNEL_LOCK).get();
        lock.lock();

        try {
            // Always reset autoRead to false before returning a channel to the pool
            // to ensure predictable behavior for the next request.
            ctx.channel().config().setAutoRead(false);

            ChannelPipeline pipeline = ctx.channel().pipeline();

            HttpProtocolVersion protocolVersion = ctx.channel().attr(Netty4AlpnHandler.HTTP_PROTOCOL_VERSION_KEY).get();
            boolean isHttp2 = protocolVersion == HttpProtocolVersion.HTTP_2;

            for (String handlerName : HANDLERS_TO_REMOVE) {
                if (isHttp2 && HTTP_CODEC.equals(handlerName)) {
                    continue;
                }

                if (pipeline.get(handlerName) != null) {
                    pipeline.remove(handlerName);
                }
            }

            if (pipeline.get(Netty4PipelineCleanupHandler.class) != null) {
                pipeline.remove(this);
            }
        } finally {
            lock.unlock();
        }

        if (closeChannel || !ctx.channel().isActive() || connectionPool == null) {
            ctx.channel().close();
        } else {
            connectionPool.release(ctx.channel());
        }
    }
}
