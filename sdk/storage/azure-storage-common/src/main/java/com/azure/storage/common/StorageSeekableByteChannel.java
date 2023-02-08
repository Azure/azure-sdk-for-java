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
public abstract class StorageSeekableByteChannel implements SeekableByteChannel {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSeekableByteChannel.class);

    private final StorageChannelMode _mode;

    private boolean _isClosed;

    private ByteBuffer _currentReadBuffer;

    protected StorageSeekableByteChannel(StorageChannelMode mode) {
        _mode = mode;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        AssertOpen();
        AssertCanRead();
        return 0;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        AssertOpen();
        AssertCanWrite();
        return 0;
    }

    @Override
    public long position() throws IOException {
        AssertOpen();
        return 0;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        AssertOpen();
        AssertCanSeek();
        return null;
    }

    @Override
    public long size() throws IOException {
        AssertOpen();
        return 0;
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        AssertOpen();
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
        _currentReadBuffer = null;
    }

    private void AssertCanSeek() {
        // only support seeking in read mode; most Storage resources do not allow random access write.
        AssertCanRead();
    }

    private void AssertCanRead() {
        if (_mode != StorageChannelMode.READ) {
            throw LOGGER.logExceptionAsError(new NonReadableChannelException());
        }
    }

    private void AssertCanWrite() {
        if (_mode != StorageChannelMode.WRITE) {
            throw LOGGER.logExceptionAsError(new NonWritableChannelException());
        }
    }

    private void AssertOpen() throws ClosedChannelException {
        if (_isClosed){
            throw LOGGER.logThrowableAsError(new ClosedChannelException());
        }
    }
}
