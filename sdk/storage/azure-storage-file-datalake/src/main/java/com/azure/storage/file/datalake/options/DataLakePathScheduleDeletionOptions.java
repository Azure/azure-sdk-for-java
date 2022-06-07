// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Extended access options that may be passed to set when scheduling deletion for a path.
 * Note: can only set either ExpiresOn value or TimeToExpire value, cannot set both.
 */
@Fluent
public class DataLakePathScheduleDeletionOptions {

    private final OffsetDateTime expiresOn;
    private final Duration timeToExpire;

    /**
     * Optional parameters for scheduling the deletion of a path.
     * @param expiresOn {@link OffsetDateTime}to set for when the file will be deleted.  If null, the existing
     * expiresOn time on the file will be removed, if it exists.
     * Note: Does not apply to directories.
     */
    public DataLakePathScheduleDeletionOptions(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        timeToExpire = null;
    }

    /**
     * Optional parameters for scheduling the deletion of a path.
     * @param timeToExpire Duration before file should be deleted.
     * Note: Does not apply to directories.
     */
    public DataLakePathScheduleDeletionOptions(Duration timeToExpire) {
        this.timeToExpire = timeToExpire;
        expiresOn = null;
    }

    /**
     * @return the expiry date.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * @return the time to expire.
     */
    public Duration getTimeToExpire() {
        return timeToExpire;
    }

}
