// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;

/**
 * Extended options that may be passed when acquiring a lease to a file or share.
 */
@Fluent
public class ShareAcquireLeaseOptions {
    private int duration;

    /**
     * Creates a new options object with an infinite duration.
     */
    public ShareAcquireLeaseOptions() {
        this.duration = -1;
    }

    /**
     * Gets the duration of the lease between 15 and 60 seconds or -1 for an infinite duration.
     *
     * @return The duration of the lease between 15 and 60 seconds or -1 for an infinite duration.
     */
    public int getDuration() {
        return this.duration;
    }

    /**
     * Sets the duration of the lease between 15 and 60 seconds or -1 for an infinite duration.
     *
     * @param durationInSeconds The duration of the lease between 15 and 60 seconds or -1 for an infinite duration.
     * Note: Share files only support infinite lease.
     * @return The updated options.
     */
    public ShareAcquireLeaseOptions setDuration(int durationInSeconds) {
        this.duration = durationInSeconds;
        return this;
    }
}
