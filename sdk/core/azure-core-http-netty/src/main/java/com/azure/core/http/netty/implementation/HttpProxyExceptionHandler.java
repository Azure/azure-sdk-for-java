// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLException;

/**
 * This class handles removing {@link SSLException SSLExceptions} from being propagated when connecting to the proxy
 * fails.
 * <p>
 * The {@link SSLException} is removed since the {@link SslHandler} processes in the pipeline after the
 * {@link ProxyHandler} and if there is a failure to connect to the proxy it may bubble up as an issue with SSL. This
 * will remove the {@link SSLException} if its cause is a {@link ProxyConnectException}, if this happens the
 * {@link ProxyConnectException} will be bubbled up instead.
 */
public final class HttpProxyExceptionHandler extends ChannelDuplexHandler {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SSLException) {
            SSLException sslException = (SSLException) cause;
            if (sslException.getCause() instanceof ProxyConnectException) {
                /*
                 * The exception was an SSLException that was caused by a failure to connect to the proxy, extract the
                 * inner ProxyConnectException and bubble that up instead.
                 */
                ctx.fireExceptionCaught(sslException.getCause());
                return;
            }
        }

        /*
         * The cause either wasn't an SSLException or its inner exception wasn't a ProxyConnectException, continue
         * bubbling up this exception.
         */
        ctx.fireExceptionCaught(cause);
    }
}
