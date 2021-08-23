// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ReferenceManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClientResponse;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static com.azure.core.http.netty.implementation.Utility.closeConnection;

/**
 * Default HTTP response for Reactor Netty.
 */
public final class NettyAsyncHttpResponse extends NettyAsyncHttpResponseBase {
    private static final ReferenceManager REFERENCE_MANAGER = ReferenceManager.create();

    private final Connection reactorNettyConnection;

    public NettyAsyncHttpResponse(HttpClientResponse reactorNettyResponse, Connection reactorNettyConnection,
        HttpRequest httpRequest) {
        super(reactorNettyResponse, httpRequest);
        this.reactorNettyConnection = reactorNettyConnection;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return bodyIntern().doFinally(ignored -> close())
            .map(byteBuf -> {
                byteBuf.retain();
                ByteBuffer buffer = byteBuf.nioBuffer();
                REFERENCE_MANAGER.register(buffer, byteBuf::release);

                return buffer;
            });
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return bodyIntern().aggregate().asByteArray().doFinally(ignored -> close());
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue("Content-Type")));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return bodyIntern().aggregate().asString(charset).doFinally(ignored -> close());
    }

    @Override
    public void close() {
        closeConnection(reactorNettyConnection);
    }

    private ByteBufFlux bodyIntern() {
        return reactorNettyConnection.inbound().receive();
    }

    // used for testing only
    public Connection internConnection() {
        return reactorNettyConnection;
    }
}
