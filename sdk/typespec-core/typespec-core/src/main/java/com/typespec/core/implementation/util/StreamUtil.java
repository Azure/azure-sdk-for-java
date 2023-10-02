// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.util;

import com.typespec.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Utility type exposing methods to deal with streams.
 */
public final class StreamUtil {

    private static final ClientLogger LOGGER = new ClientLogger(StreamUtil.class);

    private StreamUtil() {
    }

    /**
     * Reads input stream into list of byte buffers.
     * If length hint is not provided it starts with {@code initialBufferSize} and increases
     * buffer size until hits {@code maxBufferSize}
     * or end of stream. This is to prevent over allocation for small streams.
     * On the other hand growing buffer becomes less of a problem for bigger streams.
     * If length hint is provided it starts with length as a buffer size or {@code maxBufferSize} whichever is smaller.
     *
     * <p>
     * This method does not check if provided length hint matches size of the stream.
     * </p>
     *
     * @param inputStream The source stream.
     * @param lengthHint Optional hint of the length of stream.
     * @param initialBufferSize The initial buffer size. Used if {@code length} is null.
     * @param maxBufferSize The maximum buffer size.
     * @return List of byte buffers.
     * @throws IOException If IO operation fails.
     */
    public static List<ByteBuffer> readStreamToListOfByteBuffers(
        InputStream inputStream, Long lengthHint,
        int initialBufferSize, int maxBufferSize) throws IOException {
        Objects.requireNonNull(inputStream, "'inputStream' must not be null");
        if (initialBufferSize <= 0) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'initialBufferSize' must be positive integer"));
        }
        if (maxBufferSize < initialBufferSize) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'maxBufferSize' must not be smaller than 'maxBufferSize'"));
        }
        if (lengthHint != null && lengthHint < 0) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'length' must not be negative"));
        }

        // Start small.
        int chunkSize = initialBufferSize;
        // If length is known use it to allocate larger buffer eagerly.
        if (lengthHint != null) {
            chunkSize = (int) Math.max(1, Math.min(maxBufferSize, lengthHint));
        }

        int read;
        long totalRead = 0;
        long actualLength = lengthHint != null ? lengthHint : Long.MAX_VALUE; // assume infinity for unknown length.


        ReadableByteChannel channel = Channels.newChannel(inputStream);
        List<ByteBuffer> buffers = new LinkedList<>();
        ByteBuffer chunk = ByteBuffer.allocate(chunkSize);
        do {
            read = channel.read(chunk);
            if (read >= 0) {
                totalRead += read;

                if (!chunk.hasRemaining()) {
                    // Keep doubling the chunk until we hit max or known length.
                    // This is to not over allocate for small streams eagerly.
                    int nextChunkSizeCandidate = 2 * chunkSize;
                    if (nextChunkSizeCandidate <= actualLength - totalRead
                        && nextChunkSizeCandidate <= maxBufferSize) {
                        chunkSize = nextChunkSizeCandidate;
                    }

                    chunk.flip();
                    buffers.add(chunk);

                    if (totalRead == actualLength) {
                        // if we hit user provided length, check for EOF to avoid extra allocation.
                        ByteBuffer eofCheckBuffer = ByteBuffer.allocate(1);
                        read = channel.read(eofCheckBuffer);
                        if (read != -1) {
                            chunk = ByteBuffer.allocate(chunkSize);
                            eofCheckBuffer.flip();
                            chunk.put(eofCheckBuffer);
                        } else {
                            // if we hit eof null the buffer.
                            chunk = null;
                        }
                    } else {
                        chunk = ByteBuffer.allocate(chunkSize);
                    }
                }
            } else {
                chunk.flip();
                if (chunk.hasRemaining()) {
                    buffers.add(chunk);
                }
            }
        } while (read >= 0);

        return Collections.unmodifiableList(buffers);
    }
}
