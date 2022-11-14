// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.util.CoreUtils;

/**
 * Options for additional content integrity checks on upload.
 */
public class UploadTransferValidationOptions {
    private StorageChecksumAlgorithm checksumAlgorithm = StorageChecksumAlgorithm.None;
    private byte[] precalculatedChecksum;

    /**
     * Creates a new {@link UploadTransferValidationOptions} with default parameters applied.
     */
    public UploadTransferValidationOptions() {
    }

    /**
     * Gets the identifier of the checksum algorithm to use.
     * @return Checksum algorithm ID.
     */
    public StorageChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    /**
     * Sets the identifier of the checksum algorithm to use.
     * @param checksumAlgorithm Checksum algorithm ID.
     * @return The updated options
     */
    public UploadTransferValidationOptions setChecksumAlgorithm(StorageChecksumAlgorithm checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
        return this;
    }

    /**
     * Optional. Can only be specified on specific operations and not at the client level.
     * An existing checksum of the data to be uploaded. Not all upload APIs can use this
     * value, and will throw if one is provided. Please check documentation on specific
     * APIs for whether this can be used.
     * @return the checksum value.
     */
    public byte[] getPrecalculatedChecksum() {
        return CoreUtils.clone(precalculatedChecksum);
    }

    /**
     * Optional. Can only be specified on specific operations and not at the client level.
     * An existing checksum of the data to be uploaded. Not all upload APIs can use this
     * value, and will throw if one is provided. Please check documentation on specific
     * APIs for whether this can be used.
     * @param precalculatedChecksum the checksum value.
     * @return The updated options
     */
    public UploadTransferValidationOptions setPrecalculatedChecksum(byte[] precalculatedChecksum) {
        this.precalculatedChecksum = CoreUtils.clone(precalculatedChecksum);
        return this;
    }
}
