// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;

import java.util.Iterator;
import java.util.List;

public class Http2ResponseHeaderCleanerHandler extends ChannelInboundHandlerAdapter {
    static final String HANDLER_NAME = "customHeaderCleaner";
    private static final AsciiString SERVER_VERSION_KEY = AsciiString.cached(HttpConstants.HttpHeaders.SERVER_VERSION);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2SettingsAckFrame) {
            ReferenceCountUtil.release(msg);
            return;
        }
        if (msg instanceof Http2HeadersFrame) {
            Http2Headers headers = ((Http2HeadersFrame) msg).headers();
            Iterator<CharSequence> values = headers.valueIterator(SERVER_VERSION_KEY);
            while (values.hasNext()) {
                CharSequence value = values.next();
                if (trimOptionalWhitespace(value) != value) {
                    // Preserve duplicate fields and their order; allocate a list only when trimming is needed.
                    List<CharSequence> normalizedValues = headers.getAll(SERVER_VERSION_KEY);
                    normalizedValues.replaceAll(Http2ResponseHeaderCleanerHandler::trimOptionalWhitespace);
                    headers.set(SERVER_VERSION_KEY, normalizedValues);
                    break;
                }
            }
        }
        ctx.fireChannelRead(msg);
    }

    private static CharSequence trimOptionalWhitespace(CharSequence value) {
        int start = 0;
        int end = value.length();
        while (start < end && isOptionalWhitespace(value.charAt(start))) {
            start++;
        }
        while (end > start && isOptionalWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return start == 0 && end == value.length() ? value : value instanceof AsciiString
            ? ((AsciiString) value).subSequence(start, end, false) : value.subSequence(start, end);
    }

    private static boolean isOptionalWhitespace(char value) {
        // Do not trim CR/LF/NUL: the downstream HTTP-object validator must still reject them.
        return value == ' ' || value == '\t';
    }
}
