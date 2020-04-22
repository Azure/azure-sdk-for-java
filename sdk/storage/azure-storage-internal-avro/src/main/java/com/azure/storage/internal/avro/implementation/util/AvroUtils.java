// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.util;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility methods for Avro.
 */
public class AvroUtils {

    /**
     * Converts a List of ByteBuffers into a byte array.
     * @param bytes The buffers to convert.
     * @return The byte array.
     */
    public static byte[] getBytes(List<?> bytes) {
        long longTotalBytes = bytes
            .stream()
            .mapToLong(buffer -> {
                checkByteBuffer("'buffer'", buffer);
                return ((ByteBuffer) buffer).remaining(); })
            .sum();

        if (longTotalBytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Bytes can not fit into a single array.");
        }

        int totalBytes = Math.toIntExact(longTotalBytes);

        byte[] ret = new byte[totalBytes];
        AtomicInteger offset = new AtomicInteger();
        bytes.forEach(buffer -> {
            checkByteBuffer("'buffer'", buffer);
            ByteBuffer b = (ByteBuffer) buffer;
            int length = b.remaining();
            ((ByteBuffer) buffer).get(ret, 0, length);
            offset.addAndGet(length);
        });

        return ret;
    }

    /**
     * Checks whether an Object is of type Long.
     * @param name The name of the variable
     * @param variable The variable.
     */
    public static void checkLong(String name, Object variable) {
        if (!(variable instanceof Long)) {
            throw new IllegalStateException(String.format("Expected %s to be of type %s", name,
                Long.class.getSimpleName()));
        }
    }

    /**
     * Checks whether an Object is of type Integer.
     * @param name The name of the variable
     * @param variable The variable.
     */
    public static void checkInteger(String name, Object variable) {
        if (!(variable instanceof Integer)) {
            throw new IllegalStateException(String.format("Expected %s to be of type %s", name,
                Integer.class.getSimpleName()));
        }
    }

    /**
     * Checks whether an Object is of type String.
     * @param name The name of the variable
     * @param variable The variable.
     */
    public static void checkString(String name, Object variable) {
        if (!(variable instanceof String)) {
            throw new IllegalStateException(String.format("Expected %s to be of type %s", name,
                String.class.getSimpleName()));
        }
    }

    /**
     * Checks whether an Object is of type List.
     * @param name The name of the variable
     * @param variable The variable.
     */
    public static void checkList(String name, Object variable) {
        if (!(variable instanceof List<?>)) {
            throw new IllegalStateException(String.format("Expected %s to be of type %s", name,
                List.class.getSimpleName()));
        }
    }

    /**
     * Checks whether an Object is of type Map.
     * @param name The name of the variable
     * @param variable The variable.
     */
    public static void checkMap(String name, Object variable) {
        if (!(variable instanceof Map<?, ?>)) {
            throw new IllegalStateException(String.format("Expected %s to be of type %s", name,
                Map.class.getSimpleName()));
        }
    }

    /**
     * Checks whether an Object is of type ByteBuffer.
     * @param name The name of the variable
     * @param variable The variable.
     */
    private static void checkByteBuffer(String name, Object variable) {
        if (!(variable instanceof ByteBuffer)) {
            throw new IllegalStateException(String.format("Expected %s to be of type %s", name,
                ByteBuffer.class.getSimpleName()));
        }
    }
}
