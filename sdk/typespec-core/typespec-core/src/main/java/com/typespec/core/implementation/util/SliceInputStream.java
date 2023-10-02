// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.util;

import com.typespec.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * An {@link InputStream} that gives access to a slice of underlying {@link InputStream}.
 */
public final class SliceInputStream extends InputStream {

    private static final ClientLogger LOGGER = new ClientLogger(SliceInputStream.class);

    private final InputStream innerStream;
    private final long startOfSlice;
    private final long endOfSlice;
    private long innerPosition = 0;
    private long mark = -1;

    /**
     * Creates {@link SliceInputStream}.
     * @param inputStream An {@link InputStream} to be sliced.
     * @param position An offset of the slice.
     * @param count Maximum amount of bytes to read from the slice.
     * @throws NullPointerException if {@code inputStream} is {@code null}.
     * @throws IllegalArgumentException if {@code position} is negative.
     * @throws IllegalArgumentException if {@code count} is negative.
     */
    public SliceInputStream(InputStream inputStream, long position, long count) {
        this.innerStream = Objects.requireNonNull(inputStream, "'inputStream' cannot be null");
        if (position < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'position' cannot be negative"));
        }
        if (count < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'count' cannot be negative"));
        }
        this.startOfSlice = position;
        this.endOfSlice = position + count;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        if (ensureInWindow() < 0) {
            return 0;
        }
        if (innerPosition > endOfSlice) {
            return 0;
        }

        long toSkip = Math.min(n, Math.max(0, endOfSlice - innerPosition));
        long skipped = innerStream.skip(toSkip);
        innerPosition += skipped;
        return skipped;
    }

    @Override
    public synchronized int read() throws IOException {
        if (ensureInWindow() < 0) {
            return -1;
        }
        int nextByte = innerStream.read();
        if (nextByte >= 0) {
            innerPosition++;
        }
        return nextByte;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (ensureInWindow() < 0) {
            return -1;
        }
        int read = innerStream.read(b, off, len);
        if (read > 0) {
            innerPosition += read;
            if (innerPosition > endOfSlice) {
                return (int) (read - (innerPosition - endOfSlice));
            }
        }
        return read;
    }

    private long ensureInWindow() throws IOException {
        if (startOfSlice == endOfSlice) {
            // empty window
            return -1;
        }
        if (innerPosition >= endOfSlice) {
            return -1;
        }

        long totalSkipped = 0;
        while (innerPosition < startOfSlice) {
            long skipped = innerStream.skip(startOfSlice - innerPosition);
            totalSkipped += skipped;
            innerPosition += skipped;
            if (skipped == 0) {
                int nextByte = innerStream.read();
                if (nextByte < 0) {
                    return -1;
                } else {
                    totalSkipped += 1;
                    innerPosition += 1;
                }
            }
        }

        return totalSkipped;
    }

    @Override
    public void close() throws IOException {
        innerStream.close();
    }

    @Override
    public boolean markSupported() {
        return innerStream.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            ensureInWindow();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
        innerStream.mark(readlimit);
        mark = innerPosition;
    }

    @Override
    public synchronized void reset() throws IOException {
        innerStream.reset();
        innerPosition = mark;
    }
}
