// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.nio.ByteBuffer;

/**
 * Implementation of Vertx's {@link ReadStream} that handles sending a reactive payload.
 */
public final class FluxByteBufferReadStream implements ReadStream<ByteBuffer>, Subscriber<ByteBuffer> {
    @Override
    public ReadStream<ByteBuffer> exceptionHandler(Handler<Throwable> handler) {
        return null;
    }

    @Override
    public ReadStream<ByteBuffer> handler(Handler<ByteBuffer> handler) {
        return null;
    }

    @Override
    public ReadStream<ByteBuffer> pause() {
        return null;
    }

    @Override
    public ReadStream<ByteBuffer> resume() {
        return null;
    }

    @Override
    public ReadStream<ByteBuffer> fetch(long amount) {
        return null;
    }

    @Override
    public ReadStream<ByteBuffer> endHandler(Handler<Void> endHandler) {
        return null;
    }

    @Override
    public void onSubscribe(Subscription s) {

    }

    @Override
    public void onNext(ByteBuffer byteBuffer) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }
}
