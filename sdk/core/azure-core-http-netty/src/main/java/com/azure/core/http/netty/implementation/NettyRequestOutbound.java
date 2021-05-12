// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.RequestOutbound;

import java.nio.channels.WritableByteChannel;

/**
 * A Netty implementation of {@link RequestOutbound}.
 */
public final class NettyRequestOutbound implements RequestOutbound {
    private final WritableByteChannel outboundChannel;

    /**
     * Creates a new instance of {@link NettyRequestOutbound}.
     *
     * @param outboundChannel The request outbound channel.
     */
    public NettyRequestOutbound(WritableByteChannel outboundChannel) {
        this.outboundChannel = outboundChannel;
    }

    @Override
    public WritableByteChannel getRequestChannel() {
        return outboundChannel;
    }
}
