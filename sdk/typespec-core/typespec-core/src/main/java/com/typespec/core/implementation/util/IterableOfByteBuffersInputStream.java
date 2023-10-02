// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Objects;

/**
 * An {@link InputStream} implementation that is based on {@link Iterator} of {@link java.nio.ByteBuffer}.
 */
public class IterableOfByteBuffersInputStream extends InputStream {

    private final Iterator<ByteBuffer> buffers;
    private ByteBuffer currentBuffer;

    public IterableOfByteBuffersInputStream(Iterable<ByteBuffer> buffersIterable) {
        Objects.requireNonNull(buffersIterable, "'buffers' must not be null");
        this.buffers = buffersIterable.iterator();
    }

    @Override
    public synchronized int read() throws IOException {
        ByteBuffer buffer = getCurrentBuffer();
        if (buffer == null) {
            return -1;
        }

        return buffer.get() & 0xff; // Make positive int. See ByteArrayInputStream.read() for reference.
    }

    @Override
    public synchronized int read(byte[] b, int offset, int length) throws IOException {
        ByteBuffer buffer = getCurrentBuffer();
        if (buffer == null) {
            return -1;
        }

        int read = 0;
        while (length > 0 && buffer != null) {
            int toTransfer = Math.min(buffer.remaining(), length);
            buffer.get(b, offset, toTransfer);
            read += toTransfer;
            offset += toTransfer;
            length -= toTransfer;
            buffer = getCurrentBuffer();
        }

        return read;
    }

    @Override
    public synchronized int available() throws IOException {
        ByteBuffer buffer = getCurrentBuffer();
        if (buffer == null) {
            return 0;
        } else {
            return buffer.remaining();
        }
    }

    private ByteBuffer getCurrentBuffer() {
        if (currentBuffer != null && currentBuffer.hasRemaining()) {
            return currentBuffer;
        } else {
            while (buffers.hasNext()) {
                ByteBuffer candidate = buffers.next();
                if (candidate.hasRemaining()) {
                    ByteBuffer copy = candidate.duplicate();
                    currentBuffer = copy;
                    return copy;
                }
            }
            return null;
        }
    }
}
