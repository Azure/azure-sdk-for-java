/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.util;

import com.google.common.reflect.TypeToken;

import com.microsoft.rest.v2.http.UnexpectedLengthException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.FlowableTransformer;
import io.reactivex.Single;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.BackpressureHelper;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Contains helper methods for dealing with Flowables.
 */
public final class FlowableUtil {
    /**
     * Checks if a type is Flowable&lt;ByteBuffer&gt;.
     *
     * @param entityTypeToken the type to check
     * @return whether the type represents a Flowable that emits byte arrays
     */
    public static boolean isFlowableByteBuffer(TypeToken entityTypeToken) {
        if (entityTypeToken.isSubtypeOf(Flowable.class)) {
            final Type innerType = ((ParameterizedType) entityTypeToken.getType()).getActualTypeArguments()[0];
            final TypeToken innerTypeToken = TypeToken.of(innerType);
            if (innerTypeToken.isSubtypeOf(ByteBuffer.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collects byte buffers emitted by a Flowable into a byte array.
     * @param content A stream which emits byte buffers.
     * @return A Single which emits the concatenation of all the byte buffers given by the source Flowable.
     */
    public static Single<byte[]> collectBytesInArray(Flowable<ByteBuffer> content) {
        return content.collectInto(Unpooled.buffer(), ByteBuf::writeBytes).map(out -> {
            try {
                if (out.array().length == out.readableBytes()) {
                    return out.array();
                } else {
                    byte[] arr = new byte[out.readableBytes()];
                    out.readBytes(arr);
                    return arr;
                }
            } finally {
                out.release();
            }

        });
    }

    /**
     * Ensures the given Flowable emits the expected number of bytes.
     *
     * @param bytesExpected the number of bytes expected to be emitted
     * @return a Function which can be applied using {@link Flowable#compose}
     */
    public static FlowableTransformer<ByteBuffer, ByteBuffer> ensureLength(long bytesExpected) {
        return source -> Flowable.defer(new Callable<Publisher<? extends ByteBuffer>>() {
            long bytesRead = 0;

            @Override
            public Publisher<? extends ByteBuffer> call() throws Exception {
                return source.doOnNext(bb -> {
                    bytesRead += bb.remaining();
                    if (bytesRead > bytesExpected) {
                        throw new UnexpectedLengthException(
                                "Flowable<ByteBuffer> emitted more bytes than the expected " + bytesExpected,
                                bytesRead,
                                bytesExpected);
                    }
                }).doOnComplete(() -> {
                    if (bytesRead != bytesExpected) {
                        throw new UnexpectedLengthException(
                                String.format("Flowable<ByteBuffer> emitted %d bytes instead of the expected %d bytes.",
                                        bytesRead,
                                        bytesExpected),
                                bytesRead,
                                bytesExpected);
                    }
                });
            }
        });
    }

    /**
     * Collects byte buffers emitted by a Flowable into a ByteBuffer.
     * @param content A stream which emits byte arrays.
     * @return A Single which emits the concatenation of all the byte buffers given by the source Flowable.
     */
    public static Single<ByteBuffer> collectBytesInBuffer(Flowable<ByteBuffer> content) {
        return collectBytesInArray(content).map(ByteBuffer::wrap);
    }

    /**
     * Writes the bytes emitted by a Flowable to an AsynchronousFileChannel.
     *
     * @param content the Flowable content
     * @param outFile the file channel
     * @return a Completable which performs the write operation when subscribed
     */
    public static Completable writeFile(Flowable<ByteBuffer> content, AsynchronousFileChannel outFile) {
        return writeFile(content, outFile, 0);
    }

    /**
     * Writes the bytes emitted by a Flowable to an AsynchronousFileChannel
     * starting at the given position in the file.
     *
     * @param content the Flowable content
     * @param outFile the file channel
     * @param position the position in the file to begin writing
     * @return a Completable which performs the write operation when subscribed
     */
    public static Completable writeFile(Flowable<ByteBuffer> content, AsynchronousFileChannel outFile, long position) {
        return Completable.create(emitter -> content.subscribe(new FlowableSubscriber<ByteBuffer>() {
            // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
            // An I/O pool thread will write to isWriting and read isCompleted,
            // while another thread may read isWriting and write to isCompleted.
            volatile boolean isWriting = false;
            volatile boolean isCompleted = false;
            volatile Subscription subscription;
            volatile long pos = position;

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                s.request(1);
            }

            @Override
            public void onNext(ByteBuffer bytes) {
                isWriting = true;
                outFile.write(bytes, pos, null, onWriteCompleted);
            }


            CompletionHandler<Integer, Object> onWriteCompleted = new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer bytesWritten, Object attachment) {
                    isWriting = false;
                    if (isCompleted) {
                        emitter.onComplete();
                    }
                    //noinspection NonAtomicOperationOnVolatileField
                    pos += bytesWritten;
                    subscription.request(1);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    subscription.cancel();
                    emitter.onError(exc);
                }
            };

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
                emitter.onError(throwable);
            }

            @Override
            public void onComplete() {
                isCompleted = true;
                if (!isWriting) {
                    emitter.onComplete();
                }
            }
        }));
    }

