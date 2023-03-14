// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Wraps a {@link ByteBuffer} for access via the {@link InputStream} API.
 * Supports {@link InputStream#mark(int)} via {@link ByteBuffer#mark()}.
 */
public class ByteBufferMarkableInputStream extends InputStream {
    private final ByteBuffer bb;

    public ByteBufferMarkableInputStream(ByteBuffer buffer) {
        bb = Objects.requireNonNull(buffer);
    }

    @Override
    public int read() throws IOException {
        return bb.hasRemaining() ? bb.get() & 255 : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (!this.bb.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, this.bb.remaining());
        this.bb.get(b, off, len);
        return len;
    }

    @Override
    public int available() {
        return bb.remaining();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readlimit) {
        bb.mark();
    }

    @Override
    public void reset() {
        bb.reset();
    }

    @Override
    public long skip(long n) {
        if (n >= bb.remaining()) {
            int result = bb.remaining();
            bb.position(bb.limit());
            return result;
        }
        bb.position(bb.position() + (int) n);
        return n;
    }
}
