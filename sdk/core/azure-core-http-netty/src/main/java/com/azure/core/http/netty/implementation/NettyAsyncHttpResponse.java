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

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import static com.azure.core.http.netty.implementation.NettyUtility.closeConnection;
import static com.azure.core.http.netty.implementation.NettyUtility.deepCopyBuffer;

/**
 * Default HTTP response for Reactor Netty.
 */
public final class NettyAsyncHttpResponse extends NettyAsyncHttpResponseBase {
    private final Connection reactorNettyConnection;
    private final boolean disableBufferCopy;

    /**
     * Creates a new response.
     *
     * @param reactorNettyResponse The Reactor Netty HTTP response.
     * @param reactorNettyConnection The Reactor Netty connection.
     * @param httpRequest The HTTP request that initiated this response.
     * @param disableBufferCopy Whether to disable copying the response body into a new buffer.
     * @param headersEagerlyConverted Whether the headers were eagerly converted.
     */
    public NettyAsyncHttpResponse(HttpClientResponse reactorNettyResponse, Connection reactorNettyConnection,
        HttpRequest httpRequest, boolean disableBufferCopy, boolean headersEagerlyConverted) {
        super(reactorNettyResponse, httpRequest, headersEagerlyConverted);
        this.reactorNettyConnection = reactorNettyConnection;
        this.disableBufferCopy = disableBufferCopy;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return Flux.using(() -> this,
            response -> response.bodyIntern()
                .map(byteBuf -> this.disableBufferCopy ? byteBuf.nioBuffer() : deepCopyBuffer(byteBuf)),
            NettyAsyncHttpResponse::close);
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.using(() -> this, response -> response.bodyIntern().aggregate().asByteArray(),
            NettyAsyncHttpResponse::close);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray()
            .map(bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue(HttpHeaderName.CONTENT_TYPE)));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return Mono.using(() -> this, response -> response.bodyIntern().aggregate().asString(charset),
            NettyAsyncHttpResponse::close);
    }

    @Override
    public Mono<InputStream> getBodyAsInputStream() {
        return Mono.using(() -> this, response -> response.bodyIntern().aggregate().asInputStream(),
            NettyAsyncHttpResponse::close);
    }

    @Override
    public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
        Long length = getContentLength();
        return Mono.using(() -> this,
            response -> Mono.create(sink -> response.bodyIntern()
                .subscribe(new ByteBufWriteSubscriber(byteBuffer -> channel.write(byteBuffer).get(), sink, length))),
            NettyAsyncHttpResponse::close);
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
        Mono.using(() -> this,
            response -> Mono
                .<Void>create(sink -> response.bodyIntern()
                    .subscribe(new ByteBufWriteSubscriber(channel::write, sink, getContentLength())))
                .subscribeOn(Schedulers.boundedElastic()),
            NettyAsyncHttpResponse::close).block();
    }

    @Override
    public void close() {
        closeConnection(reactorNettyConnection);
    }

    private ByteBufFlux bodyIntern() {
        return reactorNettyConnection.inbound().receive();
    }

    // used for testing only
    /**
     * Gets the underlying Reactor Netty connection.
     *
     * @return The underlying Reactor Netty connection.
     */
    public Connection internConnection() {
        return reactorNettyConnection;
    }

    private Long getContentLength() {
        String contentLength = getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
        if (contentLength == null) {
            return null;
        }

        try {
            return Long.parseLong(contentLength);
        } catch (NumberFormatException ex) {
            // Don't let NumberFormatException fail reading the response as this is just a speculative check.
            return null;
        }
    }
}
