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
public abstract class StorageSeekableByteChannel implements SeekableByteChannel {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSeekableByteChannel.class);

    private final StorageChannelMode _mode;

    private boolean _isClosed;

    private ByteBuffer _readBuffer;
    private long _readBufferAbsolutePosition;

    private long _absolutePosition;
    private long _channelSize;

    /**
     * A flag to determine if the stream is faulted, if so the last error will be thrown on next operation.
     */
    protected volatile boolean streamFaulted;

    /**
     * Holds the last exception this stream encountered.
     */
    protected IOException lastError;

    protected StorageSeekableByteChannel(int chunkSize, StorageChannelMode mode) {
        _mode = Objects.requireNonNull(mode);
        if (_mode == StorageChannelMode.READ) {
            _readBuffer = ByteBuffer.allocate(chunkSize);
            _readBuffer.limit(0);
            _readBufferAbsolutePosition = 0;
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        assertOpen();
        assertCanRead();
        if (dst.isReadOnly()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("ByteBuffer dst must support writes."));
        }

        if (_readBuffer.remaining() == 0) {
            refillReadBuffer(_absolutePosition);
        }

        int read = Math.min(_readBuffer.remaining(), dst.remaining());
        ByteBuffer temp = _readBuffer.duplicate();
        temp.limit(temp.position() + read);
        dst.put(temp);
        _readBuffer.position(_readBuffer.position() + read);
        _absolutePosition += read;
        return read;
    }

    private void refillReadBuffer(long newBufferAbsolutePosition) {
        _readBuffer.clear();
        int read = dispatchRead(_readBuffer, newBufferAbsolutePosition);
        _readBuffer.rewind();
        _readBuffer.limit(read);
        _readBufferAbsolutePosition = newBufferAbsolutePosition;
    }

    /**
     * Dispatches a read operation to the backing Storage resource.
     * @param dst Destination to read the Storage resource into.
     * @param sourceOffset Offset to read from the Storage resource.
     * @return Number of bytes read from the Storage resource.
     */
    protected abstract int dispatchRead(ByteBuffer dst, long sourceOffset);

    @Override
    public int write(ByteBuffer src) throws IOException {
        assertOpen();
        assertCanWrite();
        throw new UnsupportedOperationException("not implemented");
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
        if (newPosition < _readBufferAbsolutePosition || newPosition > _readBufferAbsolutePosition + _readBuffer.limit()) {
            _readBuffer.clear();
            _readBuffer.limit(0);
        // seek is within the internal buffer, just adjust buffer position
        } else {
            _readBuffer.position((int)(newPosition - _readBufferAbsolutePosition));
        }

        _absolutePosition = newPosition;

        return this;
    }

    @Override
    public long size() throws IOException {
        assertOpen();
        return _channelSize;
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
        // close is documented as idempotent
        _isClosed = true;
        _readBuffer = null;
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
