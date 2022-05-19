// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} that gives access to a slice of underlying {@link InputStream}.
 */
public final class SliceInputStream extends InputStream {

    private final InputStream innerStream;
    private final long startOfSlice;
    private final long endOfSlice;
    private long currentPosition = 0;

    /**
     * Creates {@link SliceInputStream}.
     * @param inputStream An {@link InputStream} to be sliced.
     * @param position An offset of the slice.
     * @param count Maximum amount of bytes to read from the slice.
     */
    public SliceInputStream(InputStream inputStream, long position, long count) {
        this.innerStream = inputStream;
        this.startOfSlice = position;
        this.endOfSlice = position + count;
    }

    @Override
    public long skip(long n) throws IOException {
        if (ensureInWindow() < 0) {
            return 0;
        }
        if (currentPosition > endOfSlice) {
            return 0;
        }

        long toSkip = Math.min(n, endOfSlice);
        return innerStream.skip(toSkip);
    }

    @Override
    public int read() throws IOException {
        if (ensureInWindow() < 0) {
            return -1;
        }
        int nextByte = innerStream.read();
        if (nextByte >= 0) {
            currentPosition++;
        }
        return nextByte;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (ensureInWindow() < 0) {
            return -1;
        }
        int read = innerStream.read(b, off, len);
        if (read > 0) {
            currentPosition += read;
            if (currentPosition > endOfSlice) {
                return (int) (read - (currentPosition - endOfSlice));
            }
        }
        return read;
    }

    private long ensureInWindow() throws IOException {
        if (currentPosition > endOfSlice) {
            return -1;
        }

        long totalSkipped = 0;
        while (currentPosition < startOfSlice) {
            long skipped = innerStream.skip(startOfSlice - currentPosition);
            totalSkipped += skipped;
            currentPosition += skipped;
            if (skipped == 0) {
                int nextByte = innerStream.read();
                if (nextByte < 0) {
                    return -1;
                } else {
                    totalSkipped += 1;
                    currentPosition += 1;
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
        innerStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        innerStream.reset();
    }
}
