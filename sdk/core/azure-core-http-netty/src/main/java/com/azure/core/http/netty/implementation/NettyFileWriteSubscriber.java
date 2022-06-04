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
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a file.
 */
// TODO (kasobol-msft) this class is a copy of FileWriteSubscriber with extra ByteBuf handling. Find better solution.
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class NettyFileWriteSubscriber implements Subscriber<ByteBuf> {

    // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
    // An I/O pool thread will write to isWriting and read isCompleted,
    // while another thread may read isWriting and write to isCompleted.
    private volatile boolean isWriting = false;
    private volatile boolean isCompleted = false;

    // FileWriteSubscriber is a commonly used subscriber, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(NettyFileWriteSubscriber.class);

    private final AsynchronousFileChannel fileChannel;
    private final AtomicLong position;
    private final MonoSink<Void> emitter;

    private Subscription subscription;

    public NettyFileWriteSubscriber(AsynchronousFileChannel fileChannel, long position, MonoSink<Void> emitter) {
        this.fileChannel = fileChannel;
        this.position = new AtomicLong(position);
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
        try {
            bytes = bytes.retain();
            if (isWriting) {
                onError(new IllegalStateException("Received onNext while processing another write operation."));
            } else {
                write(bytes, bytes.nioBuffer());
            }
        } catch (Throwable throwable) {
            // If writing has an error, and it isn't caught, there is a possibility for it to deadlock the reactive
            // stream. Catch the exception and propagate it manually so that doesn't happen.
            bytes.release();
            onError(throwable);
        }
    }

    private void write(ByteBuf nettyBytes, ByteBuffer nioBytes) {
        isWriting = true;
        fileChannel.write(nioBytes, position.get(), nettyBytes, new CompletionHandler<Integer, ByteBuf>() {
            @Override
            public void completed(Integer result, ByteBuf attachment) {
                position.addAndGet(result);

                if (nioBytes.hasRemaining()) {
                    // If the entire ByteBuffer hasn't been written send it to be written again until it completes.
                    write(nettyBytes, nioBytes);
                } else {
                    nettyBytes.release();
                    isWriting = false;
                    if (isCompleted) {
                        emitter.success();
                    } else {
                        subscription.request(1);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuf attachment) {
                attachment.release();
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
