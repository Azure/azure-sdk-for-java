// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Extended access options that may be passed to set when scheduling deletion for a path.
 */
@Fluent
public class DataLakePathScheduleDeletionOptions {

    private OffsetDateTime expiresOn;
    private Duration timeToExpire;

    /**
     * Optional parameters for scheduling the deletion of a path.
     */
    public DataLakePathScheduleDeletionOptions() {
    }

    /**
     * @return the expiry date.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * Sets the expiry date.
     * @param expiresOn sets the expiry date.
     * @return The updated options.
     */
    public DataLakePathScheduleDeletionOptions setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * @return the time to expire.
     */
    public Duration getTimeToExpire() {
        return timeToExpire;
    }

    /**
     * Sets the expiry date.
     * @param expiryTime sets the expiry date.
     * @return The updated options.
     */
    public DataLakePathScheduleDeletionOptions setTimeToExpire(Duration expiryTime) {
        timeToExpire = expiryTime;
        return this;
    }

}
