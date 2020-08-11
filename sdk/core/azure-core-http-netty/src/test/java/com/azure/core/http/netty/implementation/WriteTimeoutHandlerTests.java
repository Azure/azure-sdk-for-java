// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
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
 * Tests {@link WriteTimeoutHandler}.
 */
public class WriteTimeoutHandlerTests {
    @Test
    public void noTimeoutDoesNotAddWatcher() {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(0);

        EventExecutor eventExecutor = mock(EventExecutor.class);
        when(eventExecutor.scheduleAtFixedRate(any(), anyLong(), anyLong(), any())).thenReturn(null);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(eventExecutor);

        writeTimeoutHandler.handlerAdded(ctx);

        verify(eventExecutor, never()).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
    }

    @Test
    public void timeoutAddsWatcher() {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(1);

        EventExecutor eventExecutor = mock(EventExecutor.class);
        when(eventExecutor.scheduleAtFixedRate(any(), eq(1L), eq(1L), any())).thenReturn(null);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(eventExecutor);

        writeTimeoutHandler.handlerAdded(ctx);

        verify(eventExecutor, times(1)).scheduleAtFixedRate(any(), eq(1L), eq(1L), any());
    }

    @Test
    public void removingHandlerCancelsTimeout() throws InterruptedException {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(100);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(new DefaultEventExecutor());

        writeTimeoutHandler.handlerAdded(ctx);
        writeTimeoutHandler.handlerRemoved(ctx);

        Thread.sleep(100);

        verify(ctx, never()).fireExceptionCaught(any());
    }

    @Test
    public void writeTimesOut() throws InterruptedException {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(100);

        Channel.Unsafe unsafe = mock(Channel.Unsafe.class);
        when(unsafe.outboundBuffer()).thenReturn(null);

        Channel channel = mock(AbstractChannel.class);
        when(channel.unsafe()).thenReturn(unsafe);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(new DefaultEventExecutor());
        when(ctx.channel()).thenReturn(channel);

        writeTimeoutHandler.handlerAdded(ctx);

        Thread.sleep(500);

        verify(ctx, atLeast(1)).fireExceptionCaught(any());
    }

    @Test
    public void writingUpdatesTimeout() throws InterruptedException {
        WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(500);

        Channel.Unsafe unsafe = mock(Channel.Unsafe.class);
        when(unsafe.outboundBuffer()).thenReturn(null);

        Channel channel = mock(AbstractChannel.class);
        when(channel.unsafe()).thenReturn(unsafe);

        EventExecutor eventExecutor = new DefaultEventExecutor();
        ChannelPromise channelPromise = new DefaultChannelPromise(channel, eventExecutor);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(eventExecutor);
        when(ctx.channel()).thenReturn(channel);
        when(ctx.write(any(), any())).thenReturn(channelPromise);

        writeTimeoutHandler.handlerAdded(ctx);

        Thread.sleep(100);

        writeTimeoutHandler.write(ctx, null, channelPromise);
        channelPromise.setSuccess();

        Thread.sleep(450);

        writeTimeoutHandler.handlerRemoved(ctx);

        verify(ctx, never()).fireExceptionCaught(any());
    }
}
