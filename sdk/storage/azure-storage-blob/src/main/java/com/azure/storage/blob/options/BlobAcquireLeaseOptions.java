// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;

/**
 * Extended options that may be passed when acquiring a lease to a blob or container.
 */
@Fluent
public class BlobAcquireLeaseOptions {
    private final int duration;
    private BlobLeaseRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link BlobAcquireLeaseOptions}.
     *
     * @param durationInSeconds The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     */
    public BlobAcquireLeaseOptions(int durationInSeconds) {
        this.duration = durationInSeconds;
    }

    /**
     * Gets the duration of the lease.
     *
     * @return The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     */
    public int getDuration() {
        return this.duration;
    }

    /**
     * Gets the {@link BlobLeaseRequestConditions}.
     *
     * @return {@link BlobLeaseRequestConditions}
     */
    public BlobLeaseRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link BlobLeaseRequestConditions}.
     *
     * @param requestConditions {@link BlobLeaseRequestConditions}
     * @return The updated options.
     */
    public BlobAcquireLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
