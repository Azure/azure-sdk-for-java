// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeaderName;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.AsciiString;
import reactor.netty.Connection;
import reactor.netty.channel.ChannelOperations;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class containing utility methods.
 */
public final class Utility {
    private static final Map<HttpHeaderName, AsciiString> DYNAMIC_HEADER_NAMES = new ConcurrentHashMap<>();

    /**
     * Deep copies the passed {@link ByteBuf} into a {@link ByteBuffer}.
     * <p>
     * Using this method ensures that data returned by the network is resilient against Reactor Netty releasing the
     * passed {@link ByteBuf} once the {@code doOnNext} operator fires.
     *
     * @param byteBuf The Netty {@link ByteBuf} to deep copy.
     * @return A newly allocated {@link ByteBuffer} containing the copied bytes.
     */
    public static ByteBuffer deepCopyBuffer(ByteBuf byteBuf) {
        ByteBuffer buffer = ByteBuffer.allocate(byteBuf.readableBytes());
        byteBuf.readBytes(buffer);
        buffer.rewind();
        return buffer;
    }

    /**
     * Closes a connection if it hasn't been disposed.
     *
     * @param reactorNettyConnection The connection to close.
     */
    public static void closeConnection(Connection reactorNettyConnection) {
        // ChannelOperations is generally the default implementation of Connection used.
        //
        // Using the specific subclass allows for a finer grain handling.
        if (reactorNettyConnection instanceof ChannelOperations) {
            ChannelOperations<?, ?> channelOperations = (ChannelOperations<?, ?>) reactorNettyConnection;

            // Given that this is an HttpResponse the only time this will be called is when the outbound has completed.
            //
            // From there the only thing that needs to be checked is whether the inbound has been disposed (completed),
            // and if not dispose it (aka drain it).
            if (!channelOperations.isInboundDisposed()) {
                channelOperations.channel().eventLoop().execute(channelOperations::discard);
            }
        } else if (!reactorNettyConnection.isDisposed()) {
            reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
        }
    }

    public static AsciiString convertHeaderNameToAsciiString(String name) {
        return convertHeaderNameToAsciiString(HttpHeaderName.fromString(name));
    }

