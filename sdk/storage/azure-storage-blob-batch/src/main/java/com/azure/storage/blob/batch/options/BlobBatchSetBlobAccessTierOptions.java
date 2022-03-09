// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when batch setting tier for blobs.
 */
@Fluent
public class BlobBatchSetBlobAccessTierOptions {

    private final ClientLogger logger = new ClientLogger(BlobBatchSetBlobAccessTierOptions.class);

    private final BlobUrlParts blobUrlParts;
    private final AccessTier tier;
    private RehydratePriority priority;
    private String leaseId;
    private String tagsConditions;

    /**
     * @param blobUrl Url of the blob to set access tier. Blob names must be encoded to UTF-8.
     * @param tier {@link AccessTier} to set on each blob.
     */
    public BlobBatchSetBlobAccessTierOptions(String blobUrl, AccessTier tier) {
        StorageImplUtils.assertNotNull("blobUrl", blobUrl);
        StorageImplUtils.assertNotNull("tier", tier);
        this.blobUrlParts = BlobUrlParts.parse(blobUrl);
        this.tier = tier;
    }

    /**
     * @param containerName Name of the container to set access tier.
     * @param blobName Name of the blob to set access tier. Blob names must be encoded to UTF-8.
     * @param tier {@link AccessTier} to set on each blob.
     */
    public BlobBatchSetBlobAccessTierOptions(String containerName, String blobName, AccessTier tier) {
        StorageImplUtils.assertNotNull("containerName", containerName);
        StorageImplUtils.assertNotNull("blobName", blobName);
        StorageImplUtils.assertNotNull("tier", tier);
        this.blobUrlParts = BlobUrlParts.parse("https://account.blob.core.windows.net")
            .setContainerName(containerName)
            .setBlobName(blobName);
        this.tier = tier;
    }

    /**
     * @return Url of the blob to set its access tier.
     */
    public String getBlobUrl() {
        return blobUrlParts.toUrl().toString();
    }

    /**
     * @return Container of the blob to set its access tier.
     */
    public String getBlobContainerName() {
        return blobUrlParts.getBlobContainerName();
    }

    /**
     * @return Name of the blob to set its access tier.
     */
    public String getBlobName() {
        return blobUrlParts.getBlobName();
    }

    /**
     * @return Identifier of the blob to set its access tier.
     */
    public String getBlobIdentifier() {
        String basePath = String.format("%s/%s", blobUrlParts.getBlobContainerName(), blobUrlParts.getBlobName());
        String snapshot = blobUrlParts.getSnapshot();
        String versionId = blobUrlParts.getVersionId();
        if (snapshot != null && versionId != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'snapshot' and 'versionId' cannot be used at the same time."));
        }
        if (snapshot != null) {
            basePath = Utility.appendQueryParameter(basePath, "snapshot", snapshot);
        }
        if (versionId != null) {
            basePath = Utility.appendQueryParameter(basePath, "versionid", versionId);
        }

        return basePath;
    }

    /**
     * @return Version id of the blob to set its access tier.
     */
    public String getVersionId() {
        return blobUrlParts.getVersionId();
    }

    /**
     * @param versionId Version id of the blob to set its access tier.
     * @return The updated BlobBatchSetBlobsAccessTierOptions.
     */
    public BlobBatchSetBlobAccessTierOptions setVersionId(String versionId) {
        blobUrlParts.setVersionId(versionId);
        return this;
    }

    /**
     * @return Snapshot of the blob to set its access tier.
     */
    public String getSnapshot() {
        return blobUrlParts.getSnapshot();
    }

    /**
     * @param snapshot Snapshot of the blob to set its access tier.
     * @return The updated BlobBatchSetBlobsAccessTierOptions.
     */
    public BlobBatchSetBlobAccessTierOptions setSnapshot(String snapshot) {
        blobUrlParts.setSnapshot(snapshot);
        return this;
    }

    /**
     * @return The new tier for the blob.
     */
    public AccessTier getTier() {
        return tier;
    }

    /**
     * @return Priority to set for re-hydrating blob.
     */
    public RehydratePriority getPriority() {
        return priority;
    }

    /**
     * @param priority Priority to set for re-hydrating blob.
     * @return The updated BlobBatchSetBlobsAccessTierOptions.
     */
    public BlobBatchSetBlobAccessTierOptions setPriority(RehydratePriority priority) {
        this.priority = priority;
        return this;
    }

    /**
     * @return The lease ID the active lease on the blob must match.
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
     * @return The SQL statement that apply to the tags of the blob.
     */
    public String getTagsConditions() {
        return tagsConditions;
    }

    /**
     * @param tagsConditions The SQL statement that apply to the tags of the blob.
     * @return The updated BlobBatchSetBlobsAccessTierOptions.
     */
    public BlobBatchSetBlobAccessTierOptions setTagsConditions(String tagsConditions) {
        this.tagsConditions = tagsConditions;
        return this;
    }
}
