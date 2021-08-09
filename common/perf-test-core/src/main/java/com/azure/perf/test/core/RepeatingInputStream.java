// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.io.InputStream;
import java.util.Random;

/**
 * Represents a repeating input stream with mark support enabled.
 */
public class RepeatingInputStream extends InputStream {
    private static final int RANDOM_BYTES_LENGTH = Integer.parseInt(
        System.getProperty("azure.core.perf.test.data.buffer.size", "1048576")); // 1MB default;
    private static final byte[] RANDOM_BYTES;
    private final long size;

    private long mark = 0;
    private long readLimit = Long.MAX_VALUE;
    private long pos = 0;

    static {
        Random random = new Random(0);
        RANDOM_BYTES = new byte[RANDOM_BYTES_LENGTH];
        random.nextBytes(RANDOM_BYTES);
    }

    /**
     * Creates an Instance of the repeating input stream.
     * @param size the size of the stream.
     */
    public  RepeatingInputStream(long size) {
        this.size = size;
    }

    @Override
    public synchronized int read() {
        return (pos < size) ? (RANDOM_BYTES[(int) (pos++ % RANDOM_BYTES_LENGTH)] & 0xFF) : -1;
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

        int readCount = Math.min(len, RANDOM_BYTES_LENGTH);
        System.arraycopy(RANDOM_BYTES, 0, b, off, readCount);
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
}

