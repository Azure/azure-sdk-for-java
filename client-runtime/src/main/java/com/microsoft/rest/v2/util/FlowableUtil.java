/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.util.BackpressureHelper;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Contains helper methods for dealing with Flowables.
 */
public class FlowableUtil {
    /**
     * Collects byte arrays emitted by a Flowable into a Single.
     * @param content A stream which emits byte arrays.
     * @return A Single which emits the concatenation of all the byte arrays given by the source Flowable.
     */
    public static Single<byte[]> collectBytes(Flowable<byte[]> content) {
        return content.collectInto(ByteStreams.newDataOutput(), new BiConsumer<ByteArrayDataOutput, byte[]>() {
            @Override
            public void accept(ByteArrayDataOutput out, byte[] chunk) throws Exception {
                out.write(chunk);
            }
        }).map(new Function<ByteArrayDataOutput, byte[]>() {
            @Override
            public byte[] apply(ByteArrayDataOutput out) throws Exception {
                return out.toByteArray();
            }
        });
    }

    /**
     * Writes the bytes emitted by a Flowable to an AsynchronousFileChannel.
     * @param content the Flowable content
     * @param fileChannel the file channel
     * @return a Completable which performs the write operation when subscribed
     */
    public static Completable writeFile(final Flowable<byte[]> content, final AsynchronousFileChannel fileChannel) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter emitter) throws Exception {
                content.subscribe(new FlowableSubscriber<byte[]>() {
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
                    public void onNext(byte[] bytes) {
                        isWriting = true;
                        fileChannel.write(ByteBuffer.wrap(bytes), position, null, onWriteCompleted);
                    }


                    CompletionHandler<Integer, Object> onWriteCompleted = new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer bytesRead, Object attachment) {
                            isWriting = false;
                            if (isCompleted) {
                                emitter.onComplete();
                            }
                            position += bytesRead;
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
     * Creates an AsyncInputStream from an AsynchronousFileChannel.
     *
     * @param fileChannel The file channel.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes of data to read from the file.
     * @return The AsyncInputStream.
     */
    public static Flowable<byte[]> readFile(final AsynchronousFileChannel fileChannel, final long offset, final long length) {
        Flowable<byte[]> fileStream = new FileReadFlowable(fileChannel, offset, length);
        return fileStream;
    }

    /**
     * Creates a {@link Flowable} from an {@link AsynchronousFileChannel} which reads the entire file.
     * @param fileChannel The file channel.
     * @throws IOException if an error occurs when determining file size
     * @return The AsyncInputStream.
     */
    public static Flowable<byte[]> readFile(AsynchronousFileChannel fileChannel) throws IOException {
        long size = fileChannel.size();
        return readFile(fileChannel, 0, size);
    }

    private static final int CHUNK_SIZE = 8192;
    private static class FileReadFlowable extends Flowable<byte[]> {
        private final AsynchronousFileChannel fileChannel;
        private final long offset;
        private final long length;

        FileReadFlowable(AsynchronousFileChannel fileChannel, long offset, long length) {
            this.fileChannel = fileChannel;
            this.offset = offset;
            this.length = length;
        }

        @Override
        protected void subscribeActual(Subscriber<? super byte[]> s) {
            s.onSubscribe(new FileReadSubscription(s));
        }

        private class FileReadSubscription implements Subscription {
            final Subscriber<? super byte[]> subscriber;
            final ByteBuffer innerBuf = ByteBuffer.wrap(new byte[CHUNK_SIZE]);
            final AtomicLong requested = new AtomicLong();
            volatile boolean cancelled = false;

            // I/O callbacks are serialized, but not guaranteed to happen on the same thread, which makes volatile necessary.
            volatile long position = offset;

            FileReadSubscription(Subscriber<? super byte[]> subscriber) {
                this.subscriber = subscriber;
            }

            @Override
            public void request(long n) {
                if (BackpressureHelper.add(requested, n) == 0L) {
                    doRead();
                }
            }

            void doRead() {
                innerBuf.clear();
                fileChannel.read(innerBuf, position, null, onReadComplete);
            }

            private final CompletionHandler<Integer, Object> onReadComplete = new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer bytesRead, Object attachment) {
                    if (!cancelled) {
                        if (bytesRead == -1) {
                            subscriber.onComplete();
                        } else {
                            int bytesWanted = (int) Math.min(bytesRead, offset + length - position);
                            //noinspection NonAtomicOperationOnVolatileField
                            position += bytesWanted;
                            subscriber.onNext(Arrays.copyOf(innerBuf.array(), bytesWanted));
                            if (position >= offset + length) {
                                subscriber.onComplete();
                            } else if (requested.decrementAndGet() > 0) {
                                doRead();
                            }
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
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
}
