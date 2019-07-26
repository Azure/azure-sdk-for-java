// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The channel initializer.
 *
 */
public class HttpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final Logger logger = LoggerFactory.getLogger(HttpProxyChannelInitializer.class);
    private AtomicLong taskCounter = new AtomicLong();
    private HttpProxyClientHandler httpProxyClientHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        httpProxyClientHandler = new HttpProxyClientHandler("task-" + taskCounter.getAndIncrement());
        logger.info("task-" + taskCounter.getAndIncrement());
        ch.pipeline().addLast(httpProxyClientHandler);
    }

}
