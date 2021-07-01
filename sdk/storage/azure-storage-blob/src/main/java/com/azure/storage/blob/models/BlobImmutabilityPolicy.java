// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.time.OffsetDateTime;

/**
 * Optional parameters for setting the immutability policy of a blob, blob snapshot or blob version.
 */
public class BlobImmutabilityPolicy {

    private OffsetDateTime expiryTime;
    private BlobImmutabilityPolicyMode policyMode;

    /**
     * @return The time when the immutability policy expires.
     */
    public OffsetDateTime getExpiryTime() {
        return expiryTime;
    }

    /**
     * @param expiryTime The time when the immutability policy expires.
     * @return The updated BlobImmutabilityPolicy
     */
    public BlobImmutabilityPolicy setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * @return The immutability policy mode.
     */
    public BlobImmutabilityPolicyMode getPolicyMode() {
        return policyMode;
    }

    /**
     * @param policyMode The immutability policy mode.
     * @return The updated BlobImmutabilityPolicy
     */
    public BlobImmutabilityPolicy setPolicyMode(BlobImmutabilityPolicyMode policyMode) {
        this.policyMode = policyMode;
        return this;
    }
}
