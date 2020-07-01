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
     * @param duration The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     */
    public BlobAcquireLeaseOptions(int duration) {
        this.duration = duration;
    }

    /**
     * @return The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     */
    public int getDuration() {
        return this.duration;
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
    public BlobAcquireLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

}
