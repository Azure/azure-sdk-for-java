// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.util;

import com.generic.core.http.Response;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.implementation.http.SimpleResponse;
import com.generic.core.models.Headers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class contains utility methods useful for building client libraries.
 */
public final class CoreUtils {
    // CoreUtils is a commonly used utility, use a static logger.
    private CoreUtils() {
        // Exists only to defeat instantiation.
    }

    /**
     * Checks if the array is null or empty.
     *
     * @param array Array being checked for nullness or emptiness.
     *
     * @return True if the array is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the collection is null or empty.
     *
     * @param collection Collection being checked for nullness or emptiness.
     *
     * @return True if the collection is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if the map is null or empty.
     *
     * @param map Map being checked for nullness or emptiness.
     *
     * @return True if the map is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if the character sequence is null or empty.
     *
     * @param charSequence Character sequence being checked for nullness or emptiness.
     *
     * @return True if the character sequence is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * Optimized version of {@link String#join(CharSequence, Iterable)} when the {@code values} has a small set of
     * object.
     *
     * @param delimiter Delimiter between the values.
     * @param values The values to join.
     *
     * @return The {@code values} joined delimited by the {@code delimiter}.
     *
     * @throws NullPointerException If {@code delimiter} or {@code values} is null.
     */
    public static String stringJoin(String delimiter, List<String> values) {
        Objects.requireNonNull(delimiter, "'delimiter' cannot be null.");
        Objects.requireNonNull(values, "'values' cannot be null.");

        int count = values.size();

        switch (count) {
            case 0:
                return "";
            case 1:
                return values.get(0);
            case 2:
                return values.get(0) + delimiter + values.get(1);
            case 3:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2);
            case 4:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3);
            case 5:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4);
            case 6:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5);
            case 7:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6);
            case 8:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6)
                    + delimiter + values.get(7);
            case 9:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6)
                    + delimiter + values.get(7) + delimiter + values.get(8);
            case 10:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6)
                    + delimiter + values.get(7) + delimiter + values.get(8) + delimiter + values.get(9);
            default:
                return String.join(delimiter, values);
        }
    }

    /**
     * Attempts to convert a byte stream into the properly encoded String.
     * <p>
     * This utility method will attempt to find the encoding for the String in this order.
     * <ol>
     *     <li>Find the byte order mark in the byte array.</li>
     *     <li>Find the {@code charset} in the {@code Content-Type} header.</li>
     *     <li>Default to {@code UTF-8}.</li>
     * </ol>
     *
     * @param bytes Byte array.
     * @param contentType {@code Content-Type} header value.
     * @return A string representation of the byte array encoded to the found encoding.
     */
    public static String bomAwareToString(byte[] bytes, String contentType) {
        if (bytes == null) {
            return null;
        }

        return ImplUtils.bomAwareToString(bytes, 0, bytes.length, contentType);
    }
}
