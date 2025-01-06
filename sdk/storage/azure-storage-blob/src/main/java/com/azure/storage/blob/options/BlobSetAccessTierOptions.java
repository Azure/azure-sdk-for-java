// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when setting tier for a blob.
 */
@Fluent
public class BlobSetAccessTierOptions {
    private final AccessTier tier;
    private RehydratePriority priority;
    private String leaseId;
    private String tagsConditions;

    /**
     * Creates a new instance of {@link BlobSetAccessTierOptions}.
     *
     * @param tier The new tier for the blob.
     * @throws NullPointerException If {@code tier} is null.
     */
    public BlobSetAccessTierOptions(AccessTier tier) {
        StorageImplUtils.assertNotNull("tier", tier);
        this.tier = tier;
    }

    /**
     * Gets the new tier for the blob.
     *
     * @return The new tier for the blob.
     */
    public AccessTier getTier() {
        return tier;
    }

    /**
     * Gets the priority to set for re-hydrating blobs.
     *
     * @return Priority to set for re-hydrating blobs.
     */
    public RehydratePriority getPriority() {
        return priority;
    }

    /**
     * Sets the priority to set for re-hydrating blobs.
     *
     * @param priority Priority to set for re-hydrating blobs.
     * @return The updated BlobSetAccessTierOptions.
     */
    public BlobSetAccessTierOptions setPriority(RehydratePriority priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Gets the lease ID the active lease on the blob must match.
     *
     * @return The lease ID the active lease on the blob must match.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets the lease ID the active lease on the blob must match.
     *
     * @param leaseId The lease ID the active lease on the blob must match.
     * @return The updated BlobSetAccessTierOptions.
     */
    public BlobSetAccessTierOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the SQL statement that apply to the tags of the blob.
     *
     * @return The SQL statement that apply to the tags of the blob.
     */
    public String getTagsConditions() {
        return tagsConditions;
    }

    /**
     * Sets the SQL statement that apply to the tags of the blob.
     *
     * @param tagsConditions The SQL statement that apply to the tags of the blob.
     * @return The updated BlobSetAccessTierOptions.
     */
    public BlobSetAccessTierOptions setTagsConditions(String tagsConditions) {
        this.tagsConditions = tagsConditions;
        return this;
    }
}
