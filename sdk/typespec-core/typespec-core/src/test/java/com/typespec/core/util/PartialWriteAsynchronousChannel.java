// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * An AsynchronousByteChannel that simulates incomplete writes, i.e. when not whole buffer is written.
 */
public final class PartialWriteAsynchronousChannel implements AsynchronousByteChannel {

    private final AsynchronousByteChannel delegate;

    public PartialWriteAsynchronousChannel(AsynchronousByteChannel delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (src.remaining() > 1) {
            byte[] partialCopy = new byte[src.remaining() - 1];
            src.get(partialCopy);
            delegate.write(ByteBuffer.wrap(partialCopy), attachment, handler);
        } else {
            delegate.write(src, attachment, handler);
        }
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
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
