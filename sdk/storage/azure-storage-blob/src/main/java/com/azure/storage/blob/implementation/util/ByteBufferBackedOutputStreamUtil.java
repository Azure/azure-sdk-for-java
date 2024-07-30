// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.implementation.util;

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * This class provides methods for seekable byte channel tests.
 */
public class ByteBufferBackedOutputStreamUtil extends OutputStream {
    private final ByteBuffer dst;

    /**
     * Creates a new ByteBufferBackedOutputStreamUtil.
     *
     * @param dst The destination byte buffer.
     */
    public ByteBufferBackedOutputStreamUtil(ByteBuffer dst) {
        this.dst = dst;
    }

    @Override
    public void write(int b) {
        dst.put((byte) b);
    }

    @Override
    public void write(byte[] b) {
        dst.put(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        dst.put(b, off, len);
    }
}
