// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.HttpConstants;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaderValidationUtil;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2FrameStreamException;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.handler.codec.http2.Http2SettingsFrame;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ChannelHandler.Sharable
final class Http2ResponseHeaderValidationHandler extends ChannelInboundHandlerAdapter {

    static final String HANDLER_NAME = "cosmosHttp2ResponseHeaderValidation";
    static final Http2ResponseHeaderValidationHandler INSTANCE = new Http2ResponseHeaderValidationHandler();
    private static final Logger logger = LoggerFactory.getLogger(Http2ResponseHeaderValidationHandler.class);
    private static final AsciiString SERVER_VERSION = AsciiString.cached(HttpConstants.HttpHeaders.SERVER_VERSION);
    private static final int SERVER_VERSION_HASH = SERVER_VERSION.hashCode();

    private Http2ResponseHeaderValidationHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame frame = (Http2HeadersFrame) msg;
            try {
                validateHeaders(frame.headers());
            } catch (IllegalArgumentException error) {
                ReferenceCountUtil.release(msg);
                // Let the multiplex handler fail/reset only the affected stream.
                ctx.fireExceptionCaught(new Http2FrameStreamException(frame.stream(), Http2Error.PROTOCOL_ERROR, error));
                return;
            }
            ctx.fireChannelRead(msg);
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

    static void validateHeaders(Http2Headers headers) {
        boolean statusSeen = false;
        for (Map.Entry<CharSequence, CharSequence> entry : headers) {
            CharSequence name = entry.getKey();
            validateName(name, true);
            if (Http2Headers.PseudoHeaderName.hasPseudoHeaderFormat(name)) {
                if (Http2Headers.PseudoHeaderName.getPseudoHeader(name) != Http2Headers.PseudoHeaderName.STATUS
                    || statusSeen || entry.getValue().length() == 0) {
                    throw new IllegalArgumentException("Invalid response pseudo-header '" + name + "'");
                }
                statusSeen = true;
            } else if (HttpHeaderValidationUtil.isConnectionHeader(name, true)
                || HttpHeaderValidationUtil.isTeNotTrailers(name, entry.getValue())) {
                throw new IllegalArgumentException("Invalid HTTP/2 response header '" + name + "'");
            }
            CharSequence value = validateValue(name, entry.getValue());
            if (value != entry.getValue()) {
                entry.setValue(value);
            }
        }
    }

    private static void validateName(CharSequence name, boolean http2) {
        if (name.length() == 0) {
            throw new IllegalArgumentException("Empty response header name");
        }
        if (http2 && Http2Headers.PseudoHeaderName.hasPseudoHeaderFormat(name)) {
            if (Http2Headers.PseudoHeaderName.getPseudoHeader(name) == null) {
                throw new IllegalArgumentException("Unknown response pseudo-header '" + name + "'");
            }
            return;
        }
        if (HttpHeaderValidationUtil.validateToken(name) != -1) {
            throw new IllegalArgumentException("Invalid response header name '" + name + "'");
        }
        if (http2) {
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) >= 'A' && name.charAt(i) <= 'Z') {
                    throw new IllegalArgumentException("Uppercase HTTP/2 response header name '" + name + "'");
                }
            }
        }
    }

    private static CharSequence validateValue(CharSequence name, CharSequence original) {
        CharSequence value = original;
        if (AsciiString.hashCode(name) == SERVER_VERSION_HASH && AsciiString.contentEquals(SERVER_VERSION, name)) {
            int start = 0;
            int end = value.length();
            while (start < end && isOptionalWhitespace(value.charAt(start))) {
                start++;
            }
            while (end > start && isOptionalWhitespace(value.charAt(end - 1))) {
                end--;
            }
            if (start != 0 || end != value.length()) {
                value = value instanceof AsciiString
                    ? ((AsciiString) value).subSequence(start, end, false)
                    : value.subSequence(start, end);
            }
        }
        int invalid = HttpHeaderValidationUtil.validateValidHeaderValue(value);
        if (invalid != -1) {
            throw new IllegalArgumentException("Validation failed for header '" + name
                + "': prohibited character 0x" + Integer.toHexString(value.charAt(invalid))
                + " at index " + invalid);
        }
        return value;
    }

    private static boolean isOptionalWhitespace(char value) {
        // Do not trim CR/LF/NUL before validation.
        return value == ' ' || value == '\t';
    }
}
