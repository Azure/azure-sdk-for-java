// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ThreadFactory;

public class RntbdLoopNIO implements RntbdLoop {

    @Override
    public String getName() {
        return "nio";
    }

    @Override
    public EventLoopGroup newEventLoopGroup(int threads, ThreadFactory threadFactory) {
        return new NioEventLoopGroup(threads, threadFactory);
    }

    @Override
    public Class<? extends SocketChannel> getChannelClass() {
        return NioSocketChannel.class;
    }
}
