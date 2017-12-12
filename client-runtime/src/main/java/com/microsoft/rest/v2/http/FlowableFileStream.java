/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;

/**
 * Exposes an AsynchronousFileChannel as a Flowable.
 *
 * Does not close the AsynchronousFileChannel after completion.
 */
public class FlowableFileStream extends Flowable<byte[]> {
    private static final int CHUNK_SIZE = 8192;
    private final AsynchronousFileChannel fileChannel;

    /**
     * Creates a FlowableFileStream.
     * @param fileChannel The AsynchronousFileChannel to read file content from.
     */
    public FlowableFileStream(AsynchronousFileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super byte[]> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            boolean canceled = false;
            long chunksRequested = 0;
            long position = 0;

            @Override
            public void request(long n) {
                chunksRequested += n;
                readSegmentsAsync();
            }

            void readSegmentsAsync() {
                final ByteBuffer dst = ByteBuffer.wrap(new byte[CHUNK_SIZE]);
                fileChannel.read(dst, position, null, new CompletionHandler<Integer, Object>() {
                    @Override
                    public void completed(Integer bytesRead, Object attachment) {
                        if (!canceled) {
                            if (bytesRead == -1) {
                                subscriber.onComplete();
                            } else {
                                if (bytesRead == CHUNK_SIZE) {
                                    subscriber.onNext(dst.array());
                                } else {
                                    subscriber.onNext(Arrays.copyOf(dst.array(), bytesRead));
                                }

                                position += bytesRead;
                                chunksRequested--;
                                if (chunksRequested > 0) {
                                    readSegmentsAsync();
                                }
                            }
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        subscriber.onError(exc);
                    }
                });
            }

            @Override
            public void cancel() {
                canceled = true;
            }
        });
    }
}
