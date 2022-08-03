// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

/**
 * This channel simulates cases where channel won't consume whole buffer.
 */
public final class PartialWriteChannel implements WritableByteChannel {
    private final WritableByteChannel delegate;

    public PartialWriteChannel(WritableByteChannel delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (src.remaining() > 1) {
            byte[] partialCopy = new byte[src.remaining() - 1];
            src.get(partialCopy);
            return delegate.write(ByteBuffer.wrap(partialCopy));
        } else {
            return delegate.write(src);
        }
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
