// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty.implementation;

import io.clientcore.http.netty.mocking.MockChannel;
import io.clientcore.http.netty.mocking.MockChannelHandlerContext;
import io.clientcore.http.netty.mocking.MockEventExecutor;
import io.clientcore.http.netty.mocking.MockUnsafe;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoreHandlerTests {
    @Test
    public void readNoTimeoutDoesNotAddWatcher() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreHandler.handlerAdded(ctx);
        coreHandler.startReadTracking();

        assertEquals(0, eventExecutor.getScheduleAtFixedRateCallCount());
    }

    @Test
    public void readTimeoutAddsWatcher() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 0, 1);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreHandler.handlerAdded(ctx);
        coreHandler.startReadTracking();

        assertEquals(1, eventExecutor.getScheduleAtFixedRateCallCount(1, 1));
    }

    @Test
    public void readRemovingHandlerCancelsTimeout() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 0, 100);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreHandler.handlerAdded(ctx);
        coreHandler.startReadTracking();
        coreHandler.handlerRemoved(ctx);

        assertNull(coreHandler.getReadTimeoutWatcher());
    }

    @Test
    public void readTimesOut() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 0, 100);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before any read operations happened.
        coreHandler.readTimeoutRunnable(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void readingUpdatesTimeout() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 0, 500);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreHandler.handlerAdded(ctx);

        coreHandler.channelReadComplete(ctx);

        // Fake that the scheduled timer completed before after a read operation happened.
        coreHandler.readTimeoutRunnable(ctx);

        coreHandler.handlerRemoved(ctx);

        assertEquals(0, ctx.getFireExceptionCaughtCallCount());
    }

    @Test
    public void responseNoTimeoutDoesNotAddWatcher() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreHandler.handlerAdded(ctx);
        coreHandler.startResponseTracking();

        assertEquals(0, eventExecutor.getScheduleCallCount());
    }

    @Test
    public void timeoutAddsWatcher() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 1, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreHandler.handlerAdded(ctx);
        coreHandler.startResponseTracking();

        assertEquals(1, eventExecutor.getScheduleCallCount(1));
    }

    @Test
    public void responseRemovingHandlerCancelsTimeout() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 100, 0);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreHandler.handlerAdded(ctx);
        coreHandler.startResponseTracking();
        coreHandler.handlerRemoved(ctx);

        assertNull(coreHandler.getResponseTimeoutWatcher());
    }

    @Test
    public void responseTimesOut() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 100, 0);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before any response is received.
        coreHandler.responseTimedOut(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void writeNoTimeoutDoesNotStartWatcher() {
        CoreHandler coreHandler = new CoreHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreHandler.handlerAdded(ctx);
        coreHandler.startWriteTracking();

        assertNull(coreHandler.getWriteTimeoutWatcher());
    }

    @Test
    public void writeTimeoutAddsWatcher() {
        CoreHandler coreHandler = new CoreHandler(null, 1, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreHandler.handlerAdded(ctx);
        coreHandler.startWriteTracking();

        assertEquals(1, eventExecutor.getScheduleAtFixedRateCallCount(1, 1));
    }

    @Test
    public void writeRemovingHandlerCancelsTimeout() {
        CoreHandler coreHandler = new CoreHandler(null, 100, 0, 0);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreHandler.handlerAdded(ctx);
        coreHandler.startWriteTracking();
        coreHandler.handlerRemoved(ctx);

        // When the handler is removed the timer is nulled out.
        assertNull(coreHandler.getWriteTimeoutWatcher());
    }

    @Test
    public void writeTimesOut() {
        CoreHandler coreHandler = new CoreHandler(null, 100, 0, 0);

        Channel channel = new MockChannel(new MockUnsafe());
        MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel, new MockEventExecutor());

        coreHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before any write operations happened.
        coreHandler.writeTimeoutRunnable(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void writingUpdatesTimeout() throws Exception {
        CoreHandler coreHandler = new CoreHandler(null, 500, 0, 0);

        Channel channel = new MockChannel(new MockUnsafe());
        EventExecutor eventExecutor = new DefaultEventExecutor();
        ChannelPromise channelPromise = new DefaultChannelPromise(channel, eventExecutor);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel, eventExecutor) {
            @Override
            public ChannelFuture write(Object o, ChannelPromise promise) {
                return channelPromise;
            }
        };

        coreHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before after a write operation happened.
        coreHandler.write(ctx, LastHttpContent.EMPTY_LAST_CONTENT, channelPromise);
        coreHandler.writeTimeoutRunnable(ctx);

        coreHandler.handlerRemoved(ctx);

        assertEquals(0, ctx.getFireExceptionCaughtCallCount());
    }
}
