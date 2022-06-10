// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ThreadFactory;

/**
 * Rntbd loop with EventLoopGroup factory.
 */
public interface RntbdLoop {

    /**
     * Get the name of the rntbd loop which will be used as part of the thread name.
     * @return The name of the rntbd loop.
     */
    String getName();

    /**
     * Create a new EventLoopGroup.
     * @param threads The number of threads used in the EventLoopGroup.
     * @param threadFactory The thread factory.
     * @return The new EventLoopGroup.
     */
    EventLoopGroup newEventLoopGroup(int threads, ThreadFactory threadFactory);

    /**
     * Get the socket channel class.
     * @return The socket channel class.
     */
    Class<? extends SocketChannel> getChannelClass();
}
