// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class AzureIdleStateHandler extends IdleStateHandler {
    private boolean closed;

    public AzureIdleStateHandler(Duration readTimeout, Duration writeTimeout) {
        super(true, readTimeout.getSeconds(), writeTimeout.getSeconds(), 0, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (!closed) {
            System.out.printf("Processing idle state: %s%n", evt.state());

            if (evt.state() == IdleState.READER_IDLE) {
                ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
            } else if (evt.state() == IdleState.WRITER_IDLE) {
                ctx.fireExceptionCaught(WriteTimeoutException.INSTANCE);
            } else {
                ctx.fireExceptionCaught(new RuntimeException("Unknown idle state."));
            }

            ctx.close();
            closed = true;
        }
    }
}
