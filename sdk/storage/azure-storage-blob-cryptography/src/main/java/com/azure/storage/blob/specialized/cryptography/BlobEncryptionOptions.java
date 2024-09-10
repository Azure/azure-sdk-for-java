// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options for configuring encryption on a blob.
 */
public class BlobEncryptionOptions {
    private long authenticatedRegionDataLength;

    /**
     * Creates a new instance of BlobEncryptionOptions.
     */
    public BlobEncryptionOptions() {
    }

    /**
     * Gets the length of the authenticated region data.
     * @return the length of the authenticated region data.
     */
    public long getAuthenticatedRegionDataLength() {
        return authenticatedRegionDataLength;
    }

    /**
     * Creates a new instance of BlobEncryptionOptions.
     * @param authenticatedRegionDataLength The length of the authenticated region data used for uploaded chunking.
     * Minimum value for the length is 128 bytes, and maximum is 1GB.
     *
     * @return the updated BlobEncryptionOptions object.
     */
    public BlobEncryptionOptions setAuthenticatedRegionDataLength(long authenticatedRegionDataLength) {
        StorageImplUtils.assertInBounds("authenticatedRegionDataLength", authenticatedRegionDataLength, 16, Constants.GB);
        this.authenticatedRegionDataLength = authenticatedRegionDataLength;
        return this;
    }
}
