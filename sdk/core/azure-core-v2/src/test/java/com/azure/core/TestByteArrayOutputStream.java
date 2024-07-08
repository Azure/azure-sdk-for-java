// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.v2.implementation.AccessibleByteArrayOutputStream;

import java.util.Arrays;

/**
 * Extension of {@link AccessibleByteArrayOutputStream} giving access to internals for better testing performance.
 */
public class TestByteArrayOutputStream extends AccessibleByteArrayOutputStream {
    /**
     * Constructs an instance of {@link TestByteArrayOutputStream} with the default initial capacity.
     */
    public TestByteArrayOutputStream() {
        super();
    }

    /**
     * Constructs an instance of {@link TestByteArrayOutputStream} with a specified initial capacity.
     *
     * @param initialCapacity The initial capacity.
     * @throws IllegalArgumentException If {@code initialCapacity} is less than 0.
     */
    public TestByteArrayOutputStream(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * If the written data count is equal to the internal byte array length the byte array is returned without copying.
     *
     * @return A byte array representing the data written.
     */
    public byte[] toByteArrayUnsafe() {
        return (buf.length == count) ? buf : Arrays.copyOf(buf, count);
    }
}
