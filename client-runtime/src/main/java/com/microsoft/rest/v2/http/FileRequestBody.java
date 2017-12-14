/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.functions.BiConsumer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * A HTTP request body that contains a chunk of a file.
 */
public class FileRequestBody implements HttpRequestBody {
    private static final int CHUNK_SIZE = 8192;
    private final FileSegment fileSegment;

    /**
     * Create a new FileHttpRequestBody with the provided file.
     *
     * @param fileSegment the segment of file as the request body
     */
    public FileRequestBody(FileSegment fileSegment) {
        this.fileSegment = fileSegment;
    }

    @Override
    public long contentLength() {
        return fileSegment.length();
    }

    @Override
    public String contentType() {
        return "application/octet-stream";
    }

    @Override
    public Flowable<byte[]> content() {
        final long offset = fileSegment.offset();
        final long length = fileSegment.length();

        Flowable<byte[]> stream = Flowable.generate(
                new Callable<FileChannel>() {
                    @Override
                    public FileChannel call() throws Exception {
                        return fileSegment.fileChannel();
                    }
                },
                new BiConsumer<FileChannel, Emitter<byte[]>>() {
                    private final ByteBuffer innerBuf = ByteBuffer.wrap(new byte[CHUNK_SIZE]);
                    private long position = offset;

                    @Override
                    public void accept(FileChannel fileChannel, Emitter<byte[]> emitter) throws Exception {
                        try {
                            int size = (int) Math.min(offset + length - position, CHUNK_SIZE);
                            if (size <= 0) {
                                emitter.onComplete();
                            } else {
                                int bytesRead = fileChannel.read(innerBuf, position);
                                if (bytesRead == -1) {
                                    emitter.onComplete();
                                } else {
                                    position += bytesRead;
                                    emitter.onNext(Arrays.copyOf(innerBuf.array(), bytesRead));
                                }
                            }
                        } catch (IOException e) {
                            emitter.onError(e);
                        }
                    }
                });

        return stream;
    }

    /**
     * Creates a Flowable which streams file content using pooled Netty buffers.
     * Buffers emitted by this Flowable must be released to avoid memory leaks.
     * @return the Flowable
     */
    Flowable<ByteBuf> pooledContent() {
        final long offset = fileSegment.offset();
        final long length = fileSegment.length();

        Flowable<ByteBuf> stream = Flowable.generate(
                new Callable<FileChannel>() {
                    @Override
                    public FileChannel call() throws Exception {
                        return fileSegment.fileChannel();
                    }
                },
                new BiConsumer<FileChannel, Emitter<ByteBuf>>() {
                    private long position = offset;

                    @Override
                    public void accept(FileChannel fileChannel, Emitter<ByteBuf> emitter) throws Exception {
                        try {
                            int size = (int) Math.min(offset + length - position, CHUNK_SIZE);
                            if (size <= 0) {
                                emitter.onComplete();
                            } else {
                                ByteBuf nextBuf = ByteBufAllocator.DEFAULT.buffer(size);
                                int bytesRead = nextBuf.writeBytes(fileChannel, position, size);
                                if (bytesRead == -1) {
                                    emitter.onComplete();
                                } else {
                                    position += bytesRead;
                                    emitter.onNext(nextBuf);
                                }
                            }
                        } catch (IOException e) {
                            emitter.onError(e);
                        }
                    }
                });

        return stream;
    }

    @Override
    public HttpRequestBody buffer() {
        return this;
    }

    /**
     * @return the lazy loaded fileSegment of the request, in the format of a file segment.
     */
    public FileSegment fileSegment() {
        return fileSegment;
    }
}
