/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousFileChannel;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Represents an asynchronous input stream with a content length.
 */
public final class AsyncInputStream {
    private final Flowable<byte[]> content;
    private final long contentLength;

    /**
     * Creates an AsyncInputStream.
     * @param flowable The flowable which emits the stream content.
     * @param contentLength The total length of the stream content.
     */
    public AsyncInputStream(Flowable<byte[]> flowable, long contentLength) {
        this.content = flowable;
        this.contentLength = contentLength;
    }

    /**
     * @return The flowable which emits the stream content.
     */
    public Flowable<byte[]> content() {
        return content;
    }

    /**
     * @return The total length of the stream content.
     */
    public long contentLength() {
        return contentLength;
    }

    /**
     * Creates an AsyncInputStream from an AsynchronousFileChannel.
     *
     * @param fileChannel The file channel.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes of data to read from the file.
     * @return The AsyncInputStream.
     */
    public static AsyncInputStream create(AsynchronousFileChannel fileChannel, long offset, long length) {
        return new AsyncInputStream(new FlowableFileStream(fileChannel, offset, length), length);
    }

    /**
     * Creates an AsyncInputStream from an AsynchronousFileChannel which reads the entire file.
     * @param fileChannel The file channel.
     * @throws IOException if an error occurs when determining file size
     * @return The AsyncInputStream.
     */
    public static AsyncInputStream create(AsynchronousFileChannel fileChannel) throws IOException {
        long size = fileChannel.size();
        return new AsyncInputStream(new FlowableFileStream(fileChannel, 0, size), size);
    }

    /**
     * Creates an AsyncInputStream which emits the content of a given InputStream with a known length.
     * Will close the InputStream when subscription completes.
     *
     * @param inputStream The input stream.
     * @param contentLength The length of the stream content.
     * @return An AsyncInputStream which emits the content from the given InputStream.
     */
    public static AsyncInputStream create(final InputStream inputStream, long contentLength) {
        Flowable<byte[]> content = Flowable.generate(
                new Callable<InputStream>() {
                    @Override
                    public InputStream call() throws Exception {
                        return inputStream;
                    }
                },
                new BiConsumer<InputStream, Emitter<byte[]>>() {
                    private static final int CHUNK_SIZE = 8192;
                    private final byte[] innerBuf = new byte[CHUNK_SIZE];

                    @Override
                    public void accept(InputStream inputStream, Emitter<byte[]> emitter) throws Exception {
                        try {
                            int bytesRead = inputStream.read(innerBuf);
                            if (bytesRead == -1) {
                                emitter.onComplete();
                            } else {
                                byte[] nextBuf = Arrays.copyOf(innerBuf, bytesRead);
                                emitter.onNext(nextBuf);
                            }
                        } catch (IOException e) {
                            emitter.onError(e);
                        }
                    }
                },
                new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        inputStream.close();
                    }
                });

        return new AsyncInputStream(content, contentLength);
    }
}