    /**
     * Creates a {@link Flowable} from an {@link AsynchronousFileChannel}
     * which reads part of a file into chunks of the given size.
     *
     * @param fileChannel The file channel.
     * @param chunkSize the size of file chunks to read.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes to read from the file.
     * @return the Flowable.
     */
    public static Flowable<ByteBuffer> readFile(AsynchronousFileChannel fileChannel, int chunkSize, long offset, long length) {
        return new FileReadFlowable(fileChannel, chunkSize, offset, length);
    }

    /**
     * Creates a {@link Flowable} from an {@link AsynchronousFileChannel}
     * which reads part of a file.
     *
     * @param fileChannel The file channel.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes to read from the file.
     * @return the Flowable.
     */
    public static Flowable<ByteBuffer> readFile(AsynchronousFileChannel fileChannel, long offset, long length) {
        return readFile(fileChannel, DEFAULT_CHUNK_SIZE, offset, length);
    }

    /**
     * Creates a {@link Flowable} from an {@link AsynchronousFileChannel}
     * which reads the entire file.
     *
     * @param fileChannel The file channel.
     * @return The AsyncInputStream.
     */
    public static Flowable<ByteBuffer> readFile(AsynchronousFileChannel fileChannel) {
        try {
            long size = fileChannel.size();
            return readFile(fileChannel, DEFAULT_CHUNK_SIZE, 0, size);
        } catch (IOException e) {
            return Flowable.error(e);
        }
    }

    private static final int DEFAULT_CHUNK_SIZE = 1024 * 64;
    
    private static final class FileReadFlowable extends Flowable<ByteBuffer> {
        private final AsynchronousFileChannel fileChannel;
        private final int chunkSize;
        private final long offset;
        private final long length;

        FileReadFlowable(AsynchronousFileChannel fileChannel, int chunkSize, long offset, long length) {
            this.fileChannel = fileChannel;
            this.chunkSize = chunkSize;
            this.offset = offset;
            this.length = length;
        }

        @Override
        protected void subscribeActual(Subscriber<? super ByteBuffer> s) {
            FileReadSubscription subscription = new FileReadSubscription(s);
            s.onSubscribe(subscription);
        }
        
        private final class FileReadSubscription  extends AtomicInteger implements Subscription, CompletionHandler<Integer, ByteBuffer> {

            private static final int NOT_SET = -1;

            private static final long serialVersionUID = -6831808726875304256L;
            
            private final AtomicLong requested = new AtomicLong();

            private final Subscriber<? super ByteBuffer> subscriber;
            
            private volatile boolean done;
            
            private Throwable error;
            
            private volatile ByteBuffer next;
            
            private volatile long position;

