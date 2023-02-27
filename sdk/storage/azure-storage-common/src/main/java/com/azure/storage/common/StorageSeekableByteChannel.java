// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * StorageSeekableByteChannel allows for uploading and downloading data to and from an Azure Storage service using the
 * {@link SeekableByteChannel} interface.
 * <p>
 * Storage channels are opened for read OR write access, not both. Not all APIs are supported depending on the channel
 * mode and storage resource type (e.g. blob).
 */
public final class StorageSeekableByteChannel implements SeekableByteChannel {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSeekableByteChannel.class);

    /**
     * Interface for injectable behavior to read from a backing Storage resource.
     */
    public interface ReadBehavior {
        /**
         * Reads n bytes from the backing resource, where {@code 0 <= n <= dst.remaining()}.
         * Emulates behavior of {@link java.nio.channels.ReadableByteChannel#read(ByteBuffer)}.
         *
         * @param dst Destination to read the resource into.
         * @param sourceOffset Offset to read from the resource.
         * @return Number of bytes read from the resource, possibly zero, or -1 end of resource.
         * @see java.nio.channels.ReadableByteChannel#read(ByteBuffer)
         */
        int read(ByteBuffer dst, long sourceOffset);

        /**
         * Gets the length of the resource. The returned value may have been cached from previous operations on this
         * instance.
         * @return The length in bytes.
         */
        long getResourceLength();
    }

    /**
     * Interface for injectable behavior to write to a backing Storage resource.
     */
    public interface WriteBehavior {
        /**
         * Writes to the backing resource.
         * @param src Bytes to write.
         * @param destOffset Offset of backing resource to write the bytes at.
         */
        void write(ByteBuffer src, long destOffset);

        /**
         * Calls any necessary commit/flush calls on the backing resource.
         * @param totalLength Total length of the bytes being committed (necessary for some resource types).
         */
        void commit(long totalLength);

        /**
         * Determines whether the write behavior can support a random seek to this position. May fetch information
         * from the service to determine if possible.
         * @param position Desired seek position.
         * @return Whether the resource supports this.
         */
        boolean canSeek(long position);

        /**
         * Changes the size of the backing resource, if supported.
         * @param newSize New size of backing resource.
         * @throws UnsupportedOperationException If operation is not supported by the backing resource.
         */
        void resize(long newSize);
    }

    private final ReadBehavior readBehavior;
    private final WriteBehavior writeBehavior;

    private boolean isClosed;

    private ByteBuffer buffer;
    private long bufferAbsolutePosition;

    private long absolutePosition;

    /**
     * Constructs an instance of this class.
     * @param chunkSize Size of the internal channel buffer to use for data transfer, and for individual REST transfers.
     * @param readBehavior Behavior for reading from the backing Storage resource.
     * @param writeBehavior Behavior for writing to the backing Storage resource.
     * @throws IllegalArgumentException If both read and write behavior are given.
     */
    public StorageSeekableByteChannel(int chunkSize, ReadBehavior readBehavior, WriteBehavior writeBehavior) {
        if (readBehavior != null && writeBehavior != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "StorageSeekableByteChannel can have only one of readBehavior or writeBehavior."));
        }

        buffer = ByteBuffer.allocate(chunkSize);
        this.readBehavior = readBehavior;
        this.writeBehavior = writeBehavior;

        bufferAbsolutePosition = 0;
        if (readBehavior != null) {
            buffer.limit(0);
        }
    }

    /**
     * Gets the read-behavior used by this channel.
     * @return {@link ReadBehavior} of this channel.
     */
    public ReadBehavior getReadBehavior() {
        return readBehavior;
    }

    /**
     * Gets the write-behavior used by this channel.
     * @return {@link WriteBehavior} of this channel.
     */
    public WriteBehavior getWriteBehavior() {
        return writeBehavior;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        assertOpen();
        assertCanRead();
        if (dst.isReadOnly()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("ByteBuffer dst must support writes."));
        }

        if (buffer.remaining() == 0) {
            if (refillReadBuffer(absolutePosition) == -1) {
                // cap any position overshooting if channel is at end
                absolutePosition = readBehavior.getResourceLength();
                return -1;
            }
        }
        // buffer still empty, no EOF signal, no exception: just return zero
        if (buffer.remaining() == 0) {
            return 0;
        }

        int read = Math.min(buffer.remaining(), dst.remaining());
        ByteBuffer temp = buffer.duplicate();
        temp.limit(temp.position() + read);
        dst.put(temp);
        buffer.position(buffer.position() + read);
        absolutePosition += read;
        return read;
    }

    private int refillReadBuffer(long newBufferAbsolutePosition) {
        buffer.clear();
        int read = readBehavior.read(buffer, newBufferAbsolutePosition);
        buffer.rewind();
        buffer.limit(Math.max(read, 0));
        bufferAbsolutePosition = Math.min(newBufferAbsolutePosition, readBehavior.getResourceLength());
        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        assertOpen();
        assertCanWrite();

        int write = Math.min(src.remaining(), buffer.remaining());
        ByteBuffer temp = src.duplicate();
        temp.limit(temp.position() + write);
        buffer.put(temp);
        src.position(src.position() + write);
        absolutePosition += write;

        if (buffer.remaining() == 0) {
            flushWriteBuffer();
        }

        return write;
    }

    private void flushWriteBuffer() {
        if (buffer.position() == 0) {
            return;
        }
        buffer.limit(buffer.position());
        buffer.rewind();
        writeBehavior.write(buffer, bufferAbsolutePosition);
        bufferAbsolutePosition += buffer.limit();
        buffer.clear();
    }

    @Override
    public long position() throws IOException {
        assertOpen();
        return absolutePosition;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        assertOpen();

        if (readBehavior != null) {
            readModeSeek(newPosition);
        } else {
            writeModeSeek(newPosition);
        }

        return this;
    }

    private void readModeSeek(long newPosition) {
        // seek exited the bounds of the internal buffer, invalidate it
        if (newPosition < bufferAbsolutePosition || newPosition > bufferAbsolutePosition + buffer.limit()) {
            buffer.clear();
            buffer.limit(0);
        // seek is within the internal buffer, just adjust buffer position
        } else {
            buffer.position((int) (newPosition - bufferAbsolutePosition));
        }
        absolutePosition = newPosition;
    }

    private void writeModeSeek(long newPosition) {
        if (!writeBehavior.canSeek(newPosition)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "The backing resource does not support this position change."));
        }

        flushWriteBuffer();
        absolutePosition = newPosition;
        bufferAbsolutePosition = newPosition;
    }

    @Override
    public long size() throws IOException {
        assertOpen();
        if (readBehavior != null) {
            return readBehavior.getResourceLength();
        } else {
            return absolutePosition;
        }
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        assertOpen();
        writeBehavior.resize(size);
        return this;
    }

    @Override
    public boolean isOpen() {
        return !isClosed;
    }

    @Override
    public void close() throws IOException {
        if (writeBehavior != null) {
            flushWriteBuffer();
            writeBehavior.commit(absolutePosition);
        }

        // close is documented as idempotent
        isClosed = true;
        buffer = null;
    }

    private void assertCanRead() {
        if (readBehavior == null) {
            throw LOGGER.logExceptionAsError(new NonReadableChannelException());
        }
    }

    private void assertCanWrite() {
        if (writeBehavior == null) {
            throw LOGGER.logExceptionAsError(new NonWritableChannelException());
        }
    }

    private void assertOpen() throws ClosedChannelException {
        if (isClosed) {
            throw LOGGER.logThrowableAsError(new ClosedChannelException());
        }
    }
}
