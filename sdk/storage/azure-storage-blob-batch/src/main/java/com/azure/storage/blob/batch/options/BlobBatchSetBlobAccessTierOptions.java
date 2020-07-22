// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when batch setting tier for blobs.
 */
@Fluent
public class BlobBatchSetBlobAccessTierOptions {

    private final String blobUrl;
    private final AccessTier tier;
    private RehydratePriority priority;
    private String leaseId;
    private String tagsConditions;

    /**
     * @param blobUrl Urls of the blobs to set their access tier. Blob names must be encoded to UTF-8.
     * @param tier {@link AccessTier} to set on each blob.
     */
    public BlobBatchSetBlobAccessTierOptions(String blobUrl, AccessTier tier) {
        StorageImplUtils.assertNotNull("blobUrl", blobUrl);
        StorageImplUtils.assertNotNull("tier", tier);
        this.blobUrl = blobUrl;
        this.tier = tier;
    }

    /**
     * @return Url of the blobs to set its access tier.
     */
    public String getBlobUrl() {
        return blobUrl;
    }

    /**
     * @return The new tier for the blobs.
     */
    public AccessTier getTier() {
        return tier;
    }

    /**
     * @return Priority to set for re-hydrating blobs.
     */
    public RehydratePriority getPriority() {
        return priority;
    }

    /**
     * @param priority Priority to set for re-hydrating blobs.
     * @return The updated BlobBatchSetBlobsAccessTierOptions.
     */
    public BlobBatchSetBlobAccessTierOptions setPriority(RehydratePriority priority) {
        this.priority = priority;
        return this;
    }

    /**
     * @return The lease ID the active lease on the blobs must match.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * @param leaseId The lease ID the active lease on the blobs must match.
     * @return The updated BlobBatchSetBlobsAccessTierOptions.
     */
    public BlobBatchSetBlobAccessTierOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * @return The SQL statement that apply to the tags of the blobs.
     */
    public String getTagsConditions() {
        return tagsConditions;
    }

    /**
     * @param tagsConditions The SQL statement that apply to the tags of the blobs.
     * @return The updated BlobBatchSetBlobsAccessTierOptions.
     */
    public BlobBatchSetBlobAccessTierOptions setTagsConditions(String tagsConditions) {
        this.tagsConditions = tagsConditions;
        return this;
    }
}
