/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.rx.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

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
