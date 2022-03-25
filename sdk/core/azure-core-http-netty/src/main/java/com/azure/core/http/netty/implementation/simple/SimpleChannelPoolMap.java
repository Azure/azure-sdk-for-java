// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.util.logging.ClientLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URI;

public class SimpleChannelPoolMap extends AbstractChannelPoolMap<URI, ChannelPool> {

    private static final ClientLogger LOGGER = new ClientLogger(SimpleChannelPoolMap.class);

    private final EventLoopGroup eventLoopGroup;

    public SimpleChannelPoolMap(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }

    @Override
    protected ChannelPool newPool(URI uri) {
        String protocol = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(protocol)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(protocol)) {
                port = 443;
            }
        }

        // Configure SSL context if necessary.
        boolean ssl = "https".equalsIgnoreCase(protocol);
        SslContext sslCtx = null;
        if (ssl) {
            try {
                sslCtx = SslContextBuilder.forClient().build();
            } catch (SSLException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
            .remoteAddress(InetSocketAddress.createUnresolved(host, port))
            .channel(NioSocketChannel.class);
        return new SimpleChannelPool(bootstrap, new SimpleChannelPoolHandler(sslCtx));
    }
}
