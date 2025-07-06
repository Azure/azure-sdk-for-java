// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.netty.channel.Channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of {@link InputStream} that reads contents from a Netty {@link Channel}.
 */
public final class Netty4ChannelInputStream extends InputStream {
    private final Channel channel;
    private final boolean isHttp2;
    private final Runnable onClose;

    // Indicator for the Channel being fully read.
    // This will become true before 'streamDone' becomes true, but both may become true in the same operation.
    // Once this is true, the Channel will never be read again.
    private boolean channelDone = false;

    // Indicator for the stream being fully read.
    // This will become true after 'channelDone' becomes true, but both may become true in the same operation.
    // Once this is true, the stream will never return data again.
    private boolean streamDone = false;

    // Queue of byte[]s that maintains the last available contents from the Channel / eager content.
    // A queue is needed as each Channel.read() may result in many channelRead calls.
    private final Queue<byte[]> additionalBuffers;

    private byte[] currentBuffer;

    // Read index of the 'currentBuffer' that is actively being read.
    // Once 'readIndex' == 'currentBuffer.length' 'additionalBuffers' will be polled for the next 'currentBuffer'.
    // If 'additionalBuffers' doesn't contain anymore byte[]s 'readMore' will be called to attempt reading more data
    // from the Channel. From there either more data will be read or 'streamDone = true' will be reached.
    private int readIndex;

    /**
     * Creates an instance of {@link Netty4ChannelInputStream} that reads from the given {@link Channel}.
     *
     * @param eagerContent Any response body content eagerly read from the {@link Channel} when processing the initial
     * status line and response headers.
     * @param channel The {@link Channel} to read from.
     * @param isHttp2 Flag indicating whether the Channel is used for HTTP/2 or not.
     * @param onClose A runnable to execute when the stream is closed.
     */
    Netty4ChannelInputStream(ByteArrayOutputStream eagerContent, Channel channel, boolean isHttp2, Runnable onClose) {
        if (eagerContent != null && eagerContent.size() > 0) {
            this.currentBuffer = eagerContent.toByteArray();
            eagerContent.reset();
        } else {
            this.currentBuffer = new byte[0];
        }
        this.readIndex = 0;
        this.additionalBuffers = new ConcurrentLinkedQueue<>();
        this.channel = channel;
        if (channel.pipeline().get(Netty4InitiateOneReadHandler.class) != null) {
            channel.pipeline().remove(Netty4InitiateOneReadHandler.class);
        }
        this.isHttp2 = isHttp2;
        this.onClose = onClose;
    }

    byte[] getCurrentBuffer() {
        return currentBuffer;
    }

    @Override
    public int read() throws IOException {
        if (streamDone) {
            return -1;
        }

        // currentBuffer has more data.
        if (readIndex < currentBuffer.length) {
            return currentBuffer[readIndex++] & 0xFF;
        }

        // currentBuffer has no more data, set up the next buffer.
        // If the next buffer can't be set up the stream is done.
        if (!setupNextBuffer()) {
            return -1;
        }

        // Next buffer will never be empty if this point is reached.
        return currentBuffer[readIndex++] & 0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (streamDone) {
            return -1;
        }

        int remainingInCurrentBuffer = currentBuffer.length - readIndex;

        // currentBuffer has enough data remaining to satisfy the read.
        if (remainingInCurrentBuffer >= len) {
            System.arraycopy(currentBuffer, readIndex, b, off, len);
            readIndex += len;
            return len;
        }

        // Read whatever is remaining in the current buffer.
        System.arraycopy(currentBuffer, readIndex, b, off, remainingInCurrentBuffer);

        // Set up a marker to determine how much more to read.
        int toRead = len - remainingInCurrentBuffer;
        off += remainingInCurrentBuffer;

        while (setupNextBuffer()) {
            if (currentBuffer.length >= toRead) {
                // The next buffer is able to satisfy the remaining read.
                System.arraycopy(currentBuffer, 0, b, off, toRead);
                readIndex = toRead;
                return len;
            } else {
                // The entire next buffer is able to fit in the read buffer.
                System.arraycopy(currentBuffer, 0, b, off, currentBuffer.length);
                toRead -= currentBuffer.length;
                off += currentBuffer.length;
            }
        }

        // Reached the end of the Channel contents before the requested read length could be completed.
        // Return the amount of content read, which would be the requested length minus what couldn't be read.
        return len - toRead;
    }

