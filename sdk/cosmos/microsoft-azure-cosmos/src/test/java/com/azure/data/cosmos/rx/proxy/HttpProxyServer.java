// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A http proxy server.
 *
 */
public class HttpProxyServer {
    private final Logger logger = LoggerFactory.getLogger(HttpProxyServer.class);
    private HttpProxyChannelInitializer httpProxyChannelInitializer;
    private int port = 8080;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    public HttpProxyServer() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
    }

    public void start() {
        new Thread(() -> {
            logger.info("HttpProxyServer started on port: {}", port);
            httpProxyChannelInitializer = new HttpProxyChannelInitializer();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .childHandler(httpProxyChannelInitializer)
                 .bind(port).sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                logger.error("Error occurred", e);
            }
        }).start();
    }

    public void  shutDown() {
        if(!workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }

        if(!bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
    }
}
