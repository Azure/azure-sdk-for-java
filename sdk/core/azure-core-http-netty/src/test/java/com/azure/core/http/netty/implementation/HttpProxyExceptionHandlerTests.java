// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.proxy.ProxyConnectException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.net.ssl.SSLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link HttpProxyExceptionHandler}.
 */
public class HttpProxyExceptionHandlerTests {
    /**
     * Tests that when a non {@link SSLException} is thrown in the pipeline it isn't unboxed.
     */
    @Test
    public void nonSslExceptionIsIgnored() {
        HttpProxyExceptionHandler proxyExceptionHandler = new HttpProxyExceptionHandler();

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        proxyExceptionHandler.exceptionCaught(ctx, new RuntimeException());
        verify(ctx).fireExceptionCaught(exceptionCaptor.capture());
        assertTrue(exceptionCaptor.getValue() instanceof RuntimeException);
    }

    /**
     * Tests that when a {@link SSLException} is thrown in the pipeline but isn't caused by a
     * {@link ProxyConnectException} it isn't unboxed.
     */
    @Test
    public void sslExceptionNotCausedByProxyConnectExceptionIsNotUnboxed() {
        HttpProxyExceptionHandler proxyExceptionHandler = new HttpProxyExceptionHandler();

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        proxyExceptionHandler.exceptionCaught(ctx, new SSLException("SSL had an issue"));
        verify(ctx).fireExceptionCaught(exceptionCaptor.capture());
        assertTrue(exceptionCaptor.getValue() instanceof SSLException);
    }

    /**
     * Tests that when a {@link SSLException} is thrown in the pipeline and is caused by a {@link ProxyConnectException}
     * it is unboxed to the {@link ProxyConnectException}.
     */
    @Test
    public void sslExceptionCauseByProxyConnectExceptionIsUnboxed() {
        HttpProxyExceptionHandler proxyExceptionHandler = new HttpProxyExceptionHandler();

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        SSLException sslException = new SSLException("SSL exception wraps the root issue.",
            new ProxyConnectException("Connecting to the proxy is the real cause."));

        proxyExceptionHandler.exceptionCaught(ctx, sslException);
        verify(ctx).fireExceptionCaught(exceptionCaptor.capture());
        assertTrue(exceptionCaptor.getValue() instanceof ProxyConnectException);
    }
}
