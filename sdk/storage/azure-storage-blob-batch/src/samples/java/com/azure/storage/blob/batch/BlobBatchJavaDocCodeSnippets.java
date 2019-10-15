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
            Response<Void> deleteResponse = batch.deleteBlob("{url of blob}");
            Response<Void> setTierResponse = batch.setBlobAccessTier("{url of another blob}", AccessTier.HOT);
        } catch (UnsupportedOperationException ex) {
            System.err.printf("This will fail as Azure Storage Blob batch operations are homogeneous. Exception: %s%n",
                ex.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatch.illegalBatchOperation
    }

    /**
     * Code snippet for {@link BlobBatch#deleteBlob(String, String)}
     */
    public void addSimpleDeleteOperationWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-String
        Response<Void> deleteResponse = batch.deleteBlob("{container name}", "{blob name}");
        // END: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-String
    }

    /**
     * Code snippet for {@link BlobBatch#deleteBlob(String)}
     */
    public void addSimpleDeleteOperationWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String
        Response<Void> deleteResponse = batch.deleteBlob("{url of blob}");
        // END: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String
    }

    /**
     * Code snippet for {@link BlobBatch#deleteBlob(String, String, DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void addDeleteOperationWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-String-DeleteSnapshotsOptionType-BlobAccessConditions
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("{lease ID}"));

        Response<Void> deleteResponse = batch.deleteBlob("{container name}", "{blob name}",
            DeleteSnapshotsOptionType.INCLUDE, blobAccessConditions);
        // END: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-String-DeleteSnapshotsOptionType-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlobBatch#deleteBlob(String, DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void addDeleteOperationWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-DeleteSnapshotsOptionType-BlobAccessConditions
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("{lease ID}"));

        Response<Void> deleteResponse = batch.deleteBlob("{url of blob}", DeleteSnapshotsOptionType.INCLUDE,
            blobAccessConditions);
        // END: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-DeleteSnapshotsOptionType-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlobBatch#setBlobAccessTier(String, String, AccessTier)}
     */
    public void addSimpleSetTierWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-String-AccessTier
        Response<Void> setTierResponse = batch.setBlobAccessTier("{container name}", "{blob name}", AccessTier.HOT);
        // END: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-String-AccessTier
    }

    /**
     * Code snippet for {@link BlobBatch#setBlobAccessTier(String, AccessTier)}
     */
    public void addSimpleSetTierWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-AccessTier
        Response<Void> setTierResponse = batch.setBlobAccessTier("{url of blob}", AccessTier.HOT);
        // END: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-AccessTier
    }

    /**
     * Code snippet for {@link BlobBatch#setBlobAccessTier(String, String, AccessTier, LeaseAccessConditions)}
     */
    public void addSetTierWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-String-AccessTier-LeaseAccessConditions
        Response<Void> setTierResponse = batch.setBlobAccessTier("{container name}", "{blob name}", AccessTier.HOT,
            new LeaseAccessConditions().setLeaseId("{lease ID}"));
        // END: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-String-AccessTier-LeaseAccessConditions
    }

    /**
     * Code snippet for {@link BlobBatch#setBlobAccessTier(String, AccessTier, LeaseAccessConditions)}
     */
    public void addSetTierWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-AccessTier-LeaseAccessConditions
        Response<Void> setTierResponse = batch.setBlobAccessTier("{url of blob}", AccessTier.HOT,
            new LeaseAccessConditions().setLeaseId("{lease ID}"));
        // END: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-AccessTier-LeaseAccessConditions
    }
}
