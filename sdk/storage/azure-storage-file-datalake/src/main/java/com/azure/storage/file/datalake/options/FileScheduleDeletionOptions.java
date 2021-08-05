// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.FileExpirationOffset;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Parameters for Schedule Deletion.
 */
public class FileScheduleDeletionOptions {
    private final Duration timeToExpire;
    private final FileExpirationOffset expiryRelativeTo;
    private final OffsetDateTime expiresOn;

    /**
     * Creates empty {@link FileScheduleDeletionOptions}.
     * If the file was scheduled for deletion, the deletion will be cancelled.
     */
    public FileScheduleDeletionOptions() {
        this.timeToExpire = null;
        this.expiryRelativeTo = null;
        this.expiresOn = null;
    }

    /**
     * Sets time when the file will be deleted, relative to the file
     * creation time or the current time.
     * @param timeToExpire Duration before file will be deleted.
     * @param expiryRelativeTo Specifies if TimeToExpire should be
     * set relative to the file's creation time, or the current time.
     */
    public FileScheduleDeletionOptions(Duration timeToExpire, FileExpirationOffset expiryRelativeTo) {
        StorageImplUtils.assertNotNull("timeToExpire", timeToExpire);
        StorageImplUtils.assertNotNull("expiryRelativeTo", expiryRelativeTo);
        this.timeToExpire = timeToExpire;
        this.expiryRelativeTo = expiryRelativeTo;
        this.expiresOn = null;
    }

    /**
     * Sets the {@link OffsetDateTime} when the file will be deleted.
     * @param expiresOn The {@link OffsetDateTime} when the file will be deleted.
     */
    public FileScheduleDeletionOptions(OffsetDateTime expiresOn) {
        StorageImplUtils.assertNotNull("expiresOn", expiresOn);
        this.expiresOn = expiresOn;
        this.timeToExpire = null;
        this.expiryRelativeTo = null;
    }

    /**
     * @return Duration before file should be deleted.
     */
    public Duration getTimeToExpire() {
        return timeToExpire;
    }

    /**
     * @return if {@link #getTimeToExpire()} should be
     * set relative to the file's creation time, or the current time.
     */
    public FileExpirationOffset getExpiryRelativeTo() {
        return expiryRelativeTo;
    }

    /**
     * @return The {@link OffsetDateTime} to set for when
     * the file will be deleted.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }
}
