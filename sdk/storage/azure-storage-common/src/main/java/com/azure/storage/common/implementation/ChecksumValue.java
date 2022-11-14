// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.UploadTransferValidationOptions;

public class ChecksumValue {
    private final byte[] checksum;
    private final StorageChecksumAlgorithm algorithm;

    public ChecksumValue(byte[] checksum, StorageChecksumAlgorithm algorithm) {
        switch (algorithm) {
            case MD5:
            case StorageCrc64:
                break;
            default:
                if (checksum != null) {
                    throw new IllegalArgumentException("UploadChecksumValue does not support the given algorithm.");
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
        return CoreUtils.clone(checksum);
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
            ? getChecksum() : null;
    }

    /**
     * Gets the CRC64 if that is the checksum stored, otherwise null.
     * @return Potential bytes of a CRC-64 or null.
     */
    public byte[] getCrc64() {
        return algorithm == StorageChecksumAlgorithm.StorageCrc64
            ? getChecksum() : null;
    }
}
