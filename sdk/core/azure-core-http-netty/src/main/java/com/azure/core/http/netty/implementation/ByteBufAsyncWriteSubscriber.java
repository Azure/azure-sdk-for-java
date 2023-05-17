// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;

/**
 * Subscriber that writes a stream of {@link ByteBuf ByteBufs} to a {@link AsynchronousByteChannel}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class ByteBufAsyncWriteSubscriber implements Subscriber<ByteBuf> {
    private static final ClientLogger LOGGER = new ClientLogger(ByteBufAsyncWriteSubscriber.class);

    // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
    // An I/O pool thread will write to isWriting and read isCompleted,
    // while another thread may read isWriting and write to isCompleted.
    private volatile boolean isWriting = false;
    private volatile boolean failed = false;
    private volatile boolean isCompleted = false;

    private final AsynchronousByteChannel channel;
    private final MonoSink<Void> emitter;

    private Subscription subscription;
    private ByteBuf activeBuffer;
    private ByteBuf nextBuffer;

    public ByteBufAsyncWriteSubscriber(AsynchronousByteChannel channel, MonoSink<Void> emitter, ByteBuf buf1,
        ByteBuf buf2) {
        this.channel = channel;
        this.emitter = emitter;
        this.activeBuffer = buf1;
        this.nextBuffer = buf2;
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
        try {
            int readableBytes = bytes.readableBytes();
            if (readableBytes <= activeBuffer.writableBytes()) {
                activeBuffer.writeBytes(bytes);
                subscription.request(1);
                return;
            }

            if (isWriting) {
                onError(new IllegalStateException("Received onNext while processing another write operation."));
            } else {
                ByteBuf writeBuf = activeBuffer;
                activeBuffer = nextBuffer;
                nextBuffer = writeBuf;
                activeBuffer.writeBytes(bytes);
                write(writeBuf);
            }
        } catch (Exception ex) {
            // If writing has an error, and it isn't caught, there is a possibility for it to deadlock the reactive
            // stream. Catch the exception and propagate it manually so that doesn't happen.
            onError(ex);
        }
    }

    private void write(ByteBuf bytes) {
        isWriting = true;

        ByteBuffer byteBuffer = bytes.nioBuffer();
        try {
            while (byteBuffer.hasRemaining()) {
                channel.write(byteBuffer).get();
            }
        } catch (Exception ex) {
            onError(ex);
            return;
        }
        bytes.clear();

        isWriting = false;
        if (isCompleted) {
            emitter.success();
        } else {
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        isWriting = false;
        failed = true;
        subscription.cancel();
        emitter.error(LOGGER.logThrowableAsError(throwable));
    }

    @Override
    public void onComplete() {
        boolean failed = this.failed;
        isCompleted = !failed;
        if (failed) {
            return;
        }

        if (activeBuffer.readableBytes() > 0) {
            write(activeBuffer);
        } else if (!isWriting) {
            emitter.success();
        }
    }
}
