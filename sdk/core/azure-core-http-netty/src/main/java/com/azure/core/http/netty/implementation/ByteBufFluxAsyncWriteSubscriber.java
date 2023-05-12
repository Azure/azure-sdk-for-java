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
import java.nio.channels.CompletionHandler;

/**
 * Subscriber that writes a stream of {@link ByteBuf ByteBufs} to a {@link AsynchronousByteChannel}.
 */
public final class ByteBufFluxAsyncWriteSubscriber implements Subscriber<ByteBuf> {
    private static final ClientLogger LOGGER = new ClientLogger(ByteBufFluxAsyncWriteSubscriber.class);

    // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
    // An I/O pool thread will write to isWriting and read isCompleted,
    // while another thread may read isWriting and write to isCompleted.
    private volatile boolean isWriting = false;
    private volatile boolean isCompleted = false;
    private final ByteBuf[] queue = new ByteBuf[2];
    private int queuePosition = 0;

    private final AsynchronousByteChannel channel;
    private final MonoSink<Void> emitter;
    private Subscription subscription;


    public ByteBufFluxAsyncWriteSubscriber(AsynchronousByteChannel channel, MonoSink<Void> emitter) {
        this.channel = channel;
        this.emitter = emitter;
    }

    @Override
    public void onSubscribe(Subscription s) {
        // Only set the Subscription if one has not been previously set.
        // Any additional Subscriptions will be cancelled.
        if (Operators.validate(this.subscription, s)) {
            subscription = s;

            s.request(2);
        }
    }

    @Override
    public void onNext(ByteBuf byteBuf) {
        if (queuePosition == 2) {
            onError(new IllegalStateException("Received onNext while processing another write operation."));
        }

        isWriting = true;
        // While the queue position is less than 2 more ByteBufs can be accepted.
        if (queuePosition < 2) {
            queue[queuePosition++] = byteBuf;
            return;
        }

        try {
            write(queue[0], false);
            write(queue[1], true);
        } catch (Exception ex) {
            // If writing has an error, and it isn't caught, there is a possibility for it to deadlock the reactive
            // stream. Catch the exception and propagate it manually so that doesn't happen.
            onError(ex);
        }
    }

    private void write(ByteBuf bytes, boolean requestMore) {
        ByteBuffer nioBytes = bytes.nioBuffer();
        channel.write(nioBytes, bytes, new CompletionHandler<Integer, ByteBuf>() {
            @Override
            public void completed(Integer result, ByteBuf attachment) {

                if (nioBytes.hasRemaining()) {
                    // If the entire ByteBuffer hasn't been written send it to be written again until it completes.
                    write(attachment, requestMore);
                } else {
                    isWriting = !requestMore;
                    safeRelease(attachment);
                    if (isCompleted) {
                        emitter.success();
                    } else if (requestMore) {
                        queuePosition = 0;
                        subscription.request(2);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuf attachment) {
                safeRelease(attachment);
                onError(exc);
            }
        });
    }

    private static void safeRelease(ByteBuf byteBuf) {
        if (byteBuf != null && byteBuf.refCnt() > 0) {
            byteBuf.release();
        }
    }

    @Override
    public void onError(Throwable t) {
        isWriting = false;
        safeRelease(queue[0]);
        safeRelease(queue[1]);
        subscription.cancel();
        emitter.error(LOGGER.logThrowableAsError(t));
    }

    @Override
    public void onComplete() {
        isCompleted = true;
        if (!isWriting) {
            emitter.success();
        }
    }
}
