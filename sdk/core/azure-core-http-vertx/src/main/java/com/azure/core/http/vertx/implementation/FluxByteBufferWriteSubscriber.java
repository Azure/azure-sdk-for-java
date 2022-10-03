// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.util.ProgressReporter;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

import java.nio.ByteBuffer;

/**
 * Subscriber that writes a steam of {@link ByteBuffer ByteBuffers} to a Vertx {@link HttpClientRequest}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class FluxByteBufferWriteSubscriber implements Subscriber<ByteBuffer> {
    private final MonoSink<?> emitter;
    private final HttpClientRequest vertxRequest;
    private final ProgressReporter progressReporter;

    private Subscription subscription;

    /**
     * Creates a new instance of {@link FluxByteBufferWriteSubscriber}.
     *
     * @param emitter The {@link MonoSink} that will be used if an error occurs during writing of the
     * {@link ByteBuffer ByteBuffers}.
     * @param vertxRequest The Vertx {@link HttpClientRequest} that is consuming the {@link ByteBuffer ByteBuffers} as
     * the request body.
     * @param progressReporter The {@link ProgressReporter} that will report data being written to the request.
     */
    public FluxByteBufferWriteSubscriber(MonoSink<?> emitter, HttpClientRequest vertxRequest,
        ProgressReporter progressReporter) {
        this.emitter = emitter;
        this.vertxRequest = vertxRequest;
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
    public void onNext(ByteBuffer byteBuffer) {
        int remaining = byteBuffer.remaining();
        if (vertxRequest.writeQueueFull()) {
            vertxRequest.write(Buffer.buffer(Unpooled.wrappedBuffer(byteBuffer)))
                .onSuccess(ignored -> {
                    reportProgress(remaining);
                    subscription.request(1);
                }); // Request next once the previous write completes.
        } else {
            vertxRequest.write(Buffer.buffer(Unpooled.wrappedBuffer(byteBuffer)))
                .onSuccess(ignored -> reportProgress(remaining));
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        subscription.cancel();
        emitter.error(throwable);
    }

    @Override
    public void onComplete() {
        // Flux<ByteBuffer> completed, mark the Vertx HttpClientRequest body as ended.
        vertxRequest.end();
    }

    private void reportProgress(int progress) {
        if (progressReporter != null) {
            progressReporter.reportProgress(progress);
        }
    }
}
