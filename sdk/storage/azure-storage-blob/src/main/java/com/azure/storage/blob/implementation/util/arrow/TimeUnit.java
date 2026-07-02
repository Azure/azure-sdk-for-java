// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

/**
 * Values for the Arrow IPC {@code TimeUnit} enum.
 */
public final class TimeUnit {
    private TimeUnit() {
    }

    /** Second resolution. */
    public static final short SECOND = 0;
    /** Millisecond resolution. */
    public static final short MILLISECOND = 1;
    /** Microsecond resolution. */
    public static final short MICROSECOND = 2;
    /** Nanosecond resolution. */
    public static final short NANOSECOND = 3;

    private static final String[] NAMES = { "SECOND", "MILLISECOND", "MICROSECOND", "NANOSECOND" };

    /**
     * Gets the canonical Arrow name for a {@code TimeUnit} value, for diagnostic messages.
     *
     * @param e the time unit value.
     * @return the canonical Arrow time unit name.
     */
    public static String name(int e) {
        return NAMES[e];
    }
}
