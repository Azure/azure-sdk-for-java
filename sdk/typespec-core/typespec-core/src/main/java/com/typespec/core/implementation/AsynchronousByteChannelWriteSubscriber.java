// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.util.logging.ClientLogger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.util.concurrent.ExecutionException;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a {@link AsynchronousByteChannel}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class AsynchronousByteChannelWriteSubscriber implements Subscriber<ByteBuffer> {
    private static final ClientLogger LOGGER = new ClientLogger(AsynchronousByteChannelWriteSubscriber.class);

    private final AsynchronousByteChannel channel;
    private final MonoSink<Void> emitter;

    // This subscriber is effectively synchronous so there is no need for these fields to be volatile.
    private Subscription subscription;
    private boolean done = false;

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
        if (done) {
            // The subscription has indicated completion, don't allow erroneous onNext emissions to be processed.
            Operators.onNextDropped(bytes, Context.of(emitter.contextView()));
            return;
        }

        if (!bytes.hasRemaining()) {
            // Nothing to process, request the next emission.
            subscription.request(1);
            return;
        }

        write(bytes);

        // Request the next ByteBuffer.
        if (!done) {
            subscription.request(1);
        }
    }

    private void write(ByteBuffer bytes) {
        try {
            while (bytes.hasRemaining()) {
                channel.write(bytes).get();
            }
        } catch (Exception ex) {
            if (ex instanceof ExecutionException) {
                onError(ex.getCause());
            } else {
                onError(ex);
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (done) {
            Operators.onErrorDropped(throwable, Context.of(emitter.contextView()));
            return;
        }

        done = true;
        subscription.cancel();
        emitter.error(LOGGER.logThrowableAsError(throwable));
    }

    @Override
    public void onComplete() {
        if (done) {
            // Already completed, just return as there is no cleanup processing to do.
            return;
        }

        done = true;
        emitter.success();
    }
}
