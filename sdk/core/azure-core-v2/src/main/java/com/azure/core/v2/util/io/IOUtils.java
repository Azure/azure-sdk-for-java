// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.io;

import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.util.ClientLogger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Utilities related to IO operations that involve channels, streams, byte transfers.
 */
public final class IOUtils {

    private static final ClientLogger LOGGER = new ClientLogger(IOUtils.class);
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int SIXTY_FOUR_KB = 64 * 1024;
    private static final int THIRTY_TWO_KB = 32 * 1024;
    private static final int MB = 1024 * 1024;
    private static final int GB = 1024 * MB;

    /**
     * Transfers bytes from {@link ReadableByteChannel} to {@link WritableByteChannel}.
     *
     * @param source A source {@link ReadableByteChannel}.
     * @param destination A destination {@link WritableByteChannel}.
     * @throws IOException When I/O operation fails.
     * @throws NullPointerException When {@code source} or {@code destination} is null.
     */
    public static void transfer(ReadableByteChannel source, WritableByteChannel destination) throws IOException {
        transfer(source, destination, null);
    }

    /**
     * Transfers bytes from {@link ReadableByteChannel} to {@link WritableByteChannel}.
     *
     * @param source A source {@link ReadableByteChannel}.
     * @param destination A destination {@link WritableByteChannel}.
     * @param estimatedSourceSize An estimated size of the source channel, may be null. Used to better determine the
     * size of the buffer used to transfer data in an attempt to reduce read and write calls.
     * @throws IOException When I/O operation fails.
     * @throws NullPointerException When {@code source} or {@code destination} is null.
     */
    public static void transfer(ReadableByteChannel source, WritableByteChannel destination, Long estimatedSourceSize)
        throws IOException {
        if (source == null && destination == null) {
            throw new NullPointerException("'source' and 'destination' cannot be null.");
        } else if (source == null) {
            throw new NullPointerException("'source' cannot be null.");
        } else if (destination == null) {
            throw new NullPointerException("'destination' cannot be null.");
        }

        int bufferSize = (estimatedSourceSize == null) ? getBufferSize(source) : getBufferSize(estimatedSourceSize);
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        int read;
        do {
            buffer.clear();
            read = source.read(buffer);
            buffer.flip();
            ImplUtils.fullyWriteBuffer(buffer, destination);
        } while (read >= 0);
    }

    /*
     * Helper method to optimize the size of the read buffer to reduce the number of reads and writes that have to be
     * performed. If the source and/or target is IO-based, file, network connection, etc, this reduces the number of
     * calls that may have to be handled by the system.
     */
    private static int getBufferSize(ReadableByteChannel source) {
        if (!(source instanceof SeekableByteChannel)) {
            return DEFAULT_BUFFER_SIZE;
        }

        SeekableByteChannel seekableSource = (SeekableByteChannel) source;
        try {
            long size = seekableSource.size();
            long position = seekableSource.position();

            return getBufferSize(size - position);
        } catch (IOException ex) {
            // Don't let an IOException prevent transfer when we are only trying to gain information.
            return DEFAULT_BUFFER_SIZE;
        }
    }

    private static int getBufferSize(long dataSize) {
        if (dataSize > GB) {
            return SIXTY_FOUR_KB;
        } else if (dataSize > MB) {
            return THIRTY_TWO_KB;
        } else {
            return DEFAULT_BUFFER_SIZE;
        }
    }

    private IOUtils() {
    }
}
