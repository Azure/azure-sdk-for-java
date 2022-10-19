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
import java.nio.file.Path;

/**
 * A byte channel that maintains a current position.
 * <p>
 * A channel may only be opened in read mode OR write mode. It may not be opened in read/write mode. Seeking is
 * supported for reads, but not for writes. Modifications to existing files is not permitted--only creating new files or
 * overwriting existing files.
 * <p>
 * This type is not threadsafe to prevent having to hold locks across network calls.
 */
public final class AzureSeekableByteChannel implements SeekableByteChannel {
    private static final ClientLogger LOGGER = new ClientLogger(AzureSeekableByteChannel.class);

    private final NioBlobInputStream reader;
    private final NioBlobOutputStream writer;
    private long position;
    private boolean closed = false;
    private final Path path;
    /*
    If this type needs to be made threadsafe, closed should be volatile. We need to add a lock to guard updates to
    position or make it an atomicLong. If we have a lock, we have to be careful about holding while doing io ops and at
    least ensure timeouts are set. We probably have to duplicate or copy the buffers for at least writing to ensure they
     don't get overwritten.
     */

    AzureSeekableByteChannel(NioBlobInputStream inputStream, Path path) {
        this.reader = inputStream;
        /*
        We mark at the beginning (we always construct a stream to the beginning of the blob) to support seeking. We can
        effectively seek anywhere by always marking at the beginning of the blob and then a seek is resetting to that
        mark and skipping.
         */
        inputStream.mark(Integer.MAX_VALUE);
        this.writer = null;
        this.position = 0;
        this.path = path;
    }

    AzureSeekableByteChannel(NioBlobOutputStream outputStream, Path path) {
        this.writer = outputStream;
        this.reader = null;
        this.position = 0;
        this.path = path;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        validateOpen();
        validateReadMode();

        // See comments in position(), remember that position is 0-based and size() is exclusive
        if (this.position >= this.size()) {
            return -1; // at or past EOF
        }

        // If the buffer is backed by an array, we can write directly to that instead of allocating new memory.
        int pos;
        final int limit;
        final byte[] buf;
        if (dst.hasArray()) {
            // ByteBuffer has a position and limit that define the bounds of the writeable area, and that
            // area can be both smaller than the backing array and might not begin at array index 0.
            pos = dst.position();
            limit = pos + dst.remaining();
            buf = dst.array();
        } else {
            pos = 0;
            limit = dst.remaining();
            buf = new byte[limit];
        }

        while (pos < limit) {
            int byteCount = this.reader.read(buf, pos, limit - pos);
            if (byteCount == -1) {
                break;
            }
            pos += byteCount;
        }

        /*
        Either write to the destination if we had to buffer separately or just set the position correctly if we wrote
        underneath the buffer
         */
        int count;
        if (dst.hasArray()) {
            count = pos - dst.position();
            dst.position(pos);
        } else {
            count = pos; // original position was 0
            dst.put(buf, 0, count);
        }

        this.position += count;
        return count;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        validateOpen();
        validateWriteMode();

        final int length = src.remaining();
        this.position += length;

        /*
        If the buffer is backed by an array, we can read directly from that instead of allocating new memory.
        Set the position correctly if we read from underneath the buffer
         */
        int pos;
        byte[] buf;
        if (src.hasArray()) {
            // ByteBuffer has a position and limit that define the bounds of the readable area, and that
            // area can be both smaller than the backing array and might not begin at array index 0.
            pos = src.position();
            buf = src.array();
            src.position(pos + length);
        } else {
            pos = 0;
            buf = new byte[length];
            src.get(buf); // advances src.position()
        }
        // Either way, the src.position() and this.position have been updated before we know if this write
        // will succeed. (Original behavior.) It may be better to update position(s) only *after* success,
        // but then on IOException would we know if there was a partial write, and if so how much?
        this.writer.write(buf, pos, length);
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
            throw LoggingUtility.logError(LOGGER, new IllegalArgumentException("Seek position cannot be negative"));
        }

        /*
        The javadoc says seeking past the end for reading is legal and that it should indicate the end of the file on
        the next read. StorageInputStream doesn't allow this, but we can get around that by modifying the
        position variable and skipping the actual read (when read is called next); we'll check in read if we've seeked
        past the end and short circuit there as well.

        Because we are in read mode this will always give us the size from properties.
         */
        if (newPosition > this.size()) {
            this.position = newPosition;
            return this;
        }
        this.reader.reset(); // Because we always mark at the beginning, this will reset us back to the beginning.
        this.reader.mark(Integer.MAX_VALUE);
        long skipAmount = this.reader.skip(newPosition);
        if (skipAmount < newPosition) {
            throw new IOException("Could not set desired position");
        }
        this.position = newPosition;

        return this;
    }

    @Override
    public long size() throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        validateOpen();

        /*
        If we are in read mode, the size is the size of the file.
        If we are in write mode, the size is the amount of data written so far.
         */
        if (reader != null) {
            return reader.getBlobInputStream().getProperties().getBlobSize();
        } else {
            return position;
        }
    }

    @Override
    public AzureSeekableByteChannel truncate(long size) throws IOException {
        throw LoggingUtility.logError(LOGGER, new UnsupportedOperationException());
    }

    @Override
    public boolean isOpen() {
        AzurePath.ensureFileSystemOpen(this.path);
        return !this.closed;
    }

    @Override
    public void close() throws IOException {
        AzurePath.ensureFileSystemOpen(this.path);
        if (this.reader != null) {
            this.reader.close();
        } else {
            this.writer.close();
        }
        this.closed = true;
    }

    Path getPath() {
        return this.path;
    }

    private void validateOpen() throws ClosedChannelException {
        if (this.closed) {
            throw LoggingUtility.logError(LOGGER, new ClosedChannelException());
        }
    }

    private void validateReadMode() {
        if (this.reader == null) {
            throw LoggingUtility.logError(LOGGER, new NonReadableChannelException());
        }
    }

    private void validateWriteMode() {
        if (this.writer == null) {
            throw LoggingUtility.logError(LOGGER, new NonWritableChannelException());
        }
    }
}
