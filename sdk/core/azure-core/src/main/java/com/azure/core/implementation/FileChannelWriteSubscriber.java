// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a file.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class FileChannelWriteSubscriber implements Subscriber<ByteBuffer> {
    private final ClientLogger logger = new ClientLogger(FileChannelWriteSubscriber.class);

    private final FileChannel fileChannel;
    private final MonoSink<Void> emitter;

    private long position;
    private Subscription subscription;

    public FileChannelWriteSubscriber(FileChannel fileChannel, long position, MonoSink<Void> emitter) {
        this.fileChannel = fileChannel;
        this.position = position;
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
            write(bytes);
        } catch (Throwable throwable) {
            // If writing has an error, and it isn't caught, there is a possibility for it to deadlock the reactive
            // stream. Catch the exception and propagate it manually so that doesn't happen.
            onError(throwable);
        }
    }

    private void write(ByteBuffer bytes) throws IOException {
        long bytesWritten = fileChannel.write(bytes, position);
        position += bytesWritten;

        if (bytes.hasRemaining()) {
            // If the entire ByteBuffer hasn't been written send it to be written again until it completes.
            write(bytes);
        } else {
            subscription.request(1);
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
