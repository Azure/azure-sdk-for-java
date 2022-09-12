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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class containing utility methods.
 */
public final class Utility {
    private static final Map<HttpHeaderName, AsciiString> KNOWN_HEADER_NAMES = new HashMap<>(200);

    static {
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCEPT, HttpHeaderNames.ACCEPT);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCEPT_CHARSET, HttpHeaderNames.ACCEPT_CHARSET);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS,
            HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCESS_CONTROL_ALLOW_HEADERS,
            HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCESS_CONTROL_ALLOW_METHODS,
            HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCESS_CONTROL_EXPOSE_HEADERS,
            HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCESS_CONTROL_MAX_AGE, HttpHeaderNames.ACCESS_CONTROL_MAX_AGE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCEPT_ENCODING, HttpHeaderNames.ACCEPT_ENCODING);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCEPT_LANGUAGE, HttpHeaderNames.ACCEPT_LANGUAGE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCEPT_PATCH, HttpHeaderNames.ACCEPT_PATCH);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ACCEPT_RANGES, HttpHeaderNames.ACCEPT_RANGES);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.AGE, HttpHeaderNames.AGE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ALLOW, HttpHeaderNames.ALLOW);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.AUTHORIZATION, HttpHeaderNames.AUTHORIZATION);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CACHE_CONTROL, HttpHeaderNames.CACHE_CONTROL);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONNECTION, HttpHeaderNames.CONNECTION);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONTENT_DISPOSITION, HttpHeaderNames.CONTENT_DISPOSITION);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONTENT_ENCODING, HttpHeaderNames.CONTENT_ENCODING);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONTENT_LANGUAGE, HttpHeaderNames.CONTENT_LANGUAGE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONTENT_LENGTH, HttpHeaderNames.CONTENT_LENGTH);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONTENT_LOCATION, HttpHeaderNames.CONTENT_LOCATION);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONTENT_MD5, HttpHeaderNames.CONTENT_MD5);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONTENT_RANGE, HttpHeaderNames.CONTENT_RANGE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.CONTENT_TYPE, HttpHeaderNames.CONTENT_TYPE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.COOKIE, HttpHeaderNames.COOKIE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.DATE, HttpHeaderNames.DATE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ETAG, HttpHeaderNames.ETAG);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.EXPECT, HttpHeaderNames.EXPECT);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.EXPIRES, HttpHeaderNames.EXPIRES);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.FROM, HttpHeaderNames.FROM);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.HOST, HttpHeaderNames.HOST);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.IF_MATCH, HttpHeaderNames.IF_MATCH);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.IF_MODIFIED_SINCE, HttpHeaderNames.IF_MODIFIED_SINCE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.IF_NONE_MATCH, HttpHeaderNames.IF_NONE_MATCH);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.IF_RANGE, HttpHeaderNames.IF_RANGE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.IF_UNMODIFIED_SINCE, HttpHeaderNames.IF_UNMODIFIED_SINCE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.LAST_MODIFIED, HttpHeaderNames.LAST_MODIFIED);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.LOCATION, HttpHeaderNames.LOCATION);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.MAX_FORWARDS, HttpHeaderNames.MAX_FORWARDS);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.ORIGIN, HttpHeaderNames.ORIGIN);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.PRAGMA, HttpHeaderNames.PRAGMA);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.PROXY_AUTHENTICATE, HttpHeaderNames.PROXY_AUTHENTICATE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.PROXY_AUTHORIZATION, HttpHeaderNames.PROXY_AUTHORIZATION);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.RANGE, HttpHeaderNames.RANGE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.REFERER, HttpHeaderNames.REFERER);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.RETRY_AFTER, HttpHeaderNames.RETRY_AFTER);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.SERVER, HttpHeaderNames.SERVER);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.SET_COOKIE, HttpHeaderNames.SET_COOKIE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.TE, HttpHeaderNames.TE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.TRAILER, HttpHeaderNames.TRAILER);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.TRANSFER_ENCODING, HttpHeaderNames.TRANSFER_ENCODING);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.USER_AGENT, HttpHeaderNames.USER_AGENT);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.UPGRADE, HttpHeaderNames.UPGRADE);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.VARY, HttpHeaderNames.VARY);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.VIA, HttpHeaderNames.VIA);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.WARNING, HttpHeaderNames.WARNING);
        KNOWN_HEADER_NAMES.put(HttpHeaderName.WWW_AUTHENTICATE, HttpHeaderNames.WWW_AUTHENTICATE);
    }

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

        AsciiString asciiString = KNOWN_HEADER_NAMES.get(name);
        if (asciiString != null) {
            return asciiString;
        }

        if (DYNAMIC_HEADER_NAMES.size() > 10000) {
            DYNAMIC_HEADER_NAMES.clear();
        }

        return DYNAMIC_HEADER_NAMES.computeIfAbsent(name, n -> new AsciiString(n.getHttp1Name()));
    }

    private Utility() {
    }
}
