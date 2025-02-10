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
 * Tests {@link ResponseTimeoutHandler}.
 */
public class ResponseTimeoutHandlerTests {
    @Test
    public void noTimeoutDoesNotAddWatcher() {
        ResponseTimeoutHandler responseTimeoutHandler = new ResponseTimeoutHandler(0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        responseTimeoutHandler.handlerAdded(ctx);

        assertEquals(0, eventExecutor.getScheduleCallCount());
    }

    @Test
    public void timeoutAddsWatcher() {
        ResponseTimeoutHandler responseTimeoutHandler = new ResponseTimeoutHandler(1);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        responseTimeoutHandler.handlerAdded(ctx);

        assertEquals(1, eventExecutor.getScheduleCallCount(1));
    }

    @Test
    public void removingHandlerCancelsTimeout() {
        ResponseTimeoutHandler responseTimeoutHandler = new ResponseTimeoutHandler(100);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        responseTimeoutHandler.handlerAdded(ctx);
        responseTimeoutHandler.handlerRemoved(ctx);

        assertNull(responseTimeoutHandler.getResponseTimeoutWatcher());
    }

    @Test
    public void responseTimesOut() {
        ResponseTimeoutHandler responseTimeoutHandler = new ResponseTimeoutHandler(100);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        responseTimeoutHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before any response is received.
        responseTimeoutHandler.responseTimedOut(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }
}
