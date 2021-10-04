// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Channel handler that watches channel write operations to ensure they aren't timing out.
 */
public final class WriteTimeoutHandler extends ChannelOutboundHandlerAdapter {
    /**
     * Name of the handler when it is added into a ChannelPipeline.
     */
    public static final String HANDLER_NAME = "azureWriteTimeoutHandler";

    private static final String WRITE_TIMED_OUT_MESSAGE = "Channel write operation timed out after %d milliseconds.";

    final ChannelFutureListener writeListener = (future) -> this.lastWriteMillis = System.currentTimeMillis();

    private final long timeoutMillis;

    private boolean closed;
    private long lastWriteMillis;
    private long lastWriteProgress;
    ScheduledFuture<?> writeTimeoutWatcher;

    /**
     * Constructs a channel handler that watches channel write operations to ensure they aren't timing out.
     *
     * @param timeoutMillis The period of milliseconds when write progress has stopped before a channel is considered
     * timed out.
     */
    public WriteTimeoutHandler(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.write(msg, promise.unvoid()).addListener(writeListener);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if (timeoutMillis > 0) {
            this.writeTimeoutWatcher = ctx.executor().scheduleAtFixedRate(() -> writeTimeoutRunnable(ctx),
                timeoutMillis, timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        disposeWatcher();
    }

    void writeTimeoutRunnable(ChannelHandlerContext ctx) {
        // Channel has completed a write operation since the last time the timeout event fired.
        if ((timeoutMillis - (System.currentTimeMillis() - lastWriteMillis)) > 0) {
            return;
        }

        ChannelOutboundBuffer buffer = ctx.channel().unsafe().outboundBuffer();

        // Channel has an outbound buffer, check if the write progress has changed.
        if (buffer != null) {
            long writeProgress = buffer.currentProgress();

            // Write progress has changed since the last time the timeout event fired.
            if (writeProgress != lastWriteProgress) {
                this.lastWriteProgress = writeProgress;
                return;
            }
        }

        // No progress has been made since the last timeout event, channel has timed out.
        if (!closed) {
            disposeWatcher();
            ctx.fireExceptionCaught(new TimeoutException(String.format(WRITE_TIMED_OUT_MESSAGE, timeoutMillis)));
            ctx.close();
            closed = true;
        }
    }

    private void disposeWatcher() {
        if (writeTimeoutWatcher != null && !writeTimeoutWatcher.isDone()) {
            writeTimeoutWatcher.cancel(false);
            writeTimeoutWatcher = null;
        }
    }
}
