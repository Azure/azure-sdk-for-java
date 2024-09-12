// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.GCM_ENCRYPTION_REGION_LENGTH;

/**
 * This class contains the configuration options used to encrypt the blob content on the client side.
 */
public class BlobClientSideEncryptionOptions {
    // default value for authenticatedRegionDataLength is 4MB
    private long authenticatedRegionDataLength = GCM_ENCRYPTION_REGION_LENGTH;

    /**
     * Gets the length of the authenticated region data.
     * @return the length of the authenticated region data.
     */
    public long getAuthenticatedRegionDataLength() {
        return authenticatedRegionDataLength;
    }

    /**
     * Sets the authenticated region length to use when encrypting blobs.
     *
     * @param authenticatedRegionDataLength authenticatedRegionDataLength The authenticated region length in bytes to
     * use when encrypting the blob. This value only is used when uploading blobs. Downloads use the authenticated
     * region data length from the blob encryption metadata when decrypting blobs. Minimum value for the length is 16
     * bytes, and maximum is 1GB.
     *
     * @return the updated BlobEncryptionOptions object.
     * @throws IllegalArgumentException If {@code authenticatedRegionDataLength} is less than 16 or greater than 1GB.
     */
    public BlobClientSideEncryptionOptions setAuthenticatedRegionDataLength(long authenticatedRegionDataLength) {
        // We can increase the upper bound limit to 2^39 - 256 bits per GC specification, we are constraining it to 1GB
        // because the underlying encryption/decryption implementations do not full support byte lengths longer than MAX_INT
        StorageImplUtils.assertInBounds("authenticatedRegionDataLength", authenticatedRegionDataLength, 16, Constants.GB);
        this.authenticatedRegionDataLength = authenticatedRegionDataLength;
        return this;
    }
}
