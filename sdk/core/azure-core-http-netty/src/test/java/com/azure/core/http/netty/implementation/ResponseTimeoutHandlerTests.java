// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

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
 * Tests {@link ResponseTimeoutHandler}.
 */
public class ResponseTimeoutHandlerTests {
    @Test
    public void noTimeoutDoesNotAddWatcher() {
        ResponseTimeoutHandler responseTimeoutHandler = new ResponseTimeoutHandler(0);

        EventExecutor eventExecutor = mock(EventExecutor.class);
        when(eventExecutor.schedule(any(Runnable.class), anyLong(), any())).thenReturn(null);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(eventExecutor);

        responseTimeoutHandler.handlerAdded(ctx);

        verify(eventExecutor, never()).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    public void timeoutAddsWatcher() {
        ResponseTimeoutHandler responseTimeoutHandler = new ResponseTimeoutHandler(1);

        EventExecutor eventExecutor = mock(EventExecutor.class);
        when(eventExecutor.schedule(any(Runnable.class), eq(1L), any())).thenReturn(null);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(eventExecutor);

        responseTimeoutHandler.handlerAdded(ctx);

        verify(eventExecutor, times(1)).schedule(any(Runnable.class), eq(1L), any());
    }

    @Test
    public void removingHandlerCancelsTimeout() throws InterruptedException {
        ResponseTimeoutHandler responseTimeoutHandler = new ResponseTimeoutHandler(100);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(new DefaultEventExecutor());

        responseTimeoutHandler.handlerAdded(ctx);
        responseTimeoutHandler.handlerRemoved(ctx);

        Thread.sleep(200);

        verify(ctx, never()).fireExceptionCaught(any());
    }

    @Test
    public void responseTimesOut() throws Exception {
        ResponseTimeoutHandler responseTimeoutHandler = new ResponseTimeoutHandler(100);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.executor()).thenReturn(new DefaultEventExecutor());

        responseTimeoutHandler.handlerAdded(ctx);

        // Fake that the scheduled timer completed before any response is received.
        Method responseTimedOut = responseTimeoutHandler.getClass()
            .getDeclaredMethod("responseTimedOut", ChannelHandlerContext.class);

        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            responseTimedOut.setAccessible(true);
            return null;
        });

        responseTimedOut.invoke(responseTimeoutHandler, ctx);

        verify(ctx, atLeast(1)).fireExceptionCaught(any());
    }
}
