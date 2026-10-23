// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

final class WebSocketSessionTestServer implements AutoCloseable {
    private static final String PROTOCOL = "json.webpubsub.azure.v1";
    private final EventLoopGroup group = new NioEventLoopGroup(1);
    private final CompletableFuture<Channel> connection = new CompletableFuture<>();
    private final Channel listener;

    WebSocketSessionTestServer() throws InterruptedException {
        this(PROTOCOL, true);
    }

    WebSocketSessionTestServer(String protocol, boolean handleCloseFrames) throws InterruptedException {
        listener = new ServerBootstrap().group(group)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) {
                    channel.pipeline()
                        .addLast(new HttpServerCodec(), new HttpObjectAggregator(8192),
                            new WebSocketServerProtocolHandler(WebSocketServerProtocolConfig.newBuilder()
                                .websocketPath("/")
                                .subprotocols(protocol)
                                .handleCloseFrames(handleCloseFrames)
                                .build()),
                            new SimpleChannelInboundHandler<WebSocketFrame>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext context, WebSocketFrame frame) {
                                    // Consume frames without replying when testing an unresponsive peer.
                                }
                            });
                    connection.complete(channel);
                }
            })
            .bind("127.0.0.1", 0)
            .sync()
            .channel();
    }

    String getUrl() {
        return "ws://127.0.0.1:" + ((InetSocketAddress) listener.localAddress()).getPort() + "/";
    }

    Channel getConnection() throws Exception {
        return connection.get(5, TimeUnit.SECONDS);
    }

    static WebSocketSessionNettyImpl createSession(String url, Consumer<WebSocketSession> openHandler,
        Consumer<CloseReason> closeHandler) {
        return new WebSocketSessionNettyImpl(new ClientEndpointConfiguration(PROTOCOL, "test"), url,
            new AtomicReference<>(new ClientLogger(WebSocketSessionTestServer.class)), message -> {
            }, openHandler, closeHandler);
    }

    static <T> T getField(WebSocketSessionNettyImpl session, String name, Class<T> type) throws Exception {
        Field field = WebSocketSessionNettyImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(session));
    }

    static void cleanup(WebSocketSessionNettyImpl session) throws Exception {
        Channel channel = getField(session, "ch", Channel.class);
        if (channel != null && channel.isOpen()) {
            channel.close().syncUninterruptibly();
        }
        EventLoopGroup sessionGroup = getField(session, "group", EventLoopGroup.class);
        if (sessionGroup != null) {
            sessionGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS).syncUninterruptibly();
        }
    }

    @Override
    public void close() {
        listener.close().syncUninterruptibly();
        group.shutdownGracefully(0, 0, TimeUnit.SECONDS).syncUninterruptibly();
    }
}
