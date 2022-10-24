package com.azure.storage.common.implementation;

import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.UploadTransferValidationOptions;

public class ChecksumValue {
    private final byte[] checksum;
    private final StorageChecksumAlgorithm algorithm;

    public ChecksumValue(byte[] checksum, StorageChecksumAlgorithm algorithm) {
        switch (algorithm) {
            case MD5, StorageCrc64 -> {}
            default -> {
                if (checksum != null) {
                    throw new IllegalArgumentException("UploadChecksumValue does not support the given algorithm.");
                }
            }
        }
        this.checksum = checksum;
        this.algorithm = algorithm;
    }

    public ChecksumValue(UploadTransferValidationOptions options) {
        this(options != null ? options.getPrecalculatedChecksum() : null,
            options != null ? options.getChecksumAlgorithm() : StorageChecksumAlgorithm.None);
    }

    /**
     * Gets the checksum regardless of algorithm.
     * @return Bytes of the checksum.
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * Gets the algorithm used to calculate the given checksum.
     * @return Algorithm ID.
     */
    public StorageChecksumAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Gets the MD5 if that is the checksum stored, otherwise null.
     * @return Potential bytes of an MD5 or null.
     */
    public byte[] getMd5() {
        return algorithm == StorageChecksumAlgorithm.MD5
            ? checksum : null;
    }

    /**
     * Gets the CRC64 if that is the checksum stored, otherwise null.
     * @return Potential bytes of a CRC-64 or null.
     */
    public byte[] getCrc64() {
        return algorithm == StorageChecksumAlgorithm.StorageCrc64
            ? checksum : null;
    }
}
