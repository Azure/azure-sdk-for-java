// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a {@link HttpClientRequest Vertx Request}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public class VertxRequestWriteSubscriber implements Subscriber<ByteBuffer> {
    private static final ClientLogger LOGGER = new ClientLogger(VertxRequestWriteSubscriber.class);

    private final HttpClientRequest request;
    private final MonoSink<HttpResponse> emitter;
    private final ProgressReporter progressReporter;

    // This subscriber is effectively synchronous so there is no need for these fields to be volatile.
    private Subscription subscription;

    private static final AtomicIntegerFieldUpdater<VertxRequestWriteSubscriber> WRITING
        = AtomicIntegerFieldUpdater.newUpdater(VertxRequestWriteSubscriber.class, "writing");
    private volatile int writing;

    private static final AtomicIntegerFieldUpdater<VertxRequestWriteSubscriber> DONE
        = AtomicIntegerFieldUpdater.newUpdater(VertxRequestWriteSubscriber.class, "done");
    private volatile int done;

    public VertxRequestWriteSubscriber(HttpClientRequest request, MonoSink<HttpResponse> emitter,
        ProgressReporter progressReporter) {
        this.request = request;
        this.emitter = emitter;
        this.progressReporter = progressReporter;
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
            if (!WRITING.compareAndSet(this, 0, 1)) {
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
        int remaining = bytes.remaining();
        request.write(Buffer.buffer(Unpooled.wrappedBuffer(bytes)), result -> {
            WRITING.set(this, 0);
            if (result.succeeded()) {
                if (progressReporter != null) {
                    progressReporter.reportProgress(remaining);
                }

                if (DONE.get(this) != 1) {
                    subscription.request(1);
                }
            } else {
                onError(result.cause());
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {
        if (!DONE.compareAndSet(this, 0, 1)) {
            Operators.onErrorDropped(throwable, Context.of(emitter.contextView()));
            return;
        }

        subscription.cancel();
        emitter.error(LOGGER.logThrowableAsError(throwable));
    }

    @Override
    public void onComplete() {
        if (!DONE.compareAndSet(this, 0, 1)) {
            // Already completed, just return as there is no cleanup processing to do.
            return;
        }

        request.end(result -> {
            if (result.failed()) {
                emitter.error(result.cause());
            }
        });
    }
}
