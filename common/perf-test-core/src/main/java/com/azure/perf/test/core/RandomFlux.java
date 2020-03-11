// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.nio.ByteBuffer;
import java.util.Random;

import reactor.core.publisher.Flux;

/**
 * Represents a Random Flux to be used in performance tests.
 */
public class RandomFlux {
    private static final byte[] RANDOM_BYTES;
    private static final ByteBuffer RANDOM_BYTE_BUFFER;

    static {
        RANDOM_BYTES = new byte[1024 * 1024];
        (new Random(0)).nextBytes(RANDOM_BYTES);
        RANDOM_BYTE_BUFFER = ByteBuffer.wrap(RANDOM_BYTES).asReadOnlyBuffer();
    }

    /**
     * Creates a random flux of specified size.
     * @param size the size of the stream
     * @return the {@link Flux} of {@code size}
     */
    public static Flux<ByteBuffer> create(long size) {
        return CircularFlux.create(RANDOM_BYTE_BUFFER, size);
    }
}
