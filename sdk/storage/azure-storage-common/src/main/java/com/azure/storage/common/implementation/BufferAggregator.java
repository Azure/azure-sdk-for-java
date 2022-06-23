// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.Iterator;
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

    /**
     * Returns the first n bytes of this aggregator. When asFlux is called later, they will not be returned again. This
     * is generally intended to buffer negligible amounts of data such as the nonce in GMC encryption, which is 12 bytes
     * @return
     */
    public byte[] getFirstNBytes(int numBytes) {
        if (numBytes < 0 || numBytes > this.length) {
            throw new IllegalArgumentException("numBytes is outside the range of this aggregator");
        }
        ByteBuffer data = ByteBuffer.allocate(numBytes);
        Iterator<ByteBuffer> bufferIterator = buffers.iterator();
        while (data.hasRemaining()) {
            // No need to check hasNext as we already guaranteed the aggregator was big enough to fill the request.
            ByteBuffer source = bufferIterator.next();
            if (data.remaining() < source.remaining()) {
                /*
                 * If source is bigger than data, scope source down to a duplicate of appropriate size to avoid
                 * exception. Then advance the original source by the amount read to reflect the change.
                 */
                int readAmount = data.remaining();
                ByteBuffer smallSource = source.duplicate();
                smallSource.limit(source.position() + readAmount);
                data.put(smallSource);
                source.position(source.position() + readAmount);
            } else {
                // If source is smaller than data, just transfer over all of source and move on to the next buffer.
                data.put(source);
            }
        }
        this.length -= data.array().length;
        return data.array(); // No need to flip as we're just going straight to the underlying array
    }
}
