package com.azure.storage.common;

/**
 * Algorithm for generating a checksum to be used for verifying REST contents on a transfer.
 */
public enum StorageChecksumAlgorithm {
    /**
     * Recommended. Allow the library to choose an algorithm. Different library versions may make different choices.
     */
    Auto,

    /**
     * No selected algorithm. Do not calculate or request checksums.
     */
    None,

    /**
     * Standard MD5 hash algorithm.
     */
    MD5,

    /**
     * Azure Storage custom 64 bit CRC.
     */
    StorageCrc64,
}
