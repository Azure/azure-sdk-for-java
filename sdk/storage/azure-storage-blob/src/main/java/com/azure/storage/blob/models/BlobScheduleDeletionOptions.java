// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Parameters for Schedule Deletion.
 */
public class BlobScheduleDeletionOptions {
    private final Duration timeToExpire;
    private final BlobExpirationOffset expiryRelativeTo;
    private final OffsetDateTime expiresOn;

    /**
     * Creates empty {@link BlobScheduleDeletionOptions}.
     * If the blob was scheduled for deletion, the deletion will be cancelled.
     */
    public BlobScheduleDeletionOptions() {
        this.timeToExpire = null;
        this.expiryRelativeTo = null;
        this.expiresOn = null;
    }

    /**
     * Sets time when the blob will be deleted, relative to the blob
     * creation time or the current time.
     * @param timeToExpire Duration before blob will be deleted.
     * @param expiryRelativeTo Specifies if TimeToExpire should be
     * set relative to the blob's creation time, or the current time.
     */
    public BlobScheduleDeletionOptions(Duration timeToExpire, BlobExpirationOffset expiryRelativeTo) {
        StorageImplUtils.assertNotNull("timeToExpire", timeToExpire);
        StorageImplUtils.assertNotNull("expiryRelativeTo", expiryRelativeTo);
        this.timeToExpire = timeToExpire;
        this.expiryRelativeTo = expiryRelativeTo;
        this.expiresOn = null;
    }

    /**
     * Sets the {@link OffsetDateTime} when the blob will be deleted.
     * @param expiresOn The {@link OffsetDateTime} when the blob will be deleted.
     */
    public BlobScheduleDeletionOptions(OffsetDateTime expiresOn) {
        StorageImplUtils.assertNotNull("expiresOn", expiresOn);
        this.expiresOn = expiresOn;
        this.timeToExpire = null;
        this.expiryRelativeTo = null;
    }

    /**
     * @return Duration before blob should be deleted.
     */
    public Duration getTimeToExpire() {
        return timeToExpire;
    }

    /**
     * @return if {@link #getTimeToExpire()} should be
     * set relative to the blob's creation time, or the current time.
     */
    public BlobExpirationOffset getExpiryRelativeTo() {
        return expiryRelativeTo;
    }

    /**
     * @return The {@link OffsetDateTime} to set for when
     * the blob will be deleted.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }
}
