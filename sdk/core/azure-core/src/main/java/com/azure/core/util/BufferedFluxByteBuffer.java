// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * A {@code Flux<ByteBuffer>} implementation which buffers the contents of the passed {@code Flux<ByteBuffer>} before
 * emitting them downstream.
 */
public final class BufferedFluxByteBuffer extends Flux<ByteBuffer> {
    private final Flux<ByteBuffer> flux;

    /**
     * Creates a new instance of {@link BufferedFluxByteBuffer}.
     *
     * @param flux The {@code Flux<ByteBuffer>} to buffer.
     */
    public BufferedFluxByteBuffer(Flux<ByteBuffer> flux) {
        this.flux = flux;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ByteBuffer> actual) {
        flux.map(ByteBuffer::duplicate).map(buffer -> {
            ByteBuffer duplicate = ByteBuffer.allocate(buffer.remaining());
            duplicate.put(buffer);
            duplicate.rewind();
            return duplicate;
        }).subscribe(actual);
    }
}
