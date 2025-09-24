// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.handler.codec.http2.Http2SettingsFrame;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http2ResponseHeaderCleanerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Http2ResponseHeaderCleanerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame headersFrame = (Http2HeadersFrame) msg;
            Http2Headers headers = headersFrame.headers();

            headers.forEach(entry -> {
                CharSequence key = entry.getKey();
                CharSequence value = entry.getValue();

                // Based on the tests, only 'x-ms-serviceversion' header has extra value,
                // so only check this specific header here
                if (StringUtils.equalsIgnoreCase(key, HttpConstants.HttpHeaders.SERVER_VERSION)) {
                    // Check for leading whitespace or other prohibited characters
                    if (StringUtils.isNotEmpty(value) && (value.charAt(0) == ' ' || value.charAt(value.length() - 1) == ' ')) {
                        // Clean up the header value by trimming or handling as needed
                        logger.trace("There are extra white space for key {} with value {}", key, value);

                        // TODO[Http2]: for now just trim the spaces, explore other options for example escape the whitespace
                        headers.set(key, value.toString().trim());
                    }
                }
            });

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
