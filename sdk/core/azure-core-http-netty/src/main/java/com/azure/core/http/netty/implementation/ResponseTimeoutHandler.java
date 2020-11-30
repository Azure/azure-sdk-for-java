// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Channel handler that watches that the channel receives a response within a given timeout period.
 */
public final class ResponseTimeoutHandler extends ChannelInboundHandlerAdapter {
    /**
     * Name of the handler when it is added into a ChannelPipeline.
     */
    public static final String HANDLER_NAME = "azureResponseTimeoutHandler";

    private static final String RESPONSE_TIMED_OUT_MESSAGE = "Channel response timed out after %d milliseconds.";

    private final long timeoutMillis;

    private boolean closed;
    private ScheduledFuture<?> responseTimeoutWatcher;

    /**
     * Constructs a channel that watches that the channel receives a response within a given timeout period.
     *
     * @param timeoutMillis The period of milliseconds before a channel's response is considered timed out.
     */
    public ResponseTimeoutHandler(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if (timeoutMillis > 0) {
            this.responseTimeoutWatcher = ctx.executor().schedule(() -> responseTimedOut(ctx), timeoutMillis,
                TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        disposeWatcher();
    }

    private void responseTimedOut(ChannelHandlerContext ctx) {
        if (!closed) {
            disposeWatcher();
            ctx.fireExceptionCaught(new TimeoutException(String.format(RESPONSE_TIMED_OUT_MESSAGE, timeoutMillis)));
            ctx.close();
            closed = true;
        }
    }

    private void disposeWatcher() {
        if (responseTimeoutWatcher != null && !responseTimeoutWatcher.isDone()) {
            responseTimeoutWatcher.cancel(false);
            responseTimeoutWatcher = null;
        }
    }
}
