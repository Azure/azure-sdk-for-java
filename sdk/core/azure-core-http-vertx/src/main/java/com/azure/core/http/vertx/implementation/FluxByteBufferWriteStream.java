// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import reactor.core.publisher.FluxSink;

import java.nio.ByteBuffer;

/**
 * Implementation of {@link WriteStream} which creates a {@code Flux<ByteBuffer>}.
 */
public final class FluxByteBufferWriteStream implements WriteStream<Buffer> {
    private final FluxSink<ByteBuffer> sink;

    /**
     * Creates a new instance of {@link FluxByteBufferWriteStream}.
     *
     * @param sink The {@link FluxSink} generating the {@code Flux<ByteBuffer>}.
     */
    public FluxByteBufferWriteStream(FluxSink<ByteBuffer> sink) {
        this.sink = sink;
    }

    @Override
    public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        return this;
    }

    @Override
    public Future<Void> write(Buffer data) {
        sink.next(data.getByteBuf().nioBuffer());
        return Future.succeededFuture();
    }

    @Override
    public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
        sink.next(data.getByteBuf().nioBuffer());
        handler.handle(Future.succeededFuture());
    }

    @Override
    public Future<Void> end() {
        sink.complete();
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> end(Buffer data) {
        sink.next(data.getByteBuf().nioBuffer());
        sink.complete();
        return Future.succeededFuture();
    }

    @Override
    public void end(Buffer data, Handler<AsyncResult<Void>> handler) {
        sink.next(data.getByteBuf().nioBuffer());
        sink.complete();
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        sink.complete();
        handler.handle(Future.succeededFuture());
    }

    @Override
    public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return false;
    }

    @Override
    public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
        return this;
    }
}
