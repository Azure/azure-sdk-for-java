// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles responding to {@link IdleStateEvent IdleStateEvents} by firing an exception state and closing the channel
 * that is no longer seeing state updates.
 */
public class IdleStateEventHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            System.out.println("Closing idle channel.");
            ctx.close();
        }
    }

    private static final AtomicLong ACTIVE_CHANNEL_COUNT = new AtomicLong();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();

        long activeCount = ACTIVE_CHANNEL_COUNT.incrementAndGet();
        System.out.printf("Channel active, %d active channels.%n", activeCount);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();

        long activeCount = ACTIVE_CHANNEL_COUNT.decrementAndGet();
        System.out.printf("Channel inactive, %d active channels.%n", activeCount);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
        ctx.close(promise);

        long activeCount = ACTIVE_CHANNEL_COUNT.decrementAndGet();
        System.out.printf("Closing channel, %d active channels.%n", activeCount);
    }
}
