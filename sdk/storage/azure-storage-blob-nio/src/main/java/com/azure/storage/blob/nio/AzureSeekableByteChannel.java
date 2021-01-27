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
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A byte channel that maintains a current position.
 * <p>
 * This type is primarily offered to support some jdk convenience methods such as
 * {@link Files#createFile(Path, FileAttribute[])} which requires opening a channel and closing it. A channel may only
 * be opened in read mode OR write mode. It may not be opened in read/write mode. Seeking is supported for reads, but
 * not for writes. Modifications to existing files is not permitted--only creating new files or overwriting existing
 * files.
 * <p>
 * This type is not threadsafe to prevent having to hold locks across network calls.
 * <p>
 * {@link NioBlobInputStream} and {@link NioBlobOutputStream} are the preferred types for reading and writing blob data
 * and are used internally by this type.
 */
public class AzureSeekableByteChannel implements SeekableByteChannel {
    private final ClientLogger logger = new ClientLogger(AzureSeekableByteChannel.class);

    private final NioBlobInputStream inputStream; // Always set the mark at 0 w/ max int. repositioning is a reset and then a skip. Validate length
    private final NioBlobOutputStream outputStream;
    private long position; // Needs to be threadsafe?
    private boolean closed = false;
    private final Path path;

    AzureSeekableByteChannel(NioBlobInputStream inputStream, Path path) {
        this.inputStream = inputStream;
        inputStream.mark(Integer.MAX_VALUE);
        this.outputStream = null;
        this.position = 0;
        this.path = path;
    }

    AzureSeekableByteChannel(NioBlobOutputStream outputStream, Path path) {
        this.outputStream = outputStream;
        this.inputStream = null;
        this.position = 0;
        this.path = path;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        validateOpen();
        validateReadMode();

        // See comments in position()
        if (this.position > this.size()) {
            return -1;
        }

        int count = 0;

        int len = dst.remaining();
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

        return count;
        // Would it be easier to just do the downloads directly?
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        validateOpen();
        validateWriteMode();

        int length = src.remaining();

        this.position += src.remaining();
        byte[] buf = new byte[length];
        src.get(buf);
        this.outputStream.write(buf);

        return length;
    }

    @Override
    public long position() throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        validateOpen();

        return this.position;
    }

    @Override
    public AzureSeekableByteChannel position(long newPosition) throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        validateOpen();
        validateReadMode();

        if (newPosition < 0) {
            throw LoggingUtility.logError(logger, new IllegalArgumentException("Seek position cannot be negative"));
        }

        /*
        The javadoc says seeking past the end for reading is legal and that it should indicate the end of the file on
        the next read. StorageInputStream doesn't allow this, but we can get around that by just playing with the
        position variable and skipping the actual read. We'll check in read if we've seeked past the end and short
        circuit there as well.

        Because we are in read mode this will always give us the size from properties.
         */
        if (newPosition > this.size()) {
            this.position = newPosition;
            return this;
        }
        this.inputStream.reset();
        this.inputStream.mark(Integer.MAX_VALUE);
        this.inputStream.skip(newPosition);
        this.position = newPosition;

        return this;
    }

    @Override
    public long size() throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        validateOpen();

        if (inputStream != null) {
            return inputStream.getBlobInputStream().getProperties().getBlobSize();
        } else {
            return position;
        }
    }

    @Override
    public AzureSeekableByteChannel truncate(long size) throws IOException {
        throw LoggingUtility.logError(logger, new UnsupportedOperationException());
    }

    @Override
    public boolean isOpen() {
        AzurePath.ensureFileSystemOpen(this.path);
        return !this.closed;
    }

    @Override
    public void close() throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
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
