/*
 * Copyright (c) 2020-2026 VMware, Inc. or its affiliates, All Rights Reserved.
 * Modifications Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azure.cosmos.implementation.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2SettingsFrame;
import io.netty.util.AttributeKey;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;
import reactor.netty.channel.ChannelOperations;

import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_ENABLE_CONNECT_PROTOCOL;

/**
 * TLS/H2 extraction of Reactor Netty 1.2.18 {@code HttpTrafficHandler}.
 * Source: https://github.com/reactor/reactor-netty/blob/c753da48febe3ea28b19bb1cf64ba3b377f1a820/reactor-netty-http/src/main/java/reactor/netty/http/client/HttpTrafficHandler.java
 *
 * Modifications: relocated/renamed class; local alias for Reactor's package-private attribute;
 * recognized connection-close exception substituted below. H2C upgrade events and H2C pool
 * invalidation are omitted because this handler is installed only after TLS negotiates H2.
 * The remaining lifecycle methods retain the upstream structure and ordering.
 *
 * @author Violeta Georgieva
 */
final class ReactorNettyHttpTrafficHandler extends ChannelInboundHandlerAdapter {
    // Same key as HttpClientConnect.ENABLE_CONNECT_PROTOCOL; never use a Cosmos-specific key here.
    private static final AttributeKey<Long> ENABLE_CONNECT_PROTOCOL = AttributeKey.valueOf("$ENABLE_CONNECT_PROTOCOL");
    final ConnectionObserver listener;

    ReactorNettyHttpTrafficHandler(ConnectionObserver listener) {
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            if (ctx.pipeline().get(NettyPipeline.H2MultiplexHandler) == null) {
                // Proceed with HTTP/1.x as per configuration
                ctx.fireChannelActive();
            } else if (ctx.pipeline().get(NettyPipeline.SslHandler) == null) {
                // Proceed with H2C as per configuration
                sendNewState(Connection.from(channel), ConnectionObserver.State.CONNECTED);
                ctx.flush();
                ctx.read();
            } else {
                // Proceed with H2 as per configuration
                sendNewState(Connection.from(channel), ConnectionObserver.State.CONNECTED);
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Http2SettingsFrame) {
            ctx.channel().attr(ENABLE_CONNECT_PROTOCOL).set(
                ((Http2SettingsFrame) msg).settings().get(SETTINGS_ENABLE_CONNECT_PROTOCOL));
            sendNewState(Connection.from(ctx.channel()), ConnectionObserver.State.CONFIGURED);
            ctx.pipeline().remove(NettyPipeline.ReactiveBridge);
            ctx.pipeline().remove(this);
            return;
        }

        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // Cosmos substitution: Reactor's PrematureCloseException constructors are package-private.
        // ClosedChannelException preserves Cosmos's network-failure classification without reflection.
        ctx.fireExceptionCaught(new Http2PrematureCloseException());
    }

    @Override
    public boolean isSharable() {
        return false;
    }

    void sendNewState(Connection connection, ConnectionObserver.State state) {
        ChannelOperations<?, ?> ops = connection.as(ChannelOperations.class);
        if (ops != null) {
            listener.onStateChange(ops, state);
        } else {
            listener.onStateChange(connection, state);
        }
    }
}
