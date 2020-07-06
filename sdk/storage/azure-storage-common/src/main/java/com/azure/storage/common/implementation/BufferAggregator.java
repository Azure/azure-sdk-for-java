// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides ability buffer data chunks that are larger than single {@link ByteBuffer} size.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class BufferAggregator {
    private final long limit;
    private long length = 0;
    private List<ByteBuffer> buffers = new LinkedList<>();

    /**
     * Creates new BufferAggregator instance.
     * @param limit Capacity in number of bytes.
     */
    BufferAggregator(long limit) {
        this.limit = limit;
    }

    /**
     * @return Remaining number of bytes this instance can store.
     */
    long remainingCapacity() {
        return limit - length;
    }

    /**
     * @return Number of bytes this instance already stores.
     */
    public long length() {
        return this.length;
    }

    /**
     * Appends additional ByteBuffer to existing data set.
     *
     * @param byteBuffer A buffer with additional data.
     */
    void append(ByteBuffer byteBuffer) {
        buffers.add(byteBuffer);
        length += byteBuffer.remaining();
    }

    /**
     * Removes data already store by this instance.
     */
    void reset() {
        this.length = 0;
        this.buffers = new LinkedList<>();
    }

    /**
     * Converts accumulated data into {@link Flux} of {@link ByteBuffer}.
     *
     * @return A {@link Flux} of {@link ByteBuffer} of accumulated data.
     */
    public Flux<ByteBuffer> asFlux() {
        return Flux.fromIterable(this.buffers);
    }
}
