// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.http.netty4.mocking.MockChannel;
import io.clientcore.http.netty4.mocking.MockChannelHandlerContext;
import io.clientcore.http.netty4.mocking.MockEventExecutor;
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

@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class Netty4ProgressAndTimeoutHandlerTests {
    @Test
    public void readNoTimeoutDoesNotAddWatcher() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        netty4ProgressAndTimeoutHandler.startReadTracking(ctx);

        assertEquals(0, eventExecutor.getScheduleAtFixedRateCallCount());
    }

    @Test
    public void readTimeoutAddsWatcher() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 0, 1);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        netty4ProgressAndTimeoutHandler.startReadTracking(ctx);

        assertEquals(1, eventExecutor.getScheduleAtFixedRateCallCount(1, 1));
    }

    @Test
    public void readRemovingHandlerCancelsTimeout() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 0, 100);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        netty4ProgressAndTimeoutHandler.startReadTracking(ctx);
        netty4ProgressAndTimeoutHandler.handlerRemoved(ctx);

        assertNull(netty4ProgressAndTimeoutHandler.getReadTimeoutWatcher());
    }

    @Test
    public void readTimesOut() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 0, 100);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        // Fake that the scheduled timer completed before any read operations happened.
        netty4ProgressAndTimeoutHandler.readTimeoutRunnable(ctx, true);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void readingUpdatesTimeout() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 0, 500);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        netty4ProgressAndTimeoutHandler.channelReadComplete(ctx);

        // Fake that the scheduled timer completed before after a read operation happened.
        netty4ProgressAndTimeoutHandler.readTimeoutRunnable(ctx, true);

        netty4ProgressAndTimeoutHandler.handlerRemoved(ctx);

        assertEquals(0, ctx.getFireExceptionCaughtCallCount());
    }

    @Test
    public void responseNoTimeoutDoesNotAddWatcher() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        netty4ProgressAndTimeoutHandler.startResponseTracking(ctx);

        assertEquals(0, eventExecutor.getScheduleCallCount());
    }

    @Test
    public void timeoutAddsWatcher() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 1, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        netty4ProgressAndTimeoutHandler.startResponseTracking(ctx);

        assertEquals(1, eventExecutor.getScheduleCallCount(1));
    }

    @Test
    public void responseRemovingHandlerCancelsTimeout() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 100, 0);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        netty4ProgressAndTimeoutHandler.startResponseTracking(ctx);
        netty4ProgressAndTimeoutHandler.handlerRemoved(ctx);

        assertNull(netty4ProgressAndTimeoutHandler.getResponseTimeoutWatcher());
    }

    @Test
    public void responseTimesOut() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 100, 0);

        MockChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        // Fake that the scheduled timer completed before any response is received.
        netty4ProgressAndTimeoutHandler.responseTimedOut(ctx, true);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void writeNoTimeoutDoesNotStartWatcher() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 0, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        netty4ProgressAndTimeoutHandler.startWriteTracking(ctx);

        assertNull(netty4ProgressAndTimeoutHandler.getWriteTimeoutWatcher());
    }

    @Test
    public void writeTimeoutAddsWatcher() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 1, 0, 0);

        MockEventExecutor eventExecutor = new MockEventExecutor();
        ChannelHandlerContext ctx = new MockChannelHandlerContext(eventExecutor);

        netty4ProgressAndTimeoutHandler.startWriteTracking(ctx);

        assertEquals(1, eventExecutor.getScheduleAtFixedRateCallCount(1, 1));
    }

    @Test
    public void writeRemovingHandlerCancelsTimeout() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 100, 0, 0);

        ChannelHandlerContext ctx = new MockChannelHandlerContext(new DefaultEventExecutor());

        netty4ProgressAndTimeoutHandler.startWriteTracking(ctx);
        netty4ProgressAndTimeoutHandler.handlerRemoved(ctx);

        // When the handler is removed the timer is nulled out.
        assertNull(netty4ProgressAndTimeoutHandler.getWriteTimeoutWatcher());
    }

    @Test
    public void writeTimesOut() {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 100, 0, 0);

        Channel channel = new MockChannel();
        MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel, new MockEventExecutor());

        // Fake that the scheduled timer completed before any write operations happened.
        netty4ProgressAndTimeoutHandler.writeTimeoutRunnable(ctx, true);

        assertTrue(ctx.getFireExceptionCaughtCallCount() >= 1);
    }

    @Test
    public void writingUpdatesTimeout() throws Exception {
        Netty4ProgressAndTimeoutHandler netty4ProgressAndTimeoutHandler
            = new Netty4ProgressAndTimeoutHandler(null, 500, 0, 0);

        Channel channel = new MockChannel();
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

        netty4ProgressAndTimeoutHandler.startWriteTracking(ctx);

        // Fake that the scheduled timer completed before after a write operation happened.
        netty4ProgressAndTimeoutHandler.write(ctx, LastHttpContent.EMPTY_LAST_CONTENT, channelPromise);
        netty4ProgressAndTimeoutHandler.writeTimeoutRunnable(ctx, true);

        netty4ProgressAndTimeoutHandler.handlerRemoved(ctx);

        assertEquals(0, ctx.getFireExceptionCaughtCallCount());
    }
}
