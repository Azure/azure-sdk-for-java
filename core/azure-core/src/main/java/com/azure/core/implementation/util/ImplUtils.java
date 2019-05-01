// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 *  The util class is a helper class for clone operation.
 */
public final class ImplUtils {

    private ImplUtils() {
        // Exists only to defeat instantiation.
    }

    /**
     * Creates a copy of the source byte array.
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static byte[] clone(byte[] source) {
        if (source == null) {
            return null;
        }
        byte[] copy = new byte[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    /**
     * Creates a copy of the source int array.
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static int[] clone(int[] source) {
        if (source == null) {
            return null;
        }
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    /**
     * Creates a copy of the source array.
     * @param source Array being copied.
     * @param <T> Generic representing the type of the source array.
     * @return A copy of the array or null if source was null.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] clone(T[] source) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(source, source.length);
    }

    /**
     * Checks if the array is null or empty.
     * @param array Array being checked for nullness or emptiness.
     * @return True if the array is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the collection is null or empty.
     * @param collection Collection being checked for nullness or emptiness.
     * @return True if the collection is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if the map is null or empty.
     * @param map Map being checked for nullness or emptiness.
     * @return True if the map is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Map map) {
        return map == null || map.isEmpty();
    }
}
