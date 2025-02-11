// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when changing a lease to a blob or container.
 */
@Fluent
public class BlobChangeLeaseOptions {
    private final String proposedId;
    private BlobLeaseRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link BlobChangeLeaseOptions}.
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @throws NullPointerException If {@code proposedId} is null.
     */
    public BlobChangeLeaseOptions(String proposedId) {
        StorageImplUtils.assertNotNull("proposedId", proposedId);
        this.proposedId = proposedId;
    }

    /**
     * Gets the proposed lease ID.
     *
     * @return A new lease ID in a valid GUID format.
     */
    public String getProposedId() {
        return this.proposedId;
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
    public BlobChangeLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
