// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.mocking;

import io.netty.channel.Channel;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;

public class MockConnection implements Connection {
    private final Channel channel;
    private final boolean isDisposed;
    private final NettyInbound inbound;

    public MockConnection(NettyInbound inbound, boolean isDisposed) {
        this.inbound = inbound;
        this.isDisposed = isDisposed;
        this.channel = null;
    }

    public MockConnection(NettyInbound inbound, Channel channel) {
        this.isDisposed = false;
        this.channel = channel;
        this.inbound = inbound;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public NettyInbound inbound() {
        return inbound;
    }
}
