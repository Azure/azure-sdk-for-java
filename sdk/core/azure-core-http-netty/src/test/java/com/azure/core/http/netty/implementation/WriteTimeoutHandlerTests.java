// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.netty.mocking.MockChannel;
import com.azure.core.http.netty.mocking.MockChannelHandlerContext;
import com.azure.core.http.netty.mocking.MockEventExecutor;
import com.azure.core.http.netty.mocking.MockUnsafe;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link WriteTimeoutHandler}.
 */
public class WriteTimeoutHandlerTests {
    @Test
    public void noTimeoutDoesNotAddWatcher() {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        writeTimeoutHandler.handlerAdded(ctx);

        assertEquals(0, eventExecutor.getScheduleAtFixedRateCallCount());
    }

    @Test
    public void timeoutAddsWatcher() {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(1);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        writeTimeoutHandler.handlerAdded(ctx);

        assertEquals(1, eventExecutor.getScheduleAtFixedRateCallCount(1, 1));
    }

    @Test
    public void removingHandlerCancelsTimeout() {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(100);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        writeTimeoutHandler.handlerAdded(ctx);
        writeTimeoutHandler.handlerRemoved(ctx);

        // When the handler is removed the timer is nulled out.
        assertNull(writeTimeoutHandler.getWriteTimeoutWatcher());
    }

    @Test
    public void writeTimesOut() {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(100);

        Channel channel = new MockChannel(new MockUnsafe());
        MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel, new MockEventExecutor());

        writeTimeoutHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before any write operations happened.
        writeTimeoutHandler.writeTimeoutRunnable(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void writingUpdatesTimeout() throws Exception {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(500);

        Channel channel = new MockChannel(new MockUnsafe());
        EventExecutor eventExecutor = new DefaultEventExecutor();
        ChannelPromise channelPromise = new DefaultChannelPromise(channel, eventExecutor);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel, eventExecutor) {
            @Override
            public ChannelFuture write(Object o, ChannelPromise promise) {
                return channelPromise;
            }
        };

        writeTimeoutHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before after a write operation happened.
        writeTimeoutHandler.getWriteListener().operationComplete(null);
        writeTimeoutHandler.writeTimeoutRunnable(ctx);

        writeTimeoutHandler.handlerRemoved(ctx);

        assertEquals(0, ctx.getFireExceptionCaughtCallCount());
    }
}
