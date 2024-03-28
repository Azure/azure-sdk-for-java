// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.util.BinaryData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Represents a repeating input stream with mark support enabled.
 */
public class RepeatingInputStream extends InputStream {
    private static final byte[] RANDOM_BYTES;
    private final long size;

    private long mark = 0;
    private long readLimit = Long.MAX_VALUE;
    private long pos = 0;
    private final byte[] source;
    static {
        int randomLength = Integer.parseInt(
            System.getProperty("azure.core.perf.test.data.buffer.size", "1048576")); // 1MB default;

        Random random = new Random(0);
        RANDOM_BYTES = new byte[randomLength];
        random.nextBytes(RANDOM_BYTES);
    }

    /**
     * Creates an Instance of the repeating input stream.
     * @param size the size of the stream.
     */
    public RepeatingInputStream(long size) {
        this.size = size;
        this.source = RANDOM_BYTES;
    }

    /**
     * Creates an instance of the stream which repeats the given buffer.
     * @param source the buffer to repeat. Must be relatively small and fit into memory.
     * @param size the size of the stream.
     */
    public RepeatingInputStream(BinaryData source, long size) {
        this.size = size;
        this.source = source.toBytes();
    }

    @Override
    public synchronized int read() {
        return (pos < size) ? getByte(pos) : -1;
    }

    @Override
    public synchronized int read(byte[] b) {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) {
        if (pos >= size || pos >= readLimit) {
            return -1;
        }

        int posSrc = (int)(pos % source.length);
        int readCount = Math.min(len, source.length - posSrc);

        long remainingDest = this.size - this.pos;
        if (remainingDest < readCount) {
            readCount = (int) remainingDest;
        }
        System.arraycopy(source, posSrc, b, off, readCount);
        pos += readCount;

        return readCount;
    }

    @Override
    public synchronized void mark(int readLimit) {
        this.readLimit = readLimit;
        this.mark = this.pos;
    }

    /**
     * Same as {@link #mark(int)} but takes long.
     * @param readLimit read limit.
     */
    public synchronized void mark(long readLimit) {
        this.readLimit = readLimit;
        this.mark = this.pos;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() {
        this.pos = this.mark;
    }

    @Override
    public int available() throws IOException {
        long remaining = this.size - this.pos;
        if (remaining > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) remaining;
        }
    }

    private int getByte(long pos) {
        return source[(int)(pos % source.length)] & 0xFF;
    }
}

