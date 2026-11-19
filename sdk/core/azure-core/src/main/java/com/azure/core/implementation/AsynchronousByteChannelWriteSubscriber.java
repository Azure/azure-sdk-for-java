// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a {@link AsynchronousByteChannel}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class AsynchronousByteChannelWriteSubscriber
    implements Subscriber<ByteBuffer>, CompletionHandler<Integer, ByteBuffer>, Disposable {
    private static final ClientLogger LOGGER = new ClientLogger(AsynchronousByteChannelWriteSubscriber.class);

    private final AsynchronousByteChannel channel;
    private final MonoSink<Void> emitter;

    private static final AtomicReferenceFieldUpdater<AsynchronousByteChannelWriteSubscriber, Subscription> SUBSCRIPTION
        = AtomicReferenceFieldUpdater.newUpdater(AsynchronousByteChannelWriteSubscriber.class, Subscription.class,
            "subscription");
    private final AtomicInteger wip = new AtomicInteger();
    private volatile Subscription subscription;
    private volatile ByteBuffer buffer;
    private volatile boolean requested;
    private volatile boolean writePending;
    private volatile boolean sourceDone;
    private volatile boolean cancelled;
    private volatile boolean terminated;
    private volatile Throwable error;

    /**
     * Creates a subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a
     * {@link AsynchronousByteChannel}.
     *
     * @param channel The channel to write the stream of {@link ByteBuffer ByteBuffers} to.
     * @param emitter The emitter to signal when the stream has been written, or an error has occurred.
     */
    public AsynchronousByteChannelWriteSubscriber(AsynchronousByteChannel channel, MonoSink<Void> emitter) {
        this.channel = channel;
        this.emitter = emitter;
        emitter.onCancel(this);
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (Operators.setOnce(SUBSCRIPTION, this, s)) {
            drain();
        }
    }

    @Override
    public void onNext(ByteBuffer bytes) {
        if (sourceDone || cancelled || terminated) {
            Operators.onNextDropped(bytes, Context.of(emitter.contextView()));
            return;
        }

        // The producer may reclaim its storage as soon as onNext returns.
        buffer = ByteBuffer.wrap(FluxUtil.byteBufferToArray(bytes));
        requested = false;
        drain();
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        writePending = false;
        drain();
    }

    @Override
    public void failed(Throwable throwable, ByteBuffer attachment) {
        if (cancelled || terminated) {
            Operators.onErrorDropped(throwable, Context.of(emitter.contextView()));
        } else {
            error = throwable;
            sourceDone = true;
            Operators.terminate(SUBSCRIPTION, this);
        }
        buffer = null;
        writePending = false;
        drain();
    }

    @Override
    public void onError(Throwable throwable) {
        if (sourceDone || cancelled || terminated) {
            Operators.onErrorDropped(throwable, Context.of(emitter.contextView()));
            return;
        }

        error = throwable;
        sourceDone = true;
        Operators.terminate(SUBSCRIPTION, this);
        drain();
    }

    @Override
    public void onComplete() {
        sourceDone = true;
        drain();
    }

    @Override
    public void dispose() {
        cancelled = true;
        Operators.terminate(SUBSCRIPTION, this);
        drain();
    }

    @Override
    public boolean isDisposed() {
        return cancelled || terminated;
    }

    private void drain() {
        // Completion handlers may run inline. Serialize writes and requests without recursive callback chains.
        if (wip.getAndIncrement() != 0) {
            return;
        }

        int missed = 1;
        do {
            if (cancelled) {
                if (!writePending) {
                    buffer = null;
                }
            } else if (!terminated && !writePending) {
                ByteBuffer current = buffer;
                if (current != null && current.hasRemaining()) {
                    writePending = true;
                    try {
                        channel.write(current, current, this);
                    } catch (RuntimeException ex) {
                        failed(ex, current);
                    }
                } else {
                    buffer = null;
                    if (sourceDone) {
                        terminated = true;
                        if (error == null) {
                            emitter.success();
                        } else {
                            emitter.error(LOGGER.logThrowableAsError(error));
                        }
                    } else if (!requested && subscription != null) {
                        requested = true;
                        subscription.request(1);
                    }
                }
            }
            missed = wip.addAndGet(-missed);
        } while (missed != 0);
    }
}
