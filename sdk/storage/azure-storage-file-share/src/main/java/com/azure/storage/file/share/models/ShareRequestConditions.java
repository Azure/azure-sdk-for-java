// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Fluent;

/**
 * This class contains values which will restrict the successful operation of a variety of requests to the conditions
 * present. These conditions are entirely optional. The entire object or any of its properties may be set to null when
 * passed to a method to indicate that those conditions are not desired. Please refer to the type of each field for more
 * information on those particular access conditions.
 */
@Fluent
public class ShareRequestConditions {
    private String leaseId;

    /**
     * Creates a new instance of {@link ShareRequestConditions}.
     */
    public ShareRequestConditions() {
    }

    /**
     * Gets the lease ID that files and shares must match.
     *
     * @return The lease ID that files and shares must match.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Optionally limits requests to files and shares that match the lease ID.
     *
     * @param leaseId Lease ID that files and shares must match.
     * @return The updated ShareRequestConditions object.
     */
    public ShareRequestConditions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }
}
