// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
final class Http2SettingsHandler extends ChannelInboundHandlerAdapter {
    static final String HANDLER_NAME = "cosmosHttp2Settings";
    static final Http2SettingsHandler INSTANCE = new Http2SettingsHandler();

    private Http2SettingsHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2SettingsAckFrame) {
            ReferenceCountUtil.release(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
