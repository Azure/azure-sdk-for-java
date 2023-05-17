// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

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
    private final AsynchronousByteChannel channel;
    private final MonoSink<Void> emitter;

    private Subscription subscription;

    public ByteBufAsyncWriteSubscriber(AsynchronousByteChannel channel, MonoSink<Void> emitter) {
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
    public void onNext(ByteBuf bytes) {
        write(bytes.nioBuffer());
    }

    private void write(ByteBuffer bytes) {
        try {
            while (bytes.hasRemaining()) {
                channel.write(bytes).get();
            }

            subscription.request(1);
        } catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        subscription.cancel();
        emitter.error(throwable);
    }

    @Override
    public void onComplete() {
        emitter.success();
    }
}
