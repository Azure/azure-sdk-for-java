// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This class offers functionality similar to {@link ByteArrayOutputStream} but instead of consuming byte arrays it
 * consumes ByteBuffers. This class is optimized to reduce the number of memory copies by directly writing a passed
 * ByteBuffers data directly into its backing byte array, this differs from handling for {@link ByteArrayOutputStream}
 * where ByteBuffer data may need to be first copied into a temporary buffer resulting in an extra memory copy.
 */
public final class ByteBufferCollector {
    /*
     * Start with a default size of 1 KB as this is small enough to be performant while covering most small response
     * sizes.
     */
    private static final int DEFAULT_INITIAL_SIZE = 1024;

    private static final String INVALID_INITIAL_SIZE = "'initialSize' cannot be equal to or less than 0.";
    private static final String REQUESTED_BUFFER_INVALID = "Required capacity is greater than Integer.MAX_VALUE.";

    private final ClientLogger logger = new ClientLogger(ByteBufferCollector.class);

    private byte[] buffer;
    private int position;

    /**
     * Constructs a new ByteBufferCollector instance with a default sized backing array.
     */
    public ByteBufferCollector() {
        this(DEFAULT_INITIAL_SIZE);
    }

    /**
     * Constructs a new ByteBufferCollector instance with a specified initial size.
     *
     * @param initialSize The initial size for the backing array.
     * @throws IllegalArgumentException If {@code initialSize} is equal to or less than {@code 0}.
     */
    public ByteBufferCollector(int initialSize) {
        if (initialSize <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(INVALID_INITIAL_SIZE));
        }

        this.buffer = new byte[initialSize];
        this.position = 0;
    }

    /**
     * Writes a ByteBuffers content into the backing array.
     *
     * @param byteBuffer The ByteBuffer to concatenate into the collector.
     * @throws IllegalStateException If the size of the backing array would be larger than {@link Integer#MAX_VALUE}
     * when the passed buffer is written.
     */
    public synchronized void write(ByteBuffer byteBuffer) {
        // Null buffer.
        if (byteBuffer == null) {
            return;
        }

        int remaining = byteBuffer.remaining();

        // Nothing to write.
        if (remaining == 0) {
            return;
        }

        ensureCapacity(remaining);
        byteBuffer.get(buffer, position, remaining);
        position += remaining;
    }

    /**
     * Creates a copy of the backing array resized to the number of bytes written into the collector.
     *
     * @return A copy of the backing array.
     */
    public synchronized byte[] toByteArray() {
        return Arrays.copyOf(buffer, position);
    }

    /*
     * This method ensures that the backing buffer has sufficient space to write the data from the passed ByteBuffer.
     */
    private void ensureCapacity(int byteBufferRemaining) throws OutOfMemoryError {
        int currentCapacity = buffer.length;
        int requiredCapacity = position + byteBufferRemaining;

        /*
         * This validates that adding the current capacity and ByteBuffer remaining doesn't result in an integer
         * overflow response by checking that the result uses the same sign as both of the addition arguments.
         */
        if (((position ^ requiredCapacity) & (byteBufferRemaining ^ requiredCapacity)) < 0) {
            throw logger.logExceptionAsError(new IllegalStateException(REQUESTED_BUFFER_INVALID));
        }

        // Buffer is already large enough to accept the data being written.
        if (currentCapacity >= requiredCapacity) {
            return;
        }

        // Propose a new capacity that is double the size of the current capacity.
        int proposedNewCapacity = currentCapacity << 1;

        // If the proposed capacity is less than the required capacity use the required capacity.
        // Subtraction is used instead of a direct comparison as the bit shift could overflow into a negative int.
        if ((proposedNewCapacity - requiredCapacity) < 0) {
            proposedNewCapacity = requiredCapacity;
        }

        // If the proposed capacity doubling overflowed integer use a slightly smaller size than max value.
        if (proposedNewCapacity < 0) {
            proposedNewCapacity = Integer.MAX_VALUE - 8;
        }

        buffer = Arrays.copyOf(buffer, proposedNewCapacity);
    }
}
