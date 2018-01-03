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
import org.reactivestreams.Subscription;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;

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
    public static Completable writeContentToFile(final Flowable<byte[]> content, final AsynchronousFileChannel fileChannel) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter emitter) throws Exception {
                content.subscribe(new FlowableSubscriber<byte[]>() {

                    // volatile ensures that writes to these flags will be immediately visible to other threads.
                    // An I/O pool thread will write to isWriting and read isCompleted,
                    // while a Netty pool thread will read isWriting and write to isCompleted.
                    volatile boolean isWriting = false;
                    volatile boolean isCompleted = false;

                    Subscription subscription;
                    long position = 0;

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
}
