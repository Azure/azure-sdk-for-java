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
     * @param proposedId A new lease ID in a valid GUID format.
     */
    public BlobChangeLeaseOptions(String proposedId) {
        StorageImplUtils.assertNotNull("proposedId", proposedId);
        this.proposedId = proposedId;
    }

    /**
     * @return A new lease ID in a valid GUID format.
     */
    public String getProposedId() {
        return this.proposedId;
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
    public BlobChangeLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
