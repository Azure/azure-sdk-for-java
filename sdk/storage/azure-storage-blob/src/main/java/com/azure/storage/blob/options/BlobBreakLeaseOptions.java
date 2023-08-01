// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;

import java.time.Duration;

/**
 * Extended options that may be passed when breaking a lease to a blob or container.
 */
@Fluent
public class BlobBreakLeaseOptions {

    private Duration breakPeriod;
    private BlobLeaseRequestConditions requestConditions;

    /**
     * @return An optional duration, between 0 and 60 seconds, that the lease should continue before
     * it is broken. If the break period is longer than the time remaining on the lease the remaining time on the lease
     * is used. A new lease will not be available before the break period has expired, but the lease may be held for
     * longer than the break period.
     */
    public Duration getBreakPeriod() {
        return this.breakPeriod;
    }

    /**
     * @param breakPeriod An optional duration, between 0 and 60 seconds, that the lease should continue before
     * it is broken. If the break period is longer than the time remaining on the lease the remaining time on the lease
     * is used. A new lease will not be available before the break period has expired, but the lease may be held for
     * longer than the break period.
     * @return The updated options.
     */
    public BlobBreakLeaseOptions setBreakPeriod(Duration breakPeriod) {
        this.breakPeriod = breakPeriod;
        return this;
    }

    /**
     * @return {@link BlobLeaseRequestConditions}
     */
    public BlobLeaseRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link BlobLeaseRequestConditions}
     * @return The updated options.
     */
    public BlobBreakLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
