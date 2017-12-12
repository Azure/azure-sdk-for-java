package com.microsoft.rest.v2.http;

import io.reactivex.Flowable;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;

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
     * @return The AsyncInputStream.
     */
    public static AsyncInputStream create(AsynchronousFileChannel fileChannel) throws IOException {
        return new AsyncInputStream(new FlowableFileStream(fileChannel), fileChannel.size());
    }
}