            private volatile boolean cancelled;

            FileReadSubscription(Subscriber<? super ByteBuffer> subscriber) {
                this.subscriber = subscriber;
                this.position = NOT_SET;
            }

            @Override
            public void request(long n) {
                if (SubscriptionHelper.validate(n)) {
                    BackpressureHelper.add(requested, n);
                    drain();
                }
            }

            private void drain() {
                // the wip counter is `this` (a way of saving allocations)
                if (getAndIncrement() == 0) {
                    // on first drain (first request) we initiate the first read
                    if (position == NOT_SET) {
                        position = offset;
                        doRead();
                    }
                    int missed = 1;
                    while (true) {
                        if (cancelled) {
                            return;
                        }
                        if (requested.get() > 0) {
                            boolean emitted = false;
                            // read d before next to avoid race
                            boolean d = done;
                            ByteBuffer bb = next;
                            if (bb != null) {
                                next = null;
                                subscriber.onNext(bb);
                                emitted = true;
                            } else {
                                emitted = false;
                            }
                            if (d) {
                                if (error != null) {
                                    subscriber.onError(error);
                                    // exit without reducing wip so that further drains will be noops
                                    return;
                                } else {
                                    subscriber.onComplete();
                                    // exit without reducing wip so that further drains will be noops
                                    return;
                                }
                            } 
                            if (emitted) {
                                // do this after checking d to avoid calling read 
                                // when done
                                BackpressureHelper.produced(requested, 1);
                                doRead();
                            }
                        } 
                        missed = addAndGet(-missed);
                        if (missed == 0) {
                            return;
                        }
                    }
                }
            }
            
            private void doRead() {
                // use local variable to limit volatile reads
                long pos = position;
                ByteBuffer innerBuf = ByteBuffer.allocate(Math.min(chunkSize, maxRequired(pos)));
                fileChannel.read(innerBuf, pos, innerBuf, this);
            }

            private int maxRequired(long pos) {
                long maxRequired = offset + length - pos;
                if (maxRequired <= 0) {
                    return 0;
                } else {
                    int m = (int) (maxRequired);
                    // support really large files by checking for overflow
                    if (m < 0) {
                        return Integer.MAX_VALUE;
                    } else {
                        return m;
                    }
                }
            }
            
            @Override
            public void completed(Integer bytesRead, ByteBuffer buffer) {
                if (!cancelled) {
                    if (bytesRead == -1) {
                        done = true;
                    } else {
                        // use local variable to perform fewer volatile reads
                        long pos = position;
                        int bytesWanted = (int) Math.min(bytesRead, maxRequired(pos));
                        long position2 = pos + bytesWanted;
                        //noinspection NonAtomicOperationOnVolatileField
                        position = position2;
                        buffer.position(bytesWanted);
                        buffer.flip();
                        next = buffer;
                        if (position2 >= offset + length) {
                            done = true;
                        } 
                    }
                    drain();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                if (!cancelled) {
                    // must set error before setting done to true
                    // so that is visible in drain loop
                    error = exc;
                    done = true;
                    drain();
                }
            }

            @Override
            public void cancel() {
                cancelled = true;
            }
        }
    }
    
    /**
     * Splits a large ByteBuffer into chunks.
     *
     * @param whole the ByteBuffer to split
     * @param chunkSize the maximum size of each emitted ByteBuffer
     * @return A stream that emits chunks of the original whole ByteBuffer
     */
    public static Flowable<ByteBuffer> split(final ByteBuffer whole, final int chunkSize) {
        return Flowable.generate(whole::position, (position, emitter) -> {
            int newLimit = Math.min(whole.limit(), position + chunkSize);
            if (position >= whole.limit()) {
                emitter.onComplete();
            } else {
                ByteBuffer chunk = whole.duplicate();
                chunk.position(position).limit(newLimit);
                emitter.onNext(chunk);
            }
            return newLimit;
        });
    }

    private FlowableUtil() {
    }
}
