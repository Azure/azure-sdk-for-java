// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.FluxUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Wraps a {@link ByteBuffer} for access via the {@link InputStream} API.
 * Supports {@link InputStream#mark(int)} via {@link ByteBuffer#mark()}.
 */
public class MappedByteBufferInputStream extends InputStream {

    private final ByteBuffer byteBuffer;

    /**
     * Creates a new input stream from the given {@link ByteBuffer}.
     *
     * @param buffer The buffer to wrap.
     */
    public MappedByteBufferInputStream(ByteBuffer buffer) {
        // Defensive copy of the ByteBuffer
        this.byteBuffer = ByteBuffer.wrap(FluxUtil.byteBufferToArray(buffer));
        this.byteBuffer.flip(); // Reset position after copying data
    }

    @Override
    public int read() throws IOException {
        if (!byteBuffer.hasRemaining()) {
            return -1;
        }
        return byteBuffer.get() & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!byteBuffer.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, byteBuffer.remaining());
        byteBuffer.get(bytes, off, len);
        return len;
    }
}
