// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Tests {@link ReadTimeoutHandler}.
 */
public class ReadTimeoutHandlerTests {
    @Test
    public void noTimeoutDoesNotAddWatcher() {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(0);

        EventExecutor eventExecutor = mock(EventExecutor.class);
        when(eventExecutor.scheduleAtFixedRate(any(), anyLong(), anyLong(), any())).thenReturn(null);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(eventExecutor);

        readTimeoutHandler.handlerAdded(ctx);

        verify(eventExecutor, never()).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
    }

    @Test
    public void timeoutAddsWatcher() {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(1);

        EventExecutor eventExecutor = mock(EventExecutor.class);
        when(eventExecutor.scheduleAtFixedRate(any(), eq(1L), eq(1L), any())).thenReturn(null);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(eventExecutor);

        readTimeoutHandler.handlerAdded(ctx);

        verify(eventExecutor, times(1)).scheduleAtFixedRate(any(), eq(1L), eq(1L), any());
    }

    @Test
    public void removingHandlerCancelsTimeout() throws InterruptedException {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(100);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(new DefaultEventExecutor());

        readTimeoutHandler.handlerAdded(ctx);
        readTimeoutHandler.handlerRemoved(ctx);

        Thread.sleep(100);

        verify(ctx, never()).fireExceptionCaught(any());
    }

    @Test
    public void readTimesOut() throws InterruptedException {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(100);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(new DefaultEventExecutor());

        readTimeoutHandler.handlerAdded(ctx);

        Thread.sleep(500);

        verify(ctx, atLeast(1)).fireExceptionCaught(any());
    }

    @Test
    public void readingUpdatesTimeout() throws InterruptedException {
        ReadTimeoutHandler readTimeoutHandler = new ReadTimeoutHandler(500);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(new DefaultEventExecutor());

        readTimeoutHandler.handlerAdded(ctx);

        Thread.sleep(100);

        readTimeoutHandler.channelReadComplete(ctx);

        Thread.sleep(450);

        readTimeoutHandler.handlerRemoved(ctx);

        verify(ctx, never()).fireExceptionCaught(any());
    }
}
