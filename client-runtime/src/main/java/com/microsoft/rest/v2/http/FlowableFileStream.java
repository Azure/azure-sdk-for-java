/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
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
    private final long offset;
    private final long length;

    /**
     * Creates a FlowableFileStream which reads a file starting from an offset and length.
     * @param fileChannel The AsynchronousFileChannel to read file content from.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes of data to read from the file.
     */
    public FlowableFileStream(AsynchronousFileChannel fileChannel, long offset, long length) {
        this.fileChannel = fileChannel;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Creates a FlowableFileStream which reads an entire file.
     * @param fileChannel The AsynchronousFileChannel to read content from
     * @throws IOException if an exception occurs when determining file size
     */
    public FlowableFileStream(AsynchronousFileChannel fileChannel) throws IOException {
        this(fileChannel, 0, fileChannel.size());
    }

    @Override
    protected void subscribeActual(final Subscriber<? super byte[]> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            final ByteBuffer innerBuf = ByteBuffer.wrap(new byte[CHUNK_SIZE]);
            boolean canceled = false;
            long chunksRequested = 0;
            long position = offset;

            @Override
            public void request(long n) {
                chunksRequested += n;
                readSegmentsAsync();
            }

            void readSegmentsAsync() {
                fileChannel.read(innerBuf, position, null, new CompletionHandler<Integer, Object>() {
                    @Override
                    public void completed(Integer bytesRead, Object attachment) {
                        if (!canceled) {
                            if (bytesRead == -1) {
                                subscriber.onComplete();
                            } else {
                                subscriber.onNext(Arrays.copyOf(innerBuf.array(), bytesRead));

                                position += bytesRead;
                                chunksRequested--;
                                if (chunksRequested > 0 && position < offset + length) {
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
