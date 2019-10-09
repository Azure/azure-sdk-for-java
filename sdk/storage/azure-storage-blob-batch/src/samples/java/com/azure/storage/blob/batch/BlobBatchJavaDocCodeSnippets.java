// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;

/**
 * Code snippets for {@link BlobBatch}
 */
public class BlobBatchJavaDocCodeSnippets {
    private BlobBatch batch = new BlobBatchClientBuilder(new BlobServiceClientBuilder().buildClient())
        .buildClient().getBlobBatch();

    /**
     * Code snippet showing an example of an illegal batching operation.
     */
    public void illegalBatchingOperation() {
            // BEGIN: com.azure.storage.blob.batch.BlobBatch.illegalBatchOperation
        try {
            Response<Void> deleteResponse = batch.delete("{url of blob}");
            Response<Void> setTierResponse = batch.setTier("{url of another blob}", AccessTier.HOT);
        } catch (UnsupportedOperationException ex) {
            System.err.printf("This will fail as Azure Storage Blob batch operations are homogeneous. Exception: %s%n",
                ex.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatch.illegalBatchOperation
    }

    /**
     * Code snippet for {@link BlobBatch#delete(String, String)}
     */
    public void addSimpleDeleteOperationWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.delete#String-String
        Response<Void> deleteResponse = batch.delete("{container name}", "{blob name}");
        // END: com.azure.storage.blob.batch.BlobBatch.delete#String-String
    }

    /**
     * Code snippet for {@link BlobBatch#delete(String)}
     */
    public void addSimpleDeleteOperationWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.delete#String
        Response<Void> deleteResponse = batch.delete("{url of blob}");
        // END: com.azure.storage.blob.batch.BlobBatch.delete#String
    }

    /**
     * Code snippet for {@link BlobBatch#delete(String, String, DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void addDeleteOperationWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.delete#String-String-DeleteSnapshotOptionsType-BlobAccessConditions
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("{lease ID}"));

        Response<Void> deleteResponse = batch.delete("{container name}", "{blob name}",
            DeleteSnapshotsOptionType.INCLUDE, blobAccessConditions);
        // END: com.azure.storage.blob.batch.BlobBatch.delete#String-String-DeleteSnapshotOptionsType-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlobBatch#delete(String, DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void addDeleteOperationWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.delete#String-DeleteSnapshotOptionsType-BlobAccessConditions
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("{lease ID}"));

        Response<Void> deleteResponse = batch.delete("{url of blob}", DeleteSnapshotsOptionType.INCLUDE,
            blobAccessConditions);
        // END: com.azure.storage.blob.batch.BlobBatch.delete#String-DeleteSnapshotOptionsType-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlobBatch#setTier(String, String, AccessTier)}
     */
    public void addSimpleSetTierWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setTier#String-String-AccessTier
        Response<Void> setTierResponse = batch.setTier("{container name}", "{blob name}", AccessTier.HOT);
        // END: com.azure.storage.blob.batch.BlobBatch.setTier#String-String-AccessTier
    }

    /**
     * Code snippet for {@link BlobBatch#setTier(String, AccessTier)}
     */
    public void addSimpleSetTierWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setTier#String-AccessTier
        Response<Void> setTierResponse = batch.setTier("{url of blob}", AccessTier.HOT);
        // END: com.azure.storage.blob.batch.BlobBatch.setTier#String-AccessTier
    }

    /**
     * Code snippet for {@link BlobBatch#setTier(String, String, AccessTier, LeaseAccessConditions)}
     */
    public void addSetTierWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setTier#String-String-AccessTier-LeaseAccessConditions
        Response<Void> setTierResponse = batch.setTier("{container name}", "{blob name}", AccessTier.HOT,
            new LeaseAccessConditions().setLeaseId("{lease ID}"));
        // END: com.azure.storage.blob.batch.BlobBatch.setTier#String-String-AccessTier-LeaseAccessConditions
    }

    /**
     * Code snippet for {@link BlobBatch#setTier(String, AccessTier, LeaseAccessConditions)}
     */
    public void addSetTierWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setTier#String-AccessTier-LeaseAccessConditions
        Response<Void> setTierResponse = batch.setTier("{url of blob}", AccessTier.HOT,
            new LeaseAccessConditions().setLeaseId("{lease ID}"));
        // END: com.azure.storage.blob.batch.BlobBatch.setTier#String-AccessTier-LeaseAccessConditions
    }
}
