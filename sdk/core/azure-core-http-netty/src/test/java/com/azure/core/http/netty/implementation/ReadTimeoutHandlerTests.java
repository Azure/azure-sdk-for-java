// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.netty.mocking.MockChannelHandlerContext;
import com.azure.core.http.netty.mocking.MockEventExecutor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultEventExecutor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ReadTimeoutHandler}.
 */
public class ReadTimeoutHandlerTests {
    @Test
    public void noTimeoutDoesNotAddWatcher() {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        readTimeoutHandler.handlerAdded(ctx);

        assertEquals(0, eventExecutor.getScheduleAtFixedRateCallCount());
    }

    @Test
    public void timeoutAddsWatcher() {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(1);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        readTimeoutHandler.handlerAdded(ctx);

        assertEquals(1, eventExecutor.getScheduleAtFixedRateCallCount(1, 1));
    }

    @Test
    public void removingHandlerCancelsTimeout() {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(100);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        readTimeoutHandler.handlerAdded(ctx);
        readTimeoutHandler.handlerRemoved(ctx);

        assertNull(readTimeoutHandler.getReadTimeoutWatcher());
    }

    @Test
    public void readTimesOut() {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(100);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        readTimeoutHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before any read operations happened.
        readTimeoutHandler.readTimeoutRunnable(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void readingUpdatesTimeout() {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(500);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        readTimeoutHandler.handlerAdded(ctx);

        readTimeoutHandler.channelReadComplete(ctx);

        // Fake that the scheduled timer completed before after a read operation happened.
        readTimeoutHandler.readTimeoutRunnable(ctx);

        readTimeoutHandler.handlerRemoved(ctx);

        assertEquals(0, ctx.getFireExceptionCaughtCallCount());
    }
}
