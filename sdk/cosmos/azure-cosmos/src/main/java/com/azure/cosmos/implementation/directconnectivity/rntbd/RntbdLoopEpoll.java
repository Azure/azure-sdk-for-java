// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ThreadFactory;

public class RntbdLoopEpoll implements RntbdLoop {

    @Override
    public String getName() {
        return "epoll";
    }

    @Override
    public EventLoopGroup newEventLoopGroup(int threads, ThreadFactory threadFactory) {
        return new EpollEventLoopGroup(threads, threadFactory);
    }

    @Override
    public Class<? extends SocketChannel> getChannelClass() {
        return EpollSocketChannel.class;
    }
}
