// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.logging.ClientLogger;

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
    // Bulk reads retain views of buffers whose content outweighs a view's overhead, and of a bounded number of smaller
    // buffers. Other small buffers are copied into READ_SIZE chunks.
    private static final int MIN_VIEW_SIZE = 1024;
    private static final int SMALL_VIEW_LIMIT = 128;
    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    ByteBufferInputStream(Iterable<ByteBuffer> buffers) {
        super(buffers);
    }

    @Override
    public synchronized byte[] readAllBytes() throws IOException {
        // Retaining a view of every buffer could use far more memory than many tiny buffers contain.
        List<ByteBuffer> segments = new ArrayList<>();
        int smallViews = 0;
        byte[] chunk = null;
        int chunkStart = 0;
        int chunkEnd = 0;
        long length = 0;
        ByteBuffer buffer;
        while ((buffer = takeCurrentBuffer()) != null) {
            int remaining = buffer.remaining();
            length += remaining;
            if (length > MAX_ARRAY_LENGTH) {
                throw LOGGER.logThrowableAsError(new OutOfMemoryError("Required array size too large"));
            }

            if (remaining >= MIN_VIEW_SIZE || smallViews < SMALL_VIEW_LIMIT) {
                if (remaining < MIN_VIEW_SIZE) {
                    smallViews++;
                }
                addSegment(segments, chunk, chunkStart, chunkEnd);
                chunkStart = chunkEnd;
                segments.add(buffer);
                continue;
            }

            while (buffer.hasRemaining()) {
                if (chunk == null || chunkEnd == READ_SIZE) {
                    addSegment(segments, chunk, chunkStart, chunkEnd);
                    chunk = new byte[READ_SIZE];
                    chunkStart = 0;
                    chunkEnd = 0;
                }
                int count = Math.min(buffer.remaining(), READ_SIZE - chunkEnd);
                buffer.get(chunk, chunkEnd, count);
                chunkEnd += count;
            }
        }
        addSegment(segments, chunk, chunkStart, chunkEnd);

        byte[] bytes = new byte[(int) length];
        int offset = 0;
        for (ByteBuffer segment : segments) {
            int count = segment.remaining();
            segment.get(bytes, offset, count);
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
            int offset = buffer.arrayOffset() + buffer.position();
            int remaining = buffer.remaining();
            while (remaining > 0) {
                int count = Math.min(remaining, READ_SIZE);
                outputStream.write(buffer.array(), offset, count);
                offset += count;
                remaining -= count;
                transferred += count;
            }
        }
    }

    private static void addSegment(List<ByteBuffer> segments, byte[] chunk, int start, int end) {
        if (end > start) {
            segments.add(ByteBuffer.wrap(chunk, start, end - start));
        }
    }
}
