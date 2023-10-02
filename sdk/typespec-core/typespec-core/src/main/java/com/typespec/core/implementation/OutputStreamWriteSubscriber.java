// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.typespec.core.implementation;

import com.typespec.core.util.logging.ClientLogger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to an {@link OutputStream}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class OutputStreamWriteSubscriber implements Subscriber<ByteBuffer> {
    private final MonoSink<Void> emitter;
    private final OutputStream stream;
    private final ClientLogger logger;

    private Subscription subscription;

    public OutputStreamWriteSubscriber(MonoSink<Void> emitter, OutputStream stream, ClientLogger logger) {
        this.emitter = emitter;
        this.stream = stream;
        this.logger = logger;
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
    public void onNext(ByteBuffer byteBuffer) {
        try {
            ImplUtils.writeByteBufferToStream(byteBuffer, stream);
            subscription.request(1);
        } catch (IOException ex) {
            // Emit IOExceptions as UncheckIOExceptions as that is the pattern used in SDKs.
            onError(new UncheckedIOException(ex));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        subscription.cancel();
        emitter.error(logger.logThrowableAsError(throwable));
    }

    @Override
    public void onComplete() {
        emitter.success();
    }
}
