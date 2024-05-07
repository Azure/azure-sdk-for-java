// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Subscriber that writes a stream of {@link ByteBuf ByteBufs} to a {@link WritableByteChannel}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public class ByteBufWriteSubscriber implements Subscriber<ByteBuf> {
    private final ExceptionThrowingConsumer<ByteBuffer> writer;
    private final MonoSink<Void> emitter;
    private final int bufferSize;
    private final ByteBuf buffer;

    // This subscriber is effectively synchronous so there is no need for these fields to be volatile.
    private Subscription subscription;
    private boolean done = false;

    /**
     * Creates a new {@link ByteBufWriteSubscriber}.
     *
     * @param writer Where to write the {@link ByteBuf ByteBufs}.
     * @param emitter {@link MonoSink} to emit completion or error signals.
     * @param bodySize The size of the request body, if known.
     */
    public ByteBufWriteSubscriber(ExceptionThrowingConsumer<ByteBuffer> writer, MonoSink<Void> emitter, Long bodySize) {
        this.writer = writer;
        this.emitter = emitter;
        // Create a writing buffer that has a minimum bound of 8KB and a maximum bound of 64KB.
        // This is safe as the writer performs writes synchronously.
        this.bufferSize = (bodySize == null) ? 65536 : (int) Math.max(8192, Math.min(bodySize, 65536));
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
        if (done) {
            // The subscription has indicated completion, don't allow erroneous onNext emissions to be processed.
            Operators.onNextDropped(bytes, Context.of(emitter.contextView()));
            return;
        }

        if (!bytes.isReadable()) {
            // Nothing to process, request the next emission.
            subscription.request(1);
            return;
        }

        if (bytes.readableBytes() > bufferSize) {
            // If the next ByteBuf is larger than the buffer write the buffer, if there is any data to write, then
            // write the passed ByteBuf without buffering.
            if (buffer.readableBytes() > 0) {
                write(buffer);
                buffer.clear();
            }

            write(bytes);
        } else if (buffer.writableBytes() >= bytes.readableBytes()) {
            // If the buffer can contain the next ByteBuf buffer it.
            buffer.writeBytes(bytes);
        } else {
            // If the next ByteBuf can't fit in the buffer write the buffer then buffer the passed ByteBuf.
            write(buffer);
            buffer.clear();
            buffer.writeBytes(bytes);
        }

        // Request the next ByteBuf.
        if (!done) {
            subscription.request(1);
        }
    }

    private void write(ByteBuf byteBuf) {
        ByteBuffer byteBuffer = byteBuf.nioBuffer();
        try {
            while (byteBuffer.hasRemaining()) {
                writer.consume(byteBuffer);
            }
        } catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (done) {
            Operators.onErrorDropped(throwable, Context.of(emitter.contextView()));
            return;
        }

        done = true;
        buffer.release();
        subscription.cancel();
        emitter.error(throwable);
    }

    @Override
    public void onComplete() {
        if (done) {
            // Already completed, just return as there is no cleanup processing to do.
            return;
        }

        if (buffer.readableBytes() > 0) {
            // If there is still buffered data when the ByteBuf stream completes write it before emitting completion.
            write(buffer);
            if (done) {
                // In case the last write fails don't double release the buffer or emit success after emitting error.
                return;
            }
        }

        done = true;
        buffer.release();
        emitter.success();
    }
}
