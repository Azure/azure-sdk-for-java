// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

/**
 * Utility class for computing CRC64 checksums.
 */
public final class StorageCrc64Calculator {
    // CRC64 table for ECMA-182 polynomial 0xC96C5795D7870F42
    private static final long[] CRC64_TABLE = new long[256];

    static {
        long poly = 0xC96C5795D7870F42L;
        for (int i = 0; i < 256; i++) {
            long crc = i;
            for (int j = 0; j < 8; j++) {
                crc = (crc & 1) != 0 ? (crc >>> 1) ^ poly : crc >>> 1;
            }
            CRC64_TABLE[i] = crc;
        }
    }

    /**
     * Computes the CRC64 checksum for the given data with an initial CRC value.
     *
     * @param data the data to compute the checksum for
     * @param initialCrc the initial CRC value
     * @return the computed CRC64 checksum
     */
    public static long compute(byte[] data, long initialCrc) {
        long crc = initialCrc;
        for (byte b : data) {
            crc = CRC64_TABLE[(int) ((crc ^ b) & 0xFF)] ^ (crc >>> 8);
        }
        return crc;
    }

    /**
     * Computes the CRC64 checksum for the given data.
     *
     * @param data the data to compute the checksum for
     * @return the computed CRC64 checksum
     */
    public static long compute(byte[] data) {
        return compute(data, 0);
    }

    private StorageCrc64Calculator() {
        // utility class
    }
}
