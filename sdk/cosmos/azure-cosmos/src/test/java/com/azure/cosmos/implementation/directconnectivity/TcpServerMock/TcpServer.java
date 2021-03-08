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
 *
 */

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd.ServerRntbdContextEncoder;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd.ServerRntbdContextRequestDecoder;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd.ServerRntbdRequestDecoder;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd.ServerRntbdRequestFramer;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd.ServerRntbdRequestManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;

public class TcpServer {

    private final static Logger logger = LoggerFactory.getLogger(TcpServer.class);
    private static final String SERVER_KEYSTORE = "server.jks";
    private final int port;
    private final EventLoopGroup parent;
    private final EventLoopGroup child;
    private final ServerRntbdRequestManager requestManager; // Use to inject fake server response.

    public TcpServer(int port) {
        this.port = port;
        this.parent = new NioEventLoopGroup();
        this.child = new NioEventLoopGroup();
        requestManager = new ServerRntbdRequestManager();
    }

    public void start(Promise<Boolean> promise) throws InterruptedException {

        SslContext sslContext = SslContextUtils.CreateSslContext(SERVER_KEYSTORE, true);
        Utils.checkNotNullOrThrow(sslContext, "sslContext", "");

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(parent, child)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {

                        SSLEngine engine = sslContext.newEngine(channel.alloc());
                        engine.setUseClientMode(false);

                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(
                            new SslHandler(engine),
                            new ServerRntbdRequestFramer(),
                            new ServerRntbdRequestDecoder(),
                            new ServerRntbdContextRequestDecoder(),
                            new ServerRntbdContextEncoder(),
                            requestManager
                        );

                        LogLevel logLevel = null;

                        if (logger.isTraceEnabled()) {
                            logLevel = LogLevel.TRACE;
                        } else if (logger.isDebugEnabled()) {
                            logLevel = LogLevel.DEBUG;
                        }

                        if (logLevel != null) {
                            pipeline.addFirst(new LoggingHandler(logLevel));
                        }
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.bind(port).sync().addListener((ChannelFuture f) -> {
                if (f.isSuccess()) {
                    logger.info(
                        "{} started and listening for connections on {}",
                        TcpServer.class.getSimpleName(),
                        f.channel().localAddress());
                    promise.setSuccess(Boolean.TRUE);
                }
            });

            channelFuture.channel().closeFuture().sync().addListener((ChannelFuture f) -> {
                logger.info("Server channel closed.");
            });

        } catch (Exception e) {
            promise.setFailure(e);
        } finally {
            parent.shutdownGracefully().sync();
            child.shutdownGracefully().sync();
        }
    }

    public void shutdown(Promise<Boolean> promise) {
        try {
            parent.shutdownGracefully().sync();
            child.shutdownGracefully().sync();
            promise.setSuccess(Boolean.TRUE);
        } catch (InterruptedException e) {
            logger.error("Error when shutting down server {}", e);
            promise.setFailure(e);
        }
    }

    public void injectServerResponse(RequestResponseType responseType) {
        this.requestManager.injectServerResponse(responseType);
    }
}
