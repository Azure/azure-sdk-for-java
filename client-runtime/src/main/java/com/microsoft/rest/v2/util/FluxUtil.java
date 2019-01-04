/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.util;

import io.netty.buffer.Unpooled;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Contains helper methods, types for dealing with Flux.
 */
public final class FluxUtil {
    /**
     * Checks if a type is Flux&lt;ByteBuffer&gt;.
     *
     * @param entityType the type to check
     * @return whether the type represents a Flux that emits byte arrays
     */
    public static boolean isFluxByteBuffer(Type entityType) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Flux.class)) {
            final Type innerType = TypeUtil.getTypeArguments(entityType)[0];
            if (TypeUtil.isTypeOrSubTypeOf(innerType, ByteBuffer.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collects byte buffers emitted by a Flux into a byte array.
     * @param byteBufferFlux A stream which emits byte buffers.
     * @return A Mono which emits the concatenation of all the byte buffers given by the source Flux.
     */
    public static Mono<byte[]> collectBytesInArray(Flux<ByteBuffer> byteBufferFlux) {
        return byteBufferFlux.collect(() -> Unpooled.buffer(), (nettyByteBuf, javaNioByteBuffer) -> nettyByteBuf.writeBytes(javaNioByteBuffer.slice()))
                .map(nettyByteBuf -> {
                    try {
                        if (nettyByteBuf.array().length == nettyByteBuf.readableBytes()) {
                            return nettyByteBuf.array();
                        } else {
                            byte[] arr = new byte[nettyByteBuf.readableBytes()];
                            nettyByteBuf.readBytes(arr);
                            return arr;
                        }
                    } finally {
                        nettyByteBuf.release();
                    }
                });
    }

    /**
     * Splits a large ByteBuffer into chunks.
     *
     * @param whole the ByteBuffer to split
     * @param chunkSize the maximum size of each emitted ByteBuffer
     * @return A stream that emits chunks of the original whole ByteBuffer
     */
    public static Flux<ByteBuffer> split(final ByteBuffer whole, final int chunkSize) {
        return Flux.generate(whole::position, (position, synchronousSync) -> {
            int newLimit = Math.min(whole.limit(), position + chunkSize);
            if (position >= whole.limit()) {
                synchronousSync.complete();
            } else {
                ByteBuffer chunk = whole.duplicate();
                chunk.position(position).limit(newLimit);
                synchronousSync.next(chunk);
            }
            return newLimit;
        });
    }

    /**
     * Ensures the given Flowable emits the expected number of bytes.
     *
     * @param bytesExpected the number of bytes expected to be emitted
     * @return a Function which can be applied using {@link Flowable#compose}
     */
    public static Function<ByteBuffer, ByteBuffer> ensureLength(long bytesExpected) {
        throw new RuntimeException("NotImplemented::not required once moved to reactor-netty");
    }

    /**
     * Collects byte buffers emitted by a Flux into a ByteBuffer.
     * @param content A stream which emits byte arrays.
     * @return A Mono which emits the concatenation of all the byte buffers given by the source Flowable.
     */
    public static Mono<ByteBuffer> collectBytesInBuffer(Flux<ByteBuffer> content) {
        return collectBytesInArray(content).map(ByteBuffer::wrap);
    }

    //region Utility methods to write Flux<ByteBuffer> to AsynchronousFileChannel.

    /**
     * Writes the bytes emitted by a Flux to an AsynchronousFileChannel.
     *
     * @param content the Flux content
     * @param outFile the file channel
     * @return a Void Mono which performs the write operation when subscribed
     */
    public static Mono<Void> writeFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile) {
        return writeFile(content, outFile, 0);
    }

    /**
     * Writes the bytes emitted by a Flux to an AsynchronousFileChannel
     * starting at the given position in the file.
     *
     * @param content the Flux content
     * @param outFile the file channel
     * @param position the position in the file to begin writing
     * @return a Void Mono which performs the write operation when subscribed
     */
    public static Mono<Void> writeFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile, long position) {
        Mono<Void> voidMono = Mono.create(monoSink -> content.subscribe(new CoreSubscriber<ByteBuffer>() {
            // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
            // An I/O pool thread will write to isWriting and read isCompleted,
            // while another thread may read isWriting and write to isCompleted.
            volatile boolean isWriting = false;
            volatile boolean isCompleted = false;
            volatile Subscription subscription;
            volatile long pos = position;
            AtomicInteger reqCount = new AtomicInteger(0);

            private Consumer<Void> requestDelegate = new Consumer<Void>() {
                @Override
                public void accept(Void aVoid) {
                    reqCount.incrementAndGet();
                    subscription.request(1);
                }
            };

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                requestDelegate.accept(null);
            }

            @Override
            public void onNext(ByteBuffer bytes) {
                isWriting = true;
                outFile.write(bytes, pos, null, onWriteCompleted);
            }

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
                monoSink.error(throwable);
            }

            @Override
            public void onComplete() {
                isCompleted = true;
                if (!isWriting) {
                    // Passing null to MonoSink<T>::success is accepted by standard implementations.
                    monoSink.success(null);
                }
            }

            CompletionHandler<Integer, Object> onWriteCompleted = new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer bytesWritten, Object attachment) {
                    isWriting = false;
                    if (isCompleted) {
                        // Passing null to MonoSink<T>::success is accepted by standard implementations.
                        monoSink.success(null);
                    } else {
                        pos += bytesWritten;
                        requestDelegate.accept(null);
                    }
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    subscription.cancel();
                    monoSink.error(exc);
                }
            };
        }));
        return voidMono;
    }
    //endregion

    //region Utility methods to create Flux<ByteBuffer> that read and emits chunks from AsynchronousFileChannel.


    //region Utility methods to write Flowable<ByteBuffer> to AsynchronousFileChannel. [This will be removed]
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
    //endregion


    private static final int DEFAULT_CHUNK_SIZE = 1024 * 64;

    /**
     * Creates a {@link Flux} from an {@link AsynchronousFileChannel}
     * which reads part of a file into chunks of the given size.
     *
     * @param fileChannel The file channel.
     * @param chunkSize the size of file chunks to read.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes to read from the file.
     * @return the Flowable.
     */
    public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel, int chunkSize, long offset, long length) {
        return new FileReadFlux(fileChannel, chunkSize, offset, length);
    }

    /**
     * Creates a {@link Flux} from an {@link AsynchronousFileChannel}
     * which reads part of a file.
     *
     * @param fileChannel The file channel.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes to read from the file.
     * @return the Flowable.
     */
    public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel, long offset, long length) {
        return readFile(fileChannel, DEFAULT_CHUNK_SIZE, offset, length);
    }

    /**
     * Creates a {@link Flux} from an {@link AsynchronousFileChannel}
     * which reads the entire file.
     *
     * @param fileChannel The file channel.
     * @return The AsyncInputStream.
     */
    public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel) {
        try {
            long size = fileChannel.size();
            return readFile(fileChannel, DEFAULT_CHUNK_SIZE, 0, size);
        } catch (IOException e) {
            return Flux.error(e);
        }
    }
    //endregion

    //region FileReadFlux implementation
    private static final class FileReadFlux extends Flux<ByteBuffer> {
        private final AsynchronousFileChannel fileChannel;
        private final int chunkSize;
        private final long offset;
        private final long length;

        FileReadFlux(AsynchronousFileChannel fileChannel, int chunkSize, long offset, long length) {
            this.fileChannel = fileChannel;
            this.chunkSize = chunkSize;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void subscribe(CoreSubscriber<? super ByteBuffer> actual) {
            FileReadSubscription subscription = new FileReadSubscription(actual, fileChannel, chunkSize, offset, length);
            actual.onSubscribe(subscription);
        }

        static final class FileReadSubscription implements Subscription, CompletionHandler<Integer, ByteBuffer> {
            private static final int NOT_SET = -1;
            private static final long serialVersionUID = -6831808726875304256L;
            //
            private final Subscriber<? super ByteBuffer> subscriber;
            private volatile long position;
            //
            private final AsynchronousFileChannel fileChannel;
            private final int chunkSize;
            private final long offset;
            private final long length;
            //
            private volatile boolean done;
            private Throwable error;
            private volatile ByteBuffer next;
            private volatile boolean cancelled;
            //
            volatile int wip;
            @SuppressWarnings("rawtypes")
            static final AtomicIntegerFieldUpdater<FileReadSubscription> WIP = AtomicIntegerFieldUpdater.newUpdater(FileReadSubscription.class, "wip");
            volatile long requested;
            @SuppressWarnings("rawtypes")
            static final AtomicLongFieldUpdater<FileReadSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(FileReadSubscription.class, "requested");
            //

            FileReadSubscription(Subscriber<? super ByteBuffer> subscriber, AsynchronousFileChannel fileChannel, int chunkSize, long offset, long length) {
                this.subscriber = subscriber;
                //
                this.fileChannel = fileChannel;
                this.chunkSize = chunkSize;
                this.offset = offset;
                this.length = length;
                //
                this.position = NOT_SET;
            }

            //region Subscription implementation

            @Override
            public void request(long n) {
                if (Operators.validate(n)) {
                    Operators.addCap(REQUESTED, this, n);
                    drain();
                }
            }

            @Override
            public void cancel() {
                this.cancelled = true;
            }

            //endregion

            //region CompletionHandler implementation

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

            //endregion

            private void drain() {
                if (WIP.getAndIncrement(this) != 0) {
                    return;
                }
                // on first drain (first request) we initiate the first read
                if (position == NOT_SET) {
                    position = offset;
                    doRead();
                }
                int missed = 1;
                for (;;) {
                    if (cancelled) {
                        return;
                    }
                    if (REQUESTED.get(this) > 0) {
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
                                // exit without reducing wip so that further drains will be NOOP
                                return;
                            } else {
                                subscriber.onComplete();
                                // exit without reducing wip so that further drains will be NOOP
                                return;
                            }
                        }
                        if (emitted) {
                            // do this after checking d to avoid calling read
                            // when done
                            Operators.produced(REQUESTED, this, 1);
                            //
                            doRead();
                        }
                    }
                    missed = WIP.addAndGet(this, -missed);
                    if (missed == 0) {
                        return;
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
        }
    }

    //endregion

    private FluxUtil() {
    }
}
