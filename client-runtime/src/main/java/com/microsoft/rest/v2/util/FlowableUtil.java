/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.internal.util.BackpressureHelper;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Callable;
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
        return content.collectInto(ByteStreams.newDataOutput(), new BiConsumer<ByteArrayDataOutput, ByteBuffer>() {
            @Override
            public void accept(ByteArrayDataOutput out, ByteBuffer chunk) throws Exception {
                // TODO: Would be nice to reduce copying here
                byte[] arrayChunk = new byte[chunk.remaining()];
                chunk.get(arrayChunk);
                out.write(arrayChunk);
            }
        }).map(new Function<ByteArrayDataOutput, byte[]>() {
            @Override
            public byte[] apply(ByteArrayDataOutput out) throws Exception {
                return out.toByteArray();
            }
        });
    }

    /**
     * Collects byte buffers emitted by a Flowable into a ByteBuffer.
     * @param content A stream which emits byte arrays.
     * @return A Single which emits the concatenation of all the byte buffers given by the source Flowable.
     */
    public static Single<ByteBuffer> collectBytesInBuffer(Flowable<ByteBuffer> content) {
        return collectBytesInArray(content)
            .map(new Function<byte[], ByteBuffer>() {
                @Override
                public ByteBuffer apply(byte[] bytes) throws Exception {
                    return ByteBuffer.wrap(bytes);
                }
            });
    }

    /**
     * Writes the bytes emitted by a Flowable to an AsynchronousFileChannel.
     * @param content the Flowable content
     * @param fileChannel the file channel
     * @return a Completable which performs the write operation when subscribed
     */
    public static Completable writeFile(final Flowable<ByteBuffer> content, final AsynchronousFileChannel fileChannel) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter emitter) throws Exception {
                content.subscribe(new FlowableSubscriber<ByteBuffer>() {
                    // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
                    // An I/O pool thread will write to isWriting and read isCompleted,
                    // while another thread may read isWriting and write to isCompleted.
                    volatile boolean isWriting = false;
                    volatile boolean isCompleted = false;
                    volatile Subscription subscription;
                    volatile long position = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(ByteBuffer bytes) {
                        isWriting = true;
                        fileChannel.write(bytes, position, null, onWriteCompleted);
                    }


                    CompletionHandler<Integer, Object> onWriteCompleted = new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer bytesWritten, Object attachment) {
                            isWriting = false;
                            if (isCompleted) {
                                emitter.onComplete();
                            }
                            //noinspection NonAtomicOperationOnVolatileField
                            position += bytesWritten;
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
                });
            }
        });
    }

    /**
     * Creates a {@link Flowable} from an {@link AsynchronousFileChannel} which reads part of a file.
     *
     * @param fileChannel The file channel.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes to read from the file.
     * @return the Flowable.
     */
    public static Flowable<ByteBuffer> readFile(final AsynchronousFileChannel fileChannel, final long offset, final long length) {
        Flowable<ByteBuffer> fileStream = new FileReadFlowable(fileChannel, offset, length);
        return fileStream;
    }

    /**
     * Creates a {@link Flowable} from an {@link AsynchronousFileChannel} which reads the entire file.
     * @param fileChannel The file channel.
     * @throws IOException if an error occurs when determining file size
     * @return The AsyncInputStream.
     */
    public static Flowable<ByteBuffer> readFile(AsynchronousFileChannel fileChannel) throws IOException {
        long size = fileChannel.size();
        return readFile(fileChannel, 0, size);
    }

    private static final int CHUNK_SIZE = 8192;
    private static class FileReadFlowable extends Flowable<ByteBuffer> {
        private final AsynchronousFileChannel fileChannel;
        private final long offset;
        private final long length;

        FileReadFlowable(AsynchronousFileChannel fileChannel, long offset, long length) {
            this.fileChannel = fileChannel;
            this.offset = offset;
            this.length = length;
        }

        @Override
        protected void subscribeActual(Subscriber<? super ByteBuffer> s) {
            s.onSubscribe(new FileReadSubscription(s));
        }

        private class FileReadSubscription implements Subscription {
            final Subscriber<? super ByteBuffer> subscriber;
            final AtomicLong requested = new AtomicLong();
            volatile boolean cancelled = false;

            // I/O callbacks are serialized, but not guaranteed to happen on the same thread, which makes volatile necessary.
            volatile long position = offset;

            FileReadSubscription(Subscriber<? super ByteBuffer> subscriber) {
                this.subscriber = subscriber;
            }

            @Override
            public void request(long n) {
                if (BackpressureHelper.add(requested, n) == 0L) {
                    doRead();
                }
            }

            void doRead() {
                ByteBuffer innerBuf = ByteBuffer.allocate(Math.min(CHUNK_SIZE, (int) (offset + length - position)));
                fileChannel.read(innerBuf, position, innerBuf, onReadComplete);
            }

            private final CompletionHandler<Integer, ByteBuffer> onReadComplete = new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer bytesRead, ByteBuffer buffer) {
                    if (!cancelled) {
                        if (bytesRead == -1) {
                            subscriber.onComplete();
                        } else {
                            int bytesWanted = (int) Math.min(bytesRead, offset + length - position);
                            //noinspection NonAtomicOperationOnVolatileField
                            position += bytesWanted;
                            buffer.flip();
                            subscriber.onNext(buffer);
                            if (position >= offset + length) {
                                subscriber.onComplete();
                            } else if (requested.decrementAndGet() > 0) {
                                doRead();
                            }
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    if (!cancelled) {
                        subscriber.onError(exc);
                    }
                }
            };

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
        return Flowable.generate(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 0;
            }
        }, new BiFunction<Integer, Emitter<ByteBuffer>, Integer>() {
            @Override
            public Integer apply(Integer position, Emitter<ByteBuffer> emitter) throws Exception {
                int newLimit = Math.min(whole.limit(), position + chunkSize);
                if (position >= whole.limit()) {
                    emitter.onComplete();
                } else {
                    ByteBuffer chunk = whole.duplicate();
                    chunk.position(position).limit(newLimit);
                    emitter.onNext(chunk);
                }
                return newLimit;
            }
        });
    }

    private FlowableUtil() {
    }
}
