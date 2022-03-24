// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;

import static com.azure.core.http.netty.implementation.simple.SimpleNettyConstants.REQUEST_CONTEXT_KEY;

public class SimpleChannelPoolHandler extends AbstractChannelPoolHandler {
    @Override
    public void channelCreated(Channel channel) {
        ChannelPipeline p = channel.pipeline();

        // Enable HTTPS if necessary.
        //if (sslCtx != null) {
        //    p.addLast(sslCtx.newHandler(ch.alloc()));
        //}

        p.addLast(new HttpClientCodec());

        // Remove the following line if you don't want automatic content decompression.
        p.addLast(new HttpContentDecompressor());

        // Uncomment the following line if you don't want to handle HttpContents.
        //p.addLast(new HttpObjectAggregator(1048576));

        p.addLast(new SimpleChannelHandler());
    }

    @Override
    public void channelReleased(Channel ch) throws Exception {
        ch.attr(REQUEST_CONTEXT_KEY).set(null);
    }
}