    public static AsciiString convertHeaderNameToAsciiString(HttpHeaderName name) {
        if (name == null) {
            return null;
        }

        if (HttpHeaderName.ACCEPT.equals(name)) {
            return HttpHeaderNames.ACCEPT;
        } else if (HttpHeaderName.ACCEPT_CHARSET.equals(name)) {
            return HttpHeaderNames.ACCEPT_CHARSET;
        } else if (HttpHeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS.equals(name)) {
            return HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS;
        } else if (HttpHeaderName.ACCESS_CONTROL_ALLOW_HEADERS.equals(name)) {
            return HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS;
        } else if (HttpHeaderName.ACCESS_CONTROL_ALLOW_METHODS.equals(name)) {
            return HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS;
        } else if (HttpHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN.equals(name)) {
            return HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
        } else if (HttpHeaderName.ACCESS_CONTROL_EXPOSE_HEADERS.equals(name)) {
            return HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS;
        } else if (HttpHeaderName.ACCESS_CONTROL_MAX_AGE.equals(name)) {
            return HttpHeaderNames.ACCESS_CONTROL_MAX_AGE;
        } else if (HttpHeaderName.ACCEPT_ENCODING.equals(name)) {
            return HttpHeaderNames.ACCEPT_ENCODING;
        } else if (HttpHeaderName.ACCEPT_LANGUAGE.equals(name)) {
            return HttpHeaderNames.ACCEPT_LANGUAGE;
        } else if (HttpHeaderName.ACCEPT_PATCH.equals(name)) {
            return HttpHeaderNames.ACCEPT_PATCH;
        } else if (HttpHeaderName.ACCEPT_RANGES.equals(name)) {
            return HttpHeaderNames.ACCEPT_RANGES;
        } else if (HttpHeaderName.AGE.equals(name)) {
            return HttpHeaderNames.AGE;
        } else if (HttpHeaderName.ALLOW.equals(name)) {
            return HttpHeaderNames.ALLOW;
        } else if (HttpHeaderName.AUTHORIZATION.equals(name)) {
            return HttpHeaderNames.AUTHORIZATION;
        } else if (HttpHeaderName.CACHE_CONTROL.equals(name)) {
            return HttpHeaderNames.CACHE_CONTROL;
        } else if (HttpHeaderName.CONNECTION.equals(name)) {
            return HttpHeaderNames.CONNECTION;
        } else if (HttpHeaderName.CONTENT_DISPOSITION.equals(name)) {
            return HttpHeaderNames.CONTENT_DISPOSITION;
        } else if (HttpHeaderName.CONTENT_ENCODING.equals(name)) {
            return HttpHeaderNames.CONTENT_ENCODING;
        } else if (HttpHeaderName.CONTENT_LANGUAGE.equals(name)) {
            return HttpHeaderNames.CONTENT_LANGUAGE;
        } else if (HttpHeaderName.CONTENT_LENGTH.equals(name)) {
            return HttpHeaderNames.CONTENT_LENGTH;
        } else if (HttpHeaderName.CONTENT_LOCATION.equals(name)) {
            return HttpHeaderNames.CONTENT_LOCATION;
        } else if (HttpHeaderName.CONTENT_MD5.equals(name)) {
            return HttpHeaderNames.CONTENT_MD5;
        } else if (HttpHeaderName.CONTENT_RANGE.equals(name)) {
            return HttpHeaderNames.CONTENT_RANGE;
        } else if (HttpHeaderName.CONTENT_TYPE.equals(name)) {
            return HttpHeaderNames.CONTENT_TYPE;
        } else if (HttpHeaderName.COOKIE.equals(name)) {
            return HttpHeaderNames.COOKIE;
        } else if (HttpHeaderName.DATE.equals(name)) {
            return HttpHeaderNames.DATE;
        } else if (HttpHeaderName.ETAG.equals(name)) {
            return HttpHeaderNames.ETAG;
        } else if (HttpHeaderName.EXPECT.equals(name)) {
            return HttpHeaderNames.EXPECT;
        } else if (HttpHeaderName.EXPIRES.equals(name)) {
            return HttpHeaderNames.EXPIRES;
        } else if (HttpHeaderName.FROM.equals(name)) {
            return HttpHeaderNames.FROM;
        } else if (HttpHeaderName.HOST.equals(name)) {
            return HttpHeaderNames.HOST;
        } else if (HttpHeaderName.IF_MATCH.equals(name)) {
            return HttpHeaderNames.IF_MATCH;
        } else if (HttpHeaderName.IF_MODIFIED_SINCE.equals(name)) {
            return HttpHeaderNames.IF_MODIFIED_SINCE;
        } else if (HttpHeaderName.IF_NONE_MATCH.equals(name)) {
            return HttpHeaderNames.IF_NONE_MATCH;
        } else if (HttpHeaderName.IF_RANGE.equals(name)) {
            return HttpHeaderNames.IF_RANGE;
        } else if (HttpHeaderName.IF_UNMODIFIED_SINCE.equals(name)) {
            return HttpHeaderNames.IF_UNMODIFIED_SINCE;
        } else if (HttpHeaderName.LAST_MODIFIED.equals(name)) {
            return HttpHeaderNames.LAST_MODIFIED;
        } else if (HttpHeaderName.LOCATION.equals(name)) {
            return HttpHeaderNames.LOCATION;
        } else if (HttpHeaderName.MAX_FORWARDS.equals(name)) {
            return HttpHeaderNames.MAX_FORWARDS;
        } else if (HttpHeaderName.ORIGIN.equals(name)) {
            return HttpHeaderNames.ORIGIN;
        } else if (HttpHeaderName.PRAGMA.equals(name)) {
            return HttpHeaderNames.PRAGMA;
        } else if (HttpHeaderName.PROXY_AUTHENTICATE.equals(name)) {
            return HttpHeaderNames.PROXY_AUTHENTICATE;
        } else if (HttpHeaderName.PROXY_AUTHORIZATION.equals(name)) {
            return HttpHeaderNames.PROXY_AUTHORIZATION;
        } else if (HttpHeaderName.RANGE.equals(name)) {
            return HttpHeaderNames.RANGE;
        } else if (HttpHeaderName.REFERER.equals(name)) {
            return HttpHeaderNames.REFERER;
        } else if (HttpHeaderName.RETRY_AFTER.equals(name)) {
            return HttpHeaderNames.RETRY_AFTER;
        } else if (HttpHeaderName.SERVER.equals(name)) {
            return HttpHeaderNames.SERVER;
        } else if (HttpHeaderName.SET_COOKIE.equals(name)) {
            return HttpHeaderNames.SET_COOKIE;
        } else if (HttpHeaderName.TE.equals(name)) {
            return HttpHeaderNames.TE;
        } else if (HttpHeaderName.TRAILER.equals(name)) {
            return HttpHeaderNames.TRAILER;
        } else if (HttpHeaderName.TRANSFER_ENCODING.equals(name)) {
            return HttpHeaderNames.TRANSFER_ENCODING;
        } else if (HttpHeaderName.USER_AGENT.equals(name)) {
            return HttpHeaderNames.USER_AGENT;
        } else if (HttpHeaderName.UPGRADE.equals(name)) {
            return HttpHeaderNames.UPGRADE;
        } else if (HttpHeaderName.VARY.equals(name)) {
            return HttpHeaderNames.VARY;
        } else if (HttpHeaderName.VIA.equals(name)) {
            return HttpHeaderNames.VIA;
        } else if (HttpHeaderName.WARNING.equals(name)) {
            return HttpHeaderNames.WARNING;
        } else if (HttpHeaderName.WWW_AUTHENTICATE.equals(name)) {
            return HttpHeaderNames.WWW_AUTHENTICATE;
        }
        return DYNAMIC_HEADER_NAMES.computeIfAbsent(name, n -> new AsciiString(n.getHttp1Name()));
    }

    private Utility() {
    }
}
