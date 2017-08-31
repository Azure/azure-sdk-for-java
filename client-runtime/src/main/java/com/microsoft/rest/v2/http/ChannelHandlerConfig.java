/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.EventExecutorGroup;
import rx.functions.Func0;

/**
 * Type to compose a Netty ChannelHandler factory and it's configuration.
 */
public class ChannelHandlerConfig {
    private final Func0<ChannelHandler> factory;

    /**
     * @return reference to a Func, that return instance of a Netty ChannelHandler upon invocation.
     */
    public Func0<ChannelHandler> factory() {
        return factory;
    }

    private final boolean mayBlock;
    /**
     * @return true the handler returned by the factory may block the thread running its methods, false otherwise
     */
    public boolean mayBlock() {
        return mayBlock;
    }

    /**
     * Creates ChannelHandlerConfig.
     *
     * @param factory the factory to create Netty ChannelHandler on demand.
     * @param mayBlock indicates whether Netty ChannelHandler blocks the thread in which it is running.
     *                 If set to true, the handler will be run on a separate {@link EventExecutorGroup} from the channel's {@link EventLoop}.
     */
    public ChannelHandlerConfig(Func0<ChannelHandler> factory, boolean mayBlock) {
        this.factory = factory;
        this.mayBlock = mayBlock;
    }
}
