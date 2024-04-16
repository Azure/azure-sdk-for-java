// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Wraps a {@link ByteBuffer} for access via the {@link InputStream} API.
 * Supports {@link InputStream#mark(int)} via {@link ByteBuffer#mark()}.
 */
public class MappedByteBufferInputStream extends InputStream {

    private final ByteBuffer buf;

    public MappedByteBufferInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }
}
