// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.mocking;

import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;

import java.util.function.Consumer;

public class MockNettyInbound implements NettyInbound {
    private final ByteBufFlux receive;

    public MockNettyInbound(ByteBufFlux receive) {
        this.receive = receive;
    }

    @Override
    public ByteBufFlux receive() {
        return receive;
    }

    @Override
    public Flux<?> receiveObject() {
        return null;
    }

    @Override
    public NettyInbound withConnection(Consumer<? super Connection> withConnection) {
        return null;
    }
}
