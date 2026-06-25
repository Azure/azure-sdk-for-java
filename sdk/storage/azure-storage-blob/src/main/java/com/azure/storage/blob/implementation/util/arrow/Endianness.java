// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

/**
 * Values for the Arrow IPC {@code Endianness} enum.
 */
public final class Endianness {
    private Endianness() {
    }

    /** Little-endian byte order. */
    public static final short LITTLE = 0;
    /** Big-endian byte order. */
    public static final short BIG = 1;
}
