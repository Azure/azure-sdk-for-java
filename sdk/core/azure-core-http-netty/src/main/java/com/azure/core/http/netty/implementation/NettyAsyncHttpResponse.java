// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.CoreUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
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
        int bufferSize = getBufferSize(getHeaders());
        ByteBuf buf1 = PooledByteBufAllocator.DEFAULT.buffer(bufferSize);
        ByteBuf buf2 = PooledByteBufAllocator.DEFAULT.buffer(bufferSize);

        return Mono.<Void>create(
            sink -> bodyIntern().subscribe(new ByteBufAsyncWriteSubscriber(channel, sink, buf1, buf2)))
            .doFinally(ignored -> {
                buf1.release();
                buf2.release();
                close();
            });
    }

    @Override
    public void writeBodyTo(WritableByteChannel channel) {
        int bufferSize = getBufferSize(getHeaders());
        ByteBuf buf1 = PooledByteBufAllocator.DEFAULT.buffer(bufferSize);
        ByteBuf buf2 = PooledByteBufAllocator.DEFAULT.buffer(bufferSize);

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
        try {
            Mono.<Void>create(sink -> bodyIntern().subscribe(new ByteBufWriteSubscriber(channel, sink, buf1, buf2)))
                .subscribeOn(Schedulers.boundedElastic())
                .doFinally(ignored -> close())
                .block();
        } finally {
            buf1.release();
            buf2.release();
        }
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

    private static int getBufferSize(HttpHeaders headers) {
        Long bodySize = null;
        String contentLength = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (contentLength != null) {
            try {
                bodySize = Long.parseLong(contentLength);
            } catch (NumberFormatException ex) {
                // Don't let NumberFormatException fail the response as this is only optional.
            }
        }

        int bufferSize = Utility.getByteBufSubscriberBufferSize(bodySize);

        // Return either the buffer size or the minimum of buffer size and body size.
        // This can reduce the buffer size to a smaller value resulting in fewer allocations.
        return (bodySize != null) ? (int) Math.min(bufferSize, bodySize) : bufferSize;
    }
}