    @Override
    public long skip(long n) throws IOException {
        if (streamDone) {
            return 0;
        }

        int remainingInCurrentBuffer = currentBuffer.length - readIndex;

        // currentBuffer has enough data remaining to satisfy the read.
        if (remainingInCurrentBuffer >= n) {
            readIndex += (int) n;
            return n;
        }

        // Set up a marker to determine how much more to skip.
        long toSkip = n - remainingInCurrentBuffer;

        while (setupNextBuffer()) {
            if (currentBuffer.length >= toSkip) {
                // The next buffer is able to satisfy the remaining skip.
                readIndex = (int) toSkip;
                return n;
            } else {
                // The entire next buffer is able to fit in the remaining skip amount.
                toSkip -= currentBuffer.length;
            }
        }

        // Reached the end of the Channel contents before the skip amount could be satisfied.
        // Return the amount of content skipped, which would be the skip amount requested minus what couldn't be
        // skipped.
        return n - toSkip;
    }

    /**
     * Closes this input stream and ensures the underlying connection can be returned to the pool.
     * This method does not close the underlying channel. Instead, it triggers the onClose
     * callback which is responsible for draining the rest of the stream content.
     */
    @Override
    public void close() throws IOException {
        try {
            if (onClose != null) {
                onClose.run();
            }
        } finally {
            super.close();
            currentBuffer = null;
            additionalBuffers.clear();
            streamDone = true;
        }
    }

    private boolean setupNextBuffer() throws IOException {
        if (!additionalBuffers.isEmpty()) {
            currentBuffer = additionalBuffers.poll();
            readIndex = 0;
            return true;
        } else if (readMore()) {
            return true;
        } else {
            streamDone = true;
            return false;
        }
    }

    // Attempts to read more data from the Channel.
    // If more data was read true will be returned, otherwise false will be returned indicating the Channel has no more
    // data is fully read.
    // This method should only be called when 'additionalBuffers' is empty and 'currentBuffer' has been fully consumed.
    private boolean readMore() throws IOException {
        // Channel has been fully read, cannot retrieve anymore data from it.
        if (channelDone) {
            return false;
        }

        // Check if the Channel is still active before attempting to retrieve more data.
        if (!channel.isActive()) {
            channelDone = true;
            return false;
        }

        Netty4InitiateOneReadHandler handler = channel.pipeline().get(Netty4InitiateOneReadHandler.class);
        if (handler == null) {
            handler = new Netty4InitiateOneReadHandler(null, byteBuf -> {
                // No need to check if the ByteBuf is readable as that is handled by Netty4InitiateOneReadHandler's
                // channelRead method.
                byte[] buffer = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(buffer);

                additionalBuffers.offer(buffer);
            }, isHttp2);
            channel.pipeline().addLast(Netty4HandlerNames.READ_ONE, handler);
        }

        // Run reading the Channel in a loop, just in case all reads return empty data but the Channel doesn't complete.
        while (additionalBuffers.isEmpty() && !channelDone) {
            CountDownLatch latch = new CountDownLatch(1);
            handler.setLatch(latch);
            channel.read();

            Netty4Utility.awaitLatch(latch);

            // Check to see if we've reach the end of the Channel.
            channelDone = handler.isChannelConsumed();
            Throwable exception = handler.channelException();
            if (exception != null) {
                if (exception instanceof Error) {
                    throw (Error) exception;
                } else if (exception instanceof IOException) {
                    throw (IOException) exception;
                } else {
                    throw new IOException(exception);
                }
            }
        }

        if (!additionalBuffers.isEmpty()) {
            currentBuffer = additionalBuffers.poll();
            readIndex = 0;
        } else if (channelDone) { // Don't listen to IntelliJ here, channelDone may be false.
            // This read contained no data and the channel completed, therefore the stream is also completed.
            streamDone = true;
            return false;
        }

        return true;
    }
}
