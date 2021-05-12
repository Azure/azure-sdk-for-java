// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.util.RequestOutbound;

import java.nio.channels.WritableByteChannel;

/**
 * An OkHttp implementation of {@link RequestOutbound}.
 */
public final class OkHttpRequestOutbound implements RequestOutbound {
    private final WritableByteChannel outboundChannel;

    /**
     * Creates a new instance of {@link OkHttpRequestOutbound}.
     *
     * @param outboundChannel The request outbound channel.
     */
    public OkHttpRequestOutbound(WritableByteChannel outboundChannel) {
        this.outboundChannel = outboundChannel;
    }

    @Override
    public WritableByteChannel getRequestChannel() {
        return outboundChannel;
    }
}
