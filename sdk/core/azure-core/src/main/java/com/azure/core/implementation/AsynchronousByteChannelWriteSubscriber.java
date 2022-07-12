// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a {@link AsynchronousByteChannel}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class AsynchronousByteChannelWriteSubscriber implements Subscriber<ByteBuffer> {

    private static final ClientLogger LOGGER = new ClientLogger(AsynchronousByteChannelWriteSubscriber.class);

    // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
    // An I/O pool thread will write to isWriting and read isCompleted,
    // while another thread may read isWriting and write to isCompleted.
    private volatile boolean isWriting = false;
    private volatile boolean isCompleted = false;

    private final AsynchronousByteChannel channel;
    private final MonoSink<Void> emitter;

    private Subscription subscription;

    public AsynchronousByteChannelWriteSubscriber(AsynchronousByteChannel channel, MonoSink<Void> emitter) {
        this.channel = channel;
        this.emitter = emitter;
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
    public void onNext(ByteBuffer bytes) {
        try {
            if (isWriting) {
                onError(new IllegalStateException("Received onNext while processing another write operation."));
            } else {
                write(bytes);
            }
        } catch (Exception ex) {
            // If writing has an error, and it isn't caught, there is a possibility for it to deadlock the reactive
            // stream. Catch the exception and propagate it manually so that doesn't happen.
            onError(ex);
        }
    }

    private void write(ByteBuffer bytes) {
        isWriting = true;

        channel.write(bytes, bytes, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {

                if (bytes.hasRemaining()) {
                    // If the entire ByteBuffer hasn't been written send it to be written again until it completes.
                    write(bytes);
                } else {
                    isWriting = false;
                    if (isCompleted) {
                        emitter.success();
                    } else {
                        subscription.request(1);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                onError(exc);
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {
        isWriting = false;
        subscription.cancel();
        emitter.error(LOGGER.logThrowableAsError(throwable));
    }

    @Override
    public void onComplete() {
        isCompleted = true;
        if (!isWriting) {
            emitter.success();
        }
    }
}
