// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

/**
 * Algorithm for generating a checksum to be used for verifying REST contents on a transfer.
 */
public enum StorageChecksumAlgorithm {
    /**
     * Recommended. Allow the library to choose an algorithm. Different library versions may
     * make different choices.
     */
    AUTO(0),

    /**
     * No checksum algorithm is used.
     */
    NONE(1),

    //    /**
    //     * Standard MD5 hash algorithm.
    //     */
    //    MD5(2),

    /**
     * Azure Storage custom 64 bit CRC.
     */
    CRC64(3);

    /**
     * The actual serialized value for a StorageChecksumAlgorithm instance.
     */
    private final int value;

    StorageChecksumAlgorithm(int value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a StorageChecksumAlgorithm instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed StorageChecksumAlgorithm object, or null if unable to parse.
     */
    public static StorageChecksumAlgorithm fromString(String value) {
        if (value == null) {
            return null;
        }
        StorageChecksumAlgorithm[] items = StorageChecksumAlgorithm.values();
        for (StorageChecksumAlgorithm item : items) {
            if (item.getValue() == Integer.parseInt(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Parses a serialized value to a StorageChecksumAlgorithm instance.
     * @param value the serialized value to parse.
     * @return the parsed StorageChecksumAlgorithm object.
     * @throws IllegalArgumentException if unable to parse.
     */
    public static StorageChecksumAlgorithm fromValue(int value) {
        for (StorageChecksumAlgorithm algorithm : StorageChecksumAlgorithm.values()) {
            if (algorithm.getValue() == value) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Invalid value for StorageChecksumAlgorithm: " + value);
    }

    /**
     * Returns the value for a StorageChecksumAlgorithm instance.
     *
     * @return the integer value of the StorageChecksumAlgorithm object.
     */
    public int getValue() {
        return value;
    }

    public StorageChecksumAlgorithm resolveAuto() {
        if (this == AUTO) {
            return StorageChecksumAlgorithm.CRC64;
        }
        return this;
    }
}
