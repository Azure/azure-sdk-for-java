// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClientResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import static com.azure.core.http.netty.implementation.Utility.closeConnection;
import static com.azure.core.http.netty.implementation.Utility.deepCopyBuffer;

/**
 * Default HTTP response for Reactor Netty.
 */
public final class NettyAsyncHttpResponse extends NettyAsyncHttpResponseBase {
    private final Connection reactorNettyConnection;
    private final boolean disableBufferCopy;

    public NettyAsyncHttpResponse(HttpClientResponse reactorNettyResponse, Connection reactorNettyConnection,
        HttpRequest httpRequest, boolean disableBufferCopy, boolean headersEagerlyConverted) {
        super(reactorNettyResponse, httpRequest, headersEagerlyConverted);
        this.reactorNettyConnection = reactorNettyConnection;
        this.disableBufferCopy = disableBufferCopy;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return bodyIntern()
            .map(byteBuf -> this.disableBufferCopy ? byteBuf.nioBuffer() : deepCopyBuffer(byteBuf))
            .doFinally(ignored -> close());
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return bodyIntern().aggregate().asByteArray().doFinally(ignored -> close());
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes,
            getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return bodyIntern().aggregate().asString(charset).doFinally(ignored -> close());
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        return bodyIntern().aggregate().asInputStream().doFinally(ignored -> close());
    }

    @Override
    public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
        return writeBodyTo(Channels.newOutputStream(channel));
    }

    @Override
    public void writeBodyTo(WritableByteChannel channel) {
        // Since this uses a synchronous write this doesn't need to retain the ByteBuf as the async version does.
        // In fact, if retain is used here and there is a cancellation or exception during writing this will likely
        // leak ByteBufs as it doesn't have the asynchronous stream handling to properly close them when errors happen
        // during writing.
        //
        // This also must use subscribeOn rather than publishOn as subscribeOn will have the entire asynchronous chain
        // run on the same subscriber where publishOn only affects reactive operations after the publishOn. While this
        // doesn't seem like it would make much of a difference as this entire call will be blocked it fixes a race
        // condition. reactor-netty's inbound receive uses a ByteBuf pool to handle the network response and the
        // returned ByteBufFlux is configured to release the ByteBuf back to the pool when the onNext operation
        // completes. Unfortunately, when publishOn is used each ByteBuf must be scheduled in the publisher thread using
        // a future and the publishOn onNext handler will complete once that operation is scheduled, not when it's
        // complete. This introduces a previously seen, but in a different flavor, race condition where the write
        // operation gets scheduled on one thread and the ByteBuf release happens on another, leaving the write
        // operation racing to complete before the release happens. With all that said, leave this as subscribeOn.
        writeBodyTo(Channels.newOutputStream(channel)).block();
    }

    private Mono<Void> writeBodyTo(OutputStream outputStream) {
        // Since this uses a synchronous write this doesn't need to retain the ByteBuf as the async version does.
        // In fact, if retain is used here and there is a cancellation or exception during writing this will likely
        // leak ByteBufs as it doesn't have the asynchronous stream handling to properly close them when errors happen
        // during writing.
        //
        // This also must use subscribeOn rather than publishOn as subscribeOn will have the entire asynchronous chain
        // run on the same subscriber where publishOn only affects reactive operations after the publishOn. While this
        // doesn't seem like it would make much of a difference as this entire call will be blocked it fixes a race
        // condition. reactor-netty's inbound receive uses a ByteBuf pool to handle the network response and the
        // returned ByteBufFlux is configured to release the ByteBuf back to the pool when the onNext operation
        // completes. Unfortunately, when publishOn is used each ByteBuf must be scheduled in the publisher thread using
        // a future and the publishOn onNext handler will complete once that operation is scheduled, not when it's
        // complete. This introduces a previously seen, but in a different flavor, race condition where the write
        // operation gets scheduled on one thread and the ByteBuf release happens on another, leaving the write
        // operation racing to complete before the release happens. With all that said, leave this as subscribeOn.
        return bodyIntern().subscribeOn(Schedulers.boundedElastic())
            .flatMap(byteBuf -> {
                try {
                    byteBuf.getBytes(0, outputStream, byteBuf.readableBytes());
                    return Mono.empty();
                } catch (IOException ex) {
                    return Mono.error(ex);
                }
            })
            .doFinally(ignored -> close())
            .then();
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
