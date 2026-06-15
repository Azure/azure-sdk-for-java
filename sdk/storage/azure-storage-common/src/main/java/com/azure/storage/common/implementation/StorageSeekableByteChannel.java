// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

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
        int read(ByteBuffer dst, long sourceOffset) throws IOException;

        /**
         * Gets the length of the resource. The returned value may have been cached from previous operations on this
         * instance.
         * <p>
         * Implementations <strong>must not</strong> perform I/O from this method. If the resource length is not yet
         * known, implementations should return a negative value (conventionally {@code -1}); the channel's
         * {@code read(...)} path treats a negative return as "unknown" and falls through to its normal refill, which
         * is expected to populate the length as a side-effect (e.g. from a range-GET's {@code Content-Range}
         * header). {@link StorageSeekableByteChannel#size()} seeds the length on demand via a minimal range probe
         * when callers need it before any read has happened, so implementations never need to perform I/O from this
         * accessor to satisfy {@link java.nio.channels.SeekableByteChannel#size()}.
         *
         * @return The length in bytes, or a negative value if not yet known.
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
        void write(ByteBuffer src, long destOffset) throws IOException;

        /**
         * Calls any necessary commit/flush calls on the backing resource.
         * @param totalLength Total length of the bytes being committed (necessary for some resource types).
         */
        void commit(long totalLength);

        /**
         * Determines whether the write behavior can support a random seek to this position. May fetch information
         * from the service to determine if possible.
         * @param position Desired seek position.
         * @throws UnsupportedOperationException describing why the attempted seek is unsupported.
         */
        void assertCanSeek(long position);

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
     * Constructs an instance of this class in read mode.
     * @param chunkSize Size of the internal channel buffer to use for data transfer, and for individual REST transfers.
     * @param readBehavior Behavior for reading from the backing Storage resource.
     * @param startingPosition Initial position for the channel.
     * @throws IllegalArgumentException If both read and write behavior are given.
     */
    public StorageSeekableByteChannel(int chunkSize, ReadBehavior readBehavior, long startingPosition) {
        this(chunkSize, null, Objects.requireNonNull(readBehavior), startingPosition);

        // indicate first read needs to call into readBehavior.
        buffer.limit(0);
    }

    /**
     * Constructs an instance of this class in write mode.
     * @param chunkSize Size of the internal channel buffer to use for data transfer, and for individual REST transfers.
     * @param writeBehavior Behavior for writing to the backing Storage resource.
     * @param startingPosition Initial position for the channel.
     * @throws IllegalArgumentException If both read and write behavior are given.
     */
    public StorageSeekableByteChannel(int chunkSize, WriteBehavior writeBehavior, long startingPosition) {
        this(chunkSize, Objects.requireNonNull(writeBehavior), null, startingPosition);
    }

    private StorageSeekableByteChannel(int chunkSize, WriteBehavior writeBehavior, ReadBehavior readBehavior,
        long startingPosition) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("'chunkSize' must be a positive number.");
        }
        if (startingPosition < 0) {
            throw new IllegalArgumentException("'startingPosition' cannot be a negative number.");
        }

        this.readBehavior = readBehavior;
        this.writeBehavior = writeBehavior;
        buffer = ByteBuffer.allocate(chunkSize);
        absolutePosition = startingPosition;
        bufferAbsolutePosition = 0;
    }

    /**
     * Gets the read-behavior used by this channel, if any.
     * @return {@link ReadBehavior} of this channel. Null if the channel is configured for writes.
     */
    public ReadBehavior getReadBehavior() {
        return readBehavior;
    }

    /**
     * Gets the write-behavior used by this channel, if any.
     * @return {@link WriteBehavior} of this channel. Null if the channel is configured for reads.
     */
    public WriteBehavior getWriteBehavior() {
        return writeBehavior;
    }

    /**
     * @return Transfer chunk size used by this channel.
     */
    public int getChunkSize() {
        return buffer.capacity();
    }

    /*
     * Implementation notes for read(ByteBuffer):
     *
     * Caller pattern. The typical caller drives a loop:
     *     while (channel.read(dst) != -1) { ... }
     * After the final bytes are drained from the internal buffer, absolutePosition equals
     * readBehavior.getResourceLength() and the next read() call is expected to return -1.
     *
     * Zero-remaining destination. Per the ReadableByteChannel/SeekableByteChannel contract, if dst.remaining() == 0
     * (e.g. dst = ByteBuffer.allocate(0) or a previously-filled buffer with position() == limit()), the channel must
     * make no attempt to read and must return 0 -- NOT -1 -- even when the underlying resource has already reached
     * end-of-stream. Without an explicit short-circuit, the cached-EOF fast-path would return -1 in violation of the
     * contract, and the not-yet-EOF path would issue a wasted refillReadBuffer() service round-trip only to
     * ultimately return 0.
     *
     * EOF short-circuit. When the internal buffer is empty, the implementation checks whether absolutePosition has
     * already reached (or exceeded) the cached resource length and returns -1 directly. Without this short-circuit,
     * an empty buffer would fall into refillReadBuffer(absolutePosition), which delegates to readBehavior.read(...)
     * and issues a real HTTP range GET at an offset >= resourceLength.
     *
     * Per the ReadBehavior#getResourceLength() contract, the call below is required to be a non-blocking accessor
     * that returns a negative value (conventionally -1) when the length is not yet known. On the very first read --
     * before any refill has populated the implementation's cached length -- the call returns -1, the guard
     * `resourceLength >= 0` fails, and execution falls through to refillReadBuffer(...) as on main. The first refill
     * populates the implementation's cache as a side-effect (typically from the response's Content-Range header),
     * so on subsequent reads the short-circuit becomes effective and prevents the wasted EOF-probe round-trip after
     * a sequential drain. {@link #size()} seeds the length on demand so the JDK {@link SeekableByteChannel#size()}
     * contract is preserved even before any read has happened.
     *
     * How absolutePosition can reach (or exceed) resourceLength:
     *   1. Normal sequential drain: after the last bytes are consumed, the refill branch sets
     *      absolutePosition = resourceLength, and the caller's loop re-enters read() once more expecting -1.
     *      This is the dominant case and produces absolutePosition == resourceLength.
     *   2. Construction with startingPosition >= resourceLength -- the constructor only validates >= 0 and does not
     *      clamp against the resource length.
     *   3. position(newPosition) -> readModeSeek(newPosition) performs no upper-bound check against resourceLength,
     *      so a caller can seek strictly past EOF (absolutePosition > resourceLength).
     *   4. getResourceLength() is documented as possibly cached; if it is later refreshed (or the underlying blob is
     *      replaced with a shorter blob), a previously-valid absolutePosition can become strictly greater than the
     *      refreshed resourceLength without the channel doing anything wrong.
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        assertOpen();
        assertCanRead();
        if (dst.isReadOnly()) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("'dst' is read-only and cannot be written to."));
        }

        // See contract note (zero-remaining destination) in the comment block above this method.
        if (!dst.hasRemaining()) {
            return 0;
        }

        if (buffer.remaining() == 0) {
            // See EOF short-circuit note in the comment block above this method. getResourceLength() is contracted
            // to be a non-blocking accessor that returns a negative value when the length is not yet known, so on
            // the first read this guard simply falls through to the refill below.
            long resourceLength = readBehavior.getResourceLength();
            if (resourceLength >= 0 && absolutePosition >= resourceLength) {
                absolutePosition = resourceLength;
                return -1;
            }
            if (refillReadBuffer(absolutePosition) == -1) {
                // cap any position overshooting if channel is at end. Skip if length is still unknown (corner
                // case: very first refill returned -1 without a Content-Range to seed the behavior's cache).
                long endOfStream = readBehavior.getResourceLength();
                if (endOfStream >= 0) {
                    absolutePosition = endOfStream;
                }
                return -1;
            }
            // buffer still empty after refill, no EOF signal, no exception: just return zero
            if (buffer.remaining() == 0) {
                return 0;
            }
        }

        int read = Math.min(buffer.remaining(), dst.remaining());
        ByteBuffer temp = buffer.duplicate();
        temp.limit(temp.position() + read);
        dst.put(temp);
        buffer.position(buffer.position() + read);
        absolutePosition += read;
        return read;
    }

    private int refillReadBuffer(long newBufferAbsolutePosition) throws IOException {
        buffer.clear();
        // This delegates to the backing behavior and may issue a service range read from newBufferAbsolutePosition.
        int read = readBehavior.read(buffer, newBufferAbsolutePosition);
        buffer.rewind();
        buffer.limit(Math.max(read, 0));
        // After read(...), shipped ReadBehavior implementations have populated their resource length from the
        // response's Content-Range header. Guard against the corner case where the length is still unknown
        // (returned -1) so we don't clobber bufferAbsolutePosition with a negative value.
        long observedLength = readBehavior.getResourceLength();
        bufferAbsolutePosition
            = observedLength >= 0 ? Math.min(newBufferAbsolutePosition, observedLength) : newBufferAbsolutePosition;
        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        assertOpen();
        assertCanWrite();

        int write = Math.min(src.remaining(), buffer.remaining());
        if (write > 0) {
            ByteBuffer temp = src.duplicate();
            temp.limit(temp.position() + write);
            buffer.put(temp);
            src.position(src.position() + write);
        }

        if (buffer.remaining() == 0) {
            try {
                flushWriteBuffer();
            } catch (RuntimeException e) {
                // undo write on failed flush by rewinding buffer position the amount it was incremented
                buffer.position(buffer.position() - write);
                throw LOGGER.logExceptionAsError(e);
            }
        }

        absolutePosition += write;
        return write;
    }

    private void flushWriteBuffer() throws IOException {
        if (buffer.position() == 0) {
            return;
        }

        int startingPosition = buffer.position();
        buffer.limit(buffer.position());
        buffer.rewind();
        try {
            writeBehavior.write(buffer, bufferAbsolutePosition);
        } catch (RuntimeException e) {
            // restore buffer state if write fails
            buffer.limit(buffer.capacity());
            buffer.position(startingPosition);
            throw LOGGER.logExceptionAsError(e);
        }
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

    private void writeModeSeek(long newPosition) throws IOException {
        writeBehavior.assertCanSeek(newPosition);

        flushWriteBuffer();
        absolutePosition = newPosition;
        bufferAbsolutePosition = newPosition;
    }

    @Override
    public long size() throws IOException {
        assertOpen();
        if (readBehavior != null) {
            long length = readBehavior.getResourceLength();
            if (length < 0) {
                // ReadBehavior#getResourceLength() is contracted to be a non-blocking accessor and returns a
                // negative value when no read has populated the length yet. To preserve the
                // SeekableByteChannel#size() contract for callers who query size before reading, seed the cache
                // here via a minimal one-byte range probe -- this triggers exactly one service round-trip
                // (typically the same round-trip a HEAD would cost) and lets the behavior parse the resource
                // length out of the response's Content-Range header. Subsequent size() calls hit the cache.
                ByteBuffer probe = ByteBuffer.allocate(1);
                readBehavior.read(probe, 0);
                length = readBehavior.getResourceLength();
            }
            return length;
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
