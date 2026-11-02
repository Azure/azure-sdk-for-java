// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.utils;

import java.nio.ByteBuffer;

/**
 * Byte buffer operations with implementations selected by the Java runtime.
 */
public final class ByteBufferUtils {
    private ByteBufferUtils() {
    }

    /**
     * Copies the remaining bytes without changing the source position, limit, or mark.
     *
     * @param buffer The source buffer.
     * @param destination The destination array.
     * @param offset The offset in the destination array.
     * @return The number of bytes copied.
     * @throws NullPointerException If the source buffer or destination array is null.
     * @throws IndexOutOfBoundsException If the destination range cannot hold the remaining bytes.
     */
    public static int copyToArray(ByteBuffer buffer, byte[] destination, int offset) {
        int count = buffer.remaining();
        buffer.duplicate().get(destination, offset, count);
        return count;
    }
}
