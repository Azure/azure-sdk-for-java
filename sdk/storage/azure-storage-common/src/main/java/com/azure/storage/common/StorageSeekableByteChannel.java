package com.azure.storage.common;

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
public class StorageSeekableByteChannel implements SeekableByteChannel {
    public interface ReadBehavior {
        /**
         * Reads from the backing resource.
         * @param dst Destination to read the resource into.
         * @param sourceOffset Offset to read from the resource.
         * @return Number of bytes read from the resource.
         */
        int read(ByteBuffer dst, long sourceOffset);

        /**
         * Gets the last known length of the resource.
         * @return The length in bytes.
         */
        long getCachedLength();
    }

    public interface WriteBehavior {
        /**
         * Writes to the backing resource.
         * @param src Bytes to write.
         * @param destOffset Offset of backing resource to write the bytes at.
         */
        void write(ByteBuffer src, long destOffset);
    }

    private static final ClientLogger LOGGER = new ClientLogger(StorageSeekableByteChannel.class);

    private final ReadBehavior _readBehavior;
    private final WriteBehavior _writeBehavior;
    private final StorageChannelMode _mode;

    private boolean _isClosed;

    private ByteBuffer _buffer;
    private long _bufferAbsolutePosition;

    private long _absolutePosition;

    protected StorageSeekableByteChannel(int chunkSize, StorageChannelMode mode, ReadBehavior readBehavior,
        WriteBehavior writeBehavior) {
        _mode = Objects.requireNonNull(mode);
        _buffer = ByteBuffer.allocate(chunkSize);
        _readBehavior = readBehavior;
        _writeBehavior = writeBehavior;

        _bufferAbsolutePosition = 0;
        if (_mode == StorageChannelMode.READ) {
            _buffer.limit(0);
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        assertOpen();
        assertCanRead();
        if (dst.isReadOnly()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("ByteBuffer dst must support writes."));
        }

        if (_buffer.remaining() == 0) {
            refillReadBuffer(_absolutePosition);
        }
        // if _readBuffer is still empty after refill, there are no bytes remaining
        if (_buffer.remaining() == 0) {
            return -1;
        }

        int read = Math.min(_buffer.remaining(), dst.remaining());
        ByteBuffer temp = _buffer.duplicate();
        temp.limit(temp.position() + read);
        dst.put(temp);
        _buffer.position(_buffer.position() + read);
        _absolutePosition += read;
        return read;
    }

    private void refillReadBuffer(long newBufferAbsolutePosition) {
        _buffer.clear();
        int read = _readBehavior.read(_buffer, newBufferAbsolutePosition);
        _buffer.rewind();
        _buffer.limit(Math.max(read, 0));
        _bufferAbsolutePosition = Math.min(newBufferAbsolutePosition, _readBehavior.getCachedLength());
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        assertOpen();
        assertCanWrite();

        int write = Math.min(src.remaining(), _buffer.remaining());
        ByteBuffer temp = src.duplicate();
        temp.limit(temp.position() + write);
        _buffer.put(temp);
        src.position(src.position() + write);
        _absolutePosition += write;

        if (_buffer.remaining() == 0) {
            flushWriteBuffer();
        }

        return write;
    }

    private void flushWriteBuffer() {
        _buffer.limit(_buffer.position());
        _buffer.rewind();
        _writeBehavior.write(_buffer, _bufferAbsolutePosition);
        _bufferAbsolutePosition += _buffer.limit();
        _buffer.clear();
    }

    @Override
    public long position() throws IOException {
        assertOpen();
        return _absolutePosition;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        assertOpen();
        assertCanSeek();

        // seek exited the bounds of the internal buffer, invalidate it
        if (newPosition < _bufferAbsolutePosition || newPosition > _bufferAbsolutePosition + _buffer.limit()) {
            _buffer.clear();
            _buffer.limit(0);
        // seek is within the internal buffer, just adjust buffer position
        } else {
            _buffer.position((int)(newPosition - _bufferAbsolutePosition));
        }

        _absolutePosition = newPosition;

        return this;
    }

    @Override
    public long size() throws IOException {
        assertOpen();
        return _readBehavior.getCachedLength();
        // TODO (jaschrep): what about when in write mode?
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        assertOpen();
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException());
    }

    @Override
    public boolean isOpen() {
        return !_isClosed;
    }

    @Override
    public void close() throws IOException {
        if (_mode == StorageChannelMode.WRITE) {
            flushWriteBuffer();
        }

        // close is documented as idempotent
        _isClosed = true;
        _buffer = null;
    }

    private void assertCanSeek() {
        // only support seeking in read mode; most Storage resources do not allow random access write.
        assertCanRead();
    }

    private void assertCanRead() {
        if (_mode != StorageChannelMode.READ) {
            throw LOGGER.logExceptionAsError(new NonReadableChannelException());
        }
    }

    private void assertCanWrite() {
        if (_mode != StorageChannelMode.WRITE) {
            throw LOGGER.logExceptionAsError(new NonWritableChannelException());
        }
    }

    private void assertOpen() throws ClosedChannelException {
        if (_isClosed){
            throw LOGGER.logThrowableAsError(new ClosedChannelException());
        }
    }
}
