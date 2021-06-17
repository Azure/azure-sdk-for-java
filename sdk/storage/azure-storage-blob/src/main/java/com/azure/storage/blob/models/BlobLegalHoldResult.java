// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * The blob legal hold result.
 */
public class BlobLegalHoldResult {

    private final boolean hasLegalHold;

    /**
     * Creates a new BlobLegalHoldResult
     * @param hasLegalHold whether or not a legal hold is enabled on the blob.
     */
    public BlobLegalHoldResult(boolean hasLegalHold) {
        this.hasLegalHold = hasLegalHold;
    }

    /**
     * @return whether or not a legal hold is enabled on the blob.
     */
    public boolean hasLegalHold() {
        return hasLegalHold;
    }
}
