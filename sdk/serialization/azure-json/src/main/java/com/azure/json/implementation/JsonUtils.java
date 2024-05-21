// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.implementation;

import java.util.Arrays;

/**
 * Utility class containing common methods for JSON operations.
 */
public final class JsonUtils {
    /**
     * Repeats a string the specified number of times.
     * <p>
     * If the {@code str} is null or empty it will be returned as-is without any repetitions attempted. If {@code times}
     * is less than zero an {@link IllegalArgumentException} will be thrown, and if {@code times} is zero an empty
     * string will be returned. If {@code times} is one the original string will be returned.
     *
     * @param c The character to repeat.
     * @param times The number of times to repeat the string.
     * @return The string repeated the specified number of times.
     * @throws IllegalArgumentException If {@code times} is less than zero.
     */
    public static String repeat(char c, int times) {
        if (times < 0) {
            throw new IllegalArgumentException("times cannot be less than zero.");
        }

        if (times == 0) {
            return "";
        }

        if (times == 1) {
            return String.valueOf(c);
        }

        char[] repeatedChars = new char[times];
        Arrays.fill(repeatedChars, c);

        return new String(repeatedChars);
    }
}
