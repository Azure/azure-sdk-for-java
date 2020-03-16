// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.nio.ByteBuffer;

import reactor.core.publisher.Flux;

/**
 * Represents a Flux with repeated values.
 */
public class CircularFlux {

    /**
     * Creates a {@link Flux} of {@code size} with repeated values of {@code byteBuffer}.
     *
     * @param byteBuffer the byteBuffer to create Flux from.
     * @param size the size of the flux to create.
     * @return The created {@link Flux}
     */
    @SuppressWarnings("cast")
    public static Flux<ByteBuffer> create(ByteBuffer byteBuffer, long size) {
        int remaining = byteBuffer.remaining();
        
        int quotient = (int) size / remaining;
        int remainder = (int) size % remaining;

        return Flux.range(0, quotient)
            .map(i -> byteBuffer.duplicate())
            .concatWithValues((ByteBuffer) byteBuffer.duplicate().limit(remainder));
    }
}
