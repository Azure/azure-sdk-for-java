// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A byte channel that maintains a current position.
 * <p>
 * This type is primarily offered to support some jdk convenience methods such as
 * {@link Files#createFile(Path, FileAttribute[])} which requires opening a channel and closing it. A very limited set
 * of functionality is offered here. More specifically, only reads--no writes or seeks--are supported.
 * <p>
 * {@link NioBlobInputStream} and {@link NioBlobOutputStream} are the preferred types for reading and writing blob data.
 */
public class AzureSeekableByteChannel implements SeekableByteChannel {
    private final ClientLogger logger = new ClientLogger(AzureSeekableByteChannel.class);

    private final NioBlobInputStream inputStream; // Always set the mark at 0 w/ max int. repositioning is a reset and then a skip. Validate length
    private final NioBlobOutputStream outputStream;
    private long position; // Needs to be threadsafe?
    private boolean closed = false;
    private final ReentrantLock lock;

    AzureSeekableByteChannel(NioBlobInputStream inputStream) {
        this.inputStream = inputStream;
        inputStream.mark(Integer.MAX_VALUE);
        this.outputStream = null;
        this.position = 0;
        this.lock = new ReentrantLock();
    }

    AzureSeekableByteChannel(NioBlobOutputStream outputStream) {
        this.outputStream = outputStream;
        this.inputStream = null;
        this.position = 0;
        this.lock = new ReentrantLock();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        validateOpen();
        validateReadMode();

        int count = 0;

        lock.lock();
        try {
            int len = dst.remaining(); // In case another thread modifies dst
            byte[] buf = new byte[len];

            while (count < len) {
                int retCount = this.inputStream.read(buf, count, len - count);
                if (retCount == -1) {
                    break;
                }
                count += retCount;
            }
            dst.put(buf, 0, count);
            this.position += count;
        } finally { // catch block
            lock.unlock();
        }
        return count;
        // Would it be easier to just do the downloads directly?
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        validateOpen();
        validateWriteMode();

        int length = src.remaining();

        lock.lock();
        try {

            this.position += src.remaining();
            byte[] buf = new byte[length];
            src.get(buf);
            this.outputStream.write(buf);
        } finally { // Catch block
            lock.unlock();
        }
        return length;
    }

    @Override
    public long position() throws IOException {
        validateOpen();

        return this.position;
    }

    @Override
    public AzureSeekableByteChannel position(long newPosition) throws IOException {
        validateOpen();
        validateReadMode();

        lock.lock();
        try {
            this.inputStream.reset();
            this.inputStream.mark(Integer.MAX_VALUE);
            this.inputStream.skip(newPosition);
            this.position = newPosition;
        } finally { // catch block
            lock.unlock();
        }
        return this;
    }

    @Override
    public long size() throws IOException {
        validateOpen();

        return 0;
    }

    @Override
    public AzureSeekableByteChannel truncate(long size) throws IOException {
        throw LoggingUtility.logError(logger, new UnsupportedOperationException());
    }

    @Override
    public boolean isOpen() {
        return !this.closed;
    }

    @Override
    public void close() throws IOException {
        if (this.inputStream != null) {
            this.inputStream.close();
        } else {
            this.outputStream.close();
        }
        this.closed = true;
    }

    private void validateOpen() throws ClosedChannelException {
        if (this.closed) {
            throw LoggingUtility.logError(logger, new ClosedChannelException());
        }
    }

    private void validateReadMode() {
        if (this.inputStream == null) {
            throw LoggingUtility.logError(logger, new NonReadableChannelException());
        }
    }

    private void validateWriteMode() {
        if (this.outputStream == null) {
            throw LoggingUtility.logError(logger, new NonWritableChannelException());
        }
    }
}
