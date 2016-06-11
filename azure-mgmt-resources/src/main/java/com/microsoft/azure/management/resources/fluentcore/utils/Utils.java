/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

/**
 * Defines a few utilities.
 */
public final class Utils {
    /**
     * Generate a random ID from a prefix.
     *
     * @param prefix the prefix for the random value
     * @return a random value with the given prefix
     */
    public static String randomId(String prefix) {
        return prefix + String.valueOf(System.currentTimeMillis() % 100000L);
    }

    /**
     * Converts an object Boolean to a primitive boolean.
     *
     * @param value the <tt>Boolean</tt> value
     * @return <tt>false</tt> if the given Boolean value is null or false else <tt>true</tt>
     */
    public static boolean toPrimitiveBoolean(Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

    private Utils() {
    }
}
