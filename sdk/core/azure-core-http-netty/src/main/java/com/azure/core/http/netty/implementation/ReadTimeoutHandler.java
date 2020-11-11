// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Channel handler that watches channel read operations to ensure they aren't timing out.
 */
public final class ReadTimeoutHandler extends ChannelInboundHandlerAdapter {
    /**
     * Name of the handler when it is added into a ChannelPipeline.
     */
    public static final String HANDLER_NAME = "azureReadTimeoutHandler";

    private static final String READ_TIMED_OUT_MESSAGE = "Channel read timed out after %d milliseconds.";

    private final long timeoutMillis;

    private long lastReadMillis;
    private ScheduledFuture<?> readTimeoutWatcher;

    /**
     * Constructs a channel handler that watches channel read operations to ensure they aren't timing out.
     *
     * @param timeoutMillis The period of milliseconds when read progress has stopped before a channel is considered
     * timed out.
     */
    public ReadTimeoutHandler(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        this.lastReadMillis = System.currentTimeMillis();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if (timeoutMillis > 0) {
            this.readTimeoutWatcher = ctx.executor().scheduleAtFixedRate(() -> readTimeoutRunnable(ctx),
                timeoutMillis, timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (readTimeoutWatcher != null && !readTimeoutWatcher.isDone()) {
            readTimeoutWatcher.cancel(false);
            readTimeoutWatcher = null;
        }
    }

    private void readTimeoutRunnable(ChannelHandlerContext ctx) {
        // Channel has completed a read operation since the last time the timeout event fired.
        if ((timeoutMillis - (System.currentTimeMillis() - lastReadMillis)) > 0) {
            return;
        }

        // No progress has been made since the last timeout event, channel has timed out.
        ctx.fireExceptionCaught(new TimeoutException(String.format(READ_TIMED_OUT_MESSAGE, timeoutMillis)));
    }
}
