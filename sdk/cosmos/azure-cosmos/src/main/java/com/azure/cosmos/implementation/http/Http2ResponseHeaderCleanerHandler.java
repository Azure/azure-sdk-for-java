// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for cleaning HTTP/2 response headers by removing unwanted whitespace.
 * This handler only processes HTTP/2 header frames and lets all other frame types pass through
 * to be handled by Netty's HTTP/2 implementation.
 */
public class Http2ResponseHeaderCleanerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Http2ResponseHeaderCleanerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Only process HTTP/2 header frames
        if (msg instanceof Http2HeadersFrame) {
            if (logger.isTraceEnabled()) {
                logger.trace("Processing HTTP/2 headers frame");
            }

            Http2HeadersFrame headersFrame = (Http2HeadersFrame) msg;
            Http2Headers headers = headersFrame.headers();

            headers.forEach(entry -> {
                CharSequence key = entry.getKey();
                CharSequence value = entry.getValue();

                // Clean any headers that have leading or trailing whitespace
                if (StringUtils.isNotEmpty(value) && 
                    (value.charAt(0) == ' ' || value.charAt(value.length() - 1) == ' ')) {
                    logger.debug("Cleaning whitespace from header '{}' with value '{}'", key, value);
                    headers.set(key, value.toString().trim());
                }
            });
        }

        // Pass the message to the next handler in the pipeline
        // All other frame types (including SETTINGS, SETTINGS_ACK) are handled by Netty
        ctx.fireChannelRead(msg);
    }
}
