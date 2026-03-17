// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.handler.codec.http2.Http2SettingsFrame;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http2ResponseHeaderCleanerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Http2ResponseHeaderCleanerHandler.class);
    private static final AsciiString SERVER_VERSION_KEY = AsciiString.of(HttpConstants.HttpHeaders.SERVER_VERSION);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame headersFrame = (Http2HeadersFrame) msg;
            Http2Headers headers = headersFrame.headers();

            // Direct O(1) hash lookup instead of O(n) forEach iteration over all headers
            CharSequence serverVersion = headers.get(SERVER_VERSION_KEY);
            if (serverVersion != null && serverVersion.length() > 0
                && (serverVersion.charAt(0) == ' ' || serverVersion.charAt(serverVersion.length() - 1) == ' ')) {
                logger.trace("There are extra white space for key {} with value {}", SERVER_VERSION_KEY, serverVersion);
                headers.set(SERVER_VERSION_KEY, serverVersion.toString().trim());
            }

            super.channelRead(ctx, msg);
        } else if (msg instanceof Http2SettingsAckFrame) {
            ReferenceCountUtil.release(msg);
        } else if (msg instanceof Http2SettingsFrame) {
            Http2SettingsFrame settingsFrame = (Http2SettingsFrame)msg;
            logger.trace("SETTINGS retrieved - {}", settingsFrame.settings());
            super.channelRead(ctx, msg);
        } else {
            // Pass the message to the next handler in the pipeline
            ctx.fireChannelRead(msg);
        }
    }
}
