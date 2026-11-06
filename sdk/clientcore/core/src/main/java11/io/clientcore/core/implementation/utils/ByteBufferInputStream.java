// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.utils;

import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Buffer-backed stream with bulk operations that avoid intermediate stream buffers.
 */
final class ByteBufferInputStream extends IterableOfByteBuffersInputStream {
    private static final ClientLogger LOGGER = new ClientLogger(ByteBufferInputStream.class);
    private static final int READ_SIZE = 8192;
    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    ByteBufferInputStream(Iterable<ByteBuffer> buffers) {
        super(buffers);
    }

    @Override
    public synchronized byte[] readAllBytes() throws IOException {
        List<ByteBuffer> buffers = new ArrayList<>();
        long length = 0;
        ByteBuffer buffer;
        while ((buffer = takeCurrentBuffer()) != null) {
            length += buffer.remaining();
            if (length > MAX_ARRAY_LENGTH) {
                OutOfMemoryError error = new OutOfMemoryError("Required array size too large");
                LOGGER.atError().setThrowable(error).log(error.getMessage());
                throw error;
            }
            buffers.add(buffer);
        }

        byte[] bytes = new byte[(int) length];
        int offset = 0;
        for (ByteBuffer source : buffers) {
            int count = source.remaining();
            source.get(bytes, offset, count);
            offset += count;
        }
        return bytes;
    }

    @Override
    public long transferTo(OutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "'outputStream' cannot be null.");
        long transferred = 0;
        while (true) {
            ByteBuffer buffer;
            synchronized (this) {
                buffer = getCurrentBuffer();
                if (buffer == null) {
                    return transferred;
                }
                if (buffer.hasArray() && buffer.remaining() >= READ_SIZE) {
                    takeCurrentBuffer();
                } else {
                    buffer = null;
                }
            }

            if (buffer == null) {
                // Let the JDK coalesce small buffers and copy direct buffers in bounded chunks.
                return transferred + super.transferTo(outputStream);
            }

            // Do not hold the stream monitor while writing to a potentially blocking destination.
            int count = buffer.remaining();
            outputStream.write(buffer.array(), buffer.arrayOffset() + buffer.position(), count);
            transferred += count;
        }
    }
}
