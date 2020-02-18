// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

/**
 * Represents a Random Stream to be used in performance tests.
 */
public class RandomStream {
    private static final int SIZE = (1024 * 1024 * 1024) + 1;
    private static final byte[] RANDOM_BYTES;

    static {
        // _randomBytes = new byte[1024 * 1024];
        RANDOM_BYTES = new byte[SIZE];
        (new Random(0)).nextBytes(RANDOM_BYTES);
    }

    /**
     * Creates a random stream of specified size.
     * @param size the size of the stream
     *
     * @throws IllegalArgumentException if {@code size} is more than {@link RandomStream#SIZE}
     * @return the {@link InputStream} of {@code size}
     */
    public static InputStream create(long size) {
        if (size > RandomStream.SIZE) {
            throw new IllegalArgumentException("size must be <= " + RandomStream.SIZE);
        }

        // Workaround for Azure/azure-sdk-for-java#6020
        // return CircularStream.create(_randomBytes, size);
        return new ByteArrayInputStream(RANDOM_BYTES, 0, (int) size);
    }
}
