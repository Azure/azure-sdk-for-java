// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.util.BinaryData;
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
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.core.http.netty.implementation.Utility.closeConnection;
import static com.azure.core.http.netty.implementation.Utility.deepCopyBuffer;

/**
 * Default HTTP response for Reactor Netty.
 */
public final class NettyAsyncHttpResponse extends NettyAsyncHttpResponseBase {
    private final Connection reactorNettyConnection;
    private final boolean disableBufferCopy;

    public NettyAsyncHttpResponse(HttpClientResponse reactorNettyResponse, Connection reactorNettyConnection,
        HttpRequest httpRequest, boolean disableBufferCopy) {
        super(reactorNettyResponse, httpRequest);
        this.reactorNettyConnection = reactorNettyConnection;
        this.disableBufferCopy = disableBufferCopy;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return bodyIntern().doFinally(ignored -> close())
            .map(byteBuf -> this.disableBufferCopy ? byteBuf.nioBuffer() : deepCopyBuffer(byteBuf));
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        return BinaryDataHelper.createBinaryData(new FluxByteBufferContent(getBody()));
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
    public Mono<InputStream> getBodyAsInputStream() {
        return bodyIntern().aggregate().asInputStream();
    }

    @Override
    public void close() {
        closeConnection(reactorNettyConnection);
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        // TODO (kasobol-msft) handle other cases optimizations from ImplUtils.writeByteBufferToStream.
        // However it seems that buffers here don't have backing arrays. And for files we should probably have
        // writeTo(Channel) API.
        byte[] buffer = new byte[8 * 1024];
        bodyIntern()
            .retain()
            // https://github.com/reactor/reactor-netty/issues/2096#issuecomment-1068832894
            .publishOn(Schedulers.boundedElastic())
            .map(byteBuff -> {
                try {
                    while (byteBuff.isReadable()) {
                        // TODO (kasobol-msft) this could be optimized further,i.e. make sure we're utilizing
                        // whole buffer before passing to outputstream.
                        int numberOfBytes = Math.min(buffer.length, byteBuff.readableBytes());
                        byteBuff.readBytes(buffer, 0, numberOfBytes);
                        outputStream.write(buffer, 0, numberOfBytes);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } finally {
                    byteBuff.release();
                }
                return byteBuff;
            }).blockLast();
    }

    @Override
    public Mono<Void> writeBodyTo(AsynchronousFileChannel asynchronousFileChannel, long position) {
        // The code below uses ByteBuff.retain/release internally to make sure that buffers are not reclaimed
        // before AsynchronousFileChannel finishes async writes.
        /*
        TODO (kasobol-msft) this implementation process buffer by buffer to keep track of write position.
        We should consider a position tracker that's based on buffer sizes and then issue multiple
        simultaneous writes.
         */
        return Utility.writeFile(
            bodyIntern().doFinally(ignored -> close()),
            asynchronousFileChannel,
            position
        );
    }

    @Override
    public void writeBodyTo(FileChannel fileChannel, long position) {
        AtomicLong currentPosition = new AtomicLong(position);
        bodyIntern()
            .retain()
            // https://github.com/reactor/reactor-netty/issues/2096#issuecomment-1068832894
            .publishOn(Schedulers.boundedElastic())
            .map(buffer -> {
                try {
                    ByteBuffer nioBuffer = buffer.nioBuffer();
                    while (nioBuffer.hasRemaining()) {
                        int written = fileChannel.write(nioBuffer, currentPosition.get());
                        currentPosition.addAndGet(written);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } finally {
                    buffer.release();
                }
                return buffer;
            }).blockLast();
    }

    private ByteBufFlux bodyIntern() {
        return reactorNettyConnection.inbound().receive();
    }

    // used for testing only
    public Connection internConnection() {
        return reactorNettyConnection;
    }
}
