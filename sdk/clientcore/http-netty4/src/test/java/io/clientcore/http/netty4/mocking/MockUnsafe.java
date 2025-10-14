// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.mocking;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.RecvByteBufAllocator;

import java.net.SocketAddress;

public class MockUnsafe implements Channel.Unsafe {
    @Override
    @Deprecated
    public RecvByteBufAllocator.Handle recvBufAllocHandle() {
        return null;
    }

    @Override
    public SocketAddress localAddress() {
        return null;
    }

    @Override
    public SocketAddress remoteAddress() {
        return null;
    }

    @Override
    public void register(EventLoop eventLoop, ChannelPromise channelPromise) {

    }

    @Override
    public void bind(SocketAddress socketAddress, ChannelPromise channelPromise) {

    }

    @Override
    public void connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {

    }

    @Override
    public void disconnect(ChannelPromise channelPromise) {

    }

    @Override
    public void close(ChannelPromise channelPromise) {

    }

    @Override
    public void closeForcibly() {

    }

    @Override
    public void deregister(ChannelPromise channelPromise) {

    }

    @Override
    public void beginRead() {

    }

    @Override
    public void write(Object o, ChannelPromise channelPromise) {

    }

    @Override
    public void flush() {

    }

    @Override
    public ChannelPromise voidPromise() {
        return null;
    }

    @Override
    public ChannelOutboundBuffer outboundBuffer() {
        return null;
    }
}
