// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

final class BufferAggregator {
    private final long limit;
    private long size = 0;
    private List<ByteBuffer> buffers = new LinkedList<>();

    BufferAggregator(long limit) {
        this.limit = limit;
    }

    public long remaining() {
        return limit - size;
    }

    public long size() {
        return this.size;
    }

    public void add(ByteBuffer byteBuffer) {
        buffers.add(byteBuffer);
        size += byteBuffer.remaining();
    }

    public void reset() {
        this.size = 0;
        this.buffers = new LinkedList<>();
    }

    public Flux<ByteBuffer> getBuffers() {
        return Flux.fromIterable(this.buffers);
    }
}
