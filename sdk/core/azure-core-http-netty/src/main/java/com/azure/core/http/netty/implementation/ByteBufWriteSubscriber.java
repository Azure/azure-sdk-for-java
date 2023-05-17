// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Subscriber that writes a stream of {@link ByteBuf ByteBufs} to a {@link WritableByteChannel}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public class ByteBufWriteSubscriber implements Subscriber<ByteBuf> {
    private final ExceptionThrowingConsumer<ByteBuffer> writer;
    private final MonoSink<Void> emitter;
    private final ByteBuf buffer;

    private Subscription subscription;

    public ByteBufWriteSubscriber(ExceptionThrowingConsumer<ByteBuffer> writer, MonoSink<Void> emitter, Long bodySize) {
        this.writer = writer;
        this.emitter = emitter;
        // Create a buffer that is either 64KB or the minimum of the expected body size and 64KB.
        // This is safe as the writer performs writes synchronously.
        int bufferSize = (bodySize == null) ? 65536 : (int) Math.min(bodySize, 65536);
        this.buffer = PooledByteBufAllocator.DEFAULT.buffer(bufferSize);
    }

    @Override
    public void onSubscribe(Subscription s) {
        // Only set the Subscription if one has not been previously set.
        // Any additional Subscriptions will be cancelled.
        if (Operators.validate(this.subscription, s)) {
            subscription = s;

            s.request(1);
        }
    }

    @Override
    public void onNext(ByteBuf bytes) {
        if (buffer.writableBytes() >= bytes.readableBytes()) {
            buffer.writeBytes(bytes);
        } else {
            write();
            buffer.writeBytes(bytes);
        }

        subscription.request(1);
    }

    private void write() {
        ByteBuffer byteBuffer = buffer.nioBuffer();
        try {
            while (byteBuffer.hasRemaining()) {
                writer.consume(byteBuffer);
            }

            buffer.clear();
        } catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        buffer.release();
        subscription.cancel();
        emitter.error(throwable);
    }

    @Override
    public void onComplete() {
        if (buffer.readableBytes() > 0) {
            write();
        }
        buffer.release();
        emitter.success();
    }
}
