// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.http.netty4.mocking.MockChannel;
import io.clientcore.http.netty4.mocking.MockChannelHandlerContext;
import io.clientcore.http.netty4.mocking.MockEventExecutor;
import io.clientcore.http.netty4.mocking.MockUnsafe;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.DefaultEventLoop;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 1, unit = TimeUnit.MINUTES)
public class CoreProgressAndTimeoutHandlerTests {
    @Test
    public void readNoTimeoutDoesNotAddWatcher() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler = new CoreProgressAndTimeoutHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreProgressAndTimeoutHandler.startReadTracking(ctx);

        assertEquals(0, eventExecutor.getScheduleAtFixedRateCallCount());
    }

    @Test
    public void readTimeoutAddsWatcher() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler = new CoreProgressAndTimeoutHandler(null, 0, 0, 1);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreProgressAndTimeoutHandler.startReadTracking(ctx);

        assertEquals(1, eventExecutor.getScheduleAtFixedRateCallCount(1, 1));
    }

    @Test
    public void readRemovingHandlerCancelsTimeout() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler
            = new CoreProgressAndTimeoutHandler(null, 0, 0, 100);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreProgressAndTimeoutHandler.startReadTracking(ctx);
        coreProgressAndTimeoutHandler.handlerRemoved(ctx);

        assertNull(coreProgressAndTimeoutHandler.getReadTimeoutWatcher());
    }

    @Test
    public void readTimesOut() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler
            = new CoreProgressAndTimeoutHandler(null, 0, 0, 100);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        // Fake that the scheduled timer completed before any read operations happened.
        coreProgressAndTimeoutHandler.readTimeoutRunnable(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void readingUpdatesTimeout() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler
            = new CoreProgressAndTimeoutHandler(null, 0, 0, 500);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreProgressAndTimeoutHandler.channelReadComplete(ctx);

        // Fake that the scheduled timer completed before after a read operation happened.
        coreProgressAndTimeoutHandler.readTimeoutRunnable(ctx);

        coreProgressAndTimeoutHandler.handlerRemoved(ctx);

        assertEquals(0, ctx.getFireExceptionCaughtCallCount());
    }

    @Test
    public void responseNoTimeoutDoesNotAddWatcher() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler = new CoreProgressAndTimeoutHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreProgressAndTimeoutHandler.startResponseTracking(ctx);

        assertEquals(0, eventExecutor.getScheduleCallCount());
    }

    @Test
    public void timeoutAddsWatcher() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler = new CoreProgressAndTimeoutHandler(null, 0, 1, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreProgressAndTimeoutHandler.startResponseTracking(ctx);

        assertEquals(1, eventExecutor.getScheduleCallCount(1));
    }

    @Test
    public void responseRemovingHandlerCancelsTimeout() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler
            = new CoreProgressAndTimeoutHandler(null, 0, 100, 0);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreProgressAndTimeoutHandler.startResponseTracking(ctx);
        coreProgressAndTimeoutHandler.handlerRemoved(ctx);

        assertNull(coreProgressAndTimeoutHandler.getResponseTimeoutWatcher());
    }

    @Test
    public void responseTimesOut() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler
            = new CoreProgressAndTimeoutHandler(null, 0, 100, 0);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        // Fake that the scheduled timer completed before any response is received.
        coreProgressAndTimeoutHandler.responseTimedOut(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void writeNoTimeoutDoesNotStartWatcher() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler = new CoreProgressAndTimeoutHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreProgressAndTimeoutHandler.startWriteTracking(ctx);

        assertNull(coreProgressAndTimeoutHandler.getWriteTimeoutWatcher());
    }

    @Test
    public void writeTimeoutAddsWatcher() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler = new CoreProgressAndTimeoutHandler(null, 1, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        coreProgressAndTimeoutHandler.startWriteTracking(ctx);

        assertEquals(1, eventExecutor.getScheduleAtFixedRateCallCount(1, 1));
    }

    @Test
    public void writeRemovingHandlerCancelsTimeout() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler
            = new CoreProgressAndTimeoutHandler(null, 100, 0, 0);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        coreProgressAndTimeoutHandler.startWriteTracking(ctx);
        coreProgressAndTimeoutHandler.handlerRemoved(ctx);

        // When the handler is removed the timer is nulled out.
        assertNull(coreProgressAndTimeoutHandler.getWriteTimeoutWatcher());
    }

    @Test
    public void writeTimesOut() {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler
            = new CoreProgressAndTimeoutHandler(null, 100, 0, 0);

        Channel channel = new MockChannel(new MockUnsafe());
        MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel, new MockEventExecutor());

        // Fake that the scheduled timer completed before any write operations happened.
        coreProgressAndTimeoutHandler.writeTimeoutRunnable(ctx);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void writingUpdatesTimeout() throws Exception {
        CoreProgressAndTimeoutHandler coreProgressAndTimeoutHandler
            = new CoreProgressAndTimeoutHandler(null, 500, 0, 0);

        Channel channel = new MockChannel(new MockUnsafe());
        EventExecutor eventExecutor = new DefaultEventLoop();
        ChannelPromise channelPromise = new DefaultChannelPromise(channel, eventExecutor) {
            @Override
            public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
                try {
                    // Since we're not doing a real write operation, override the ChannelPromise to complete the
                    // listener immediately.
                    listener.operationComplete(null);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                return this;
            }
        };

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel, eventExecutor) {
            @Override
            public ChannelFuture write(Object o, ChannelPromise promise) {
                return channelPromise;
            }
        };

        coreProgressAndTimeoutHandler.startWriteTracking(ctx);

        // Fake that the scheduled timer completed before after a write operation happened.
        coreProgressAndTimeoutHandler.write(ctx, LastHttpContent.EMPTY_LAST_CONTENT, channelPromise);
        coreProgressAndTimeoutHandler.writeTimeoutRunnable(ctx);

        coreProgressAndTimeoutHandler.handlerRemoved(ctx);

        assertEquals(0, ctx.getFireExceptionCaughtCallCount());
    }
}
