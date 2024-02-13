// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.mocking;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

public class MockChannel extends AbstractChannel {
    private final Attribute<String> attribute;
    private final Unsafe unsafe;

    public MockChannel(Attribute<String> attribute) {
        super(null);
        this.attribute = attribute;
        this.unsafe = null;
    }

    public MockChannel(Unsafe unsafe) {
        super(null);
        this.unsafe = unsafe;
        this.attribute = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return (Attribute<T>) attribute;
    }

    @Override
    public Unsafe unsafe() {
        return unsafe;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected boolean isCompatible(EventLoop eventLoop) {
        return false;
    }

    @Override
    protected SocketAddress localAddress0() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected void doBind(SocketAddress socketAddress) throws Exception {

    }

    @Override
    protected void doDisconnect() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {

    }

    @Override
    protected void doBeginRead() throws Exception {

    }

    @Override
    protected void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception {

    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }
}
