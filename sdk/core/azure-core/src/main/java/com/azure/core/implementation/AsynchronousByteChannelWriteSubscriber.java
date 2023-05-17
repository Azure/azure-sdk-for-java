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
import java.util.concurrent.ExecutionException;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a {@link AsynchronousByteChannel}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class AsynchronousByteChannelWriteSubscriber implements Subscriber<ByteBuffer> {

    private static final ClientLogger LOGGER = new ClientLogger(AsynchronousByteChannelWriteSubscriber.class);

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
        write(bytes);
    }

    private void write(ByteBuffer bytes) {
        try {
            while (bytes.hasRemaining()) {
                channel.write(bytes).get();
            }

            subscription.request(1);
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
        subscription.cancel();
        emitter.error(LOGGER.logThrowableAsError(throwable));
    }

    @Override
    public void onComplete() {
        emitter.success();
    }
}
