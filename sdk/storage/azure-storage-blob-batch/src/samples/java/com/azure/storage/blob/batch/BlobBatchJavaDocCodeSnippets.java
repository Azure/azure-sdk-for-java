// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.batch.options.BlobBatchSetBlobAccessTierOptions;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;

/**
 * Code snippets for {@link BlobBatch}
 */
public class BlobBatchJavaDocCodeSnippets {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_BLOB_ENDPOINT");
    private static final String SASTOKEN = Configuration.getGlobalConfiguration().get("SAS_TOKEN");

    private BlobBatch batch = new BlobBatchClientBuilder(new BlobServiceClientBuilder()
                                                            .endpoint(ENDPOINT)
                                                            .sasToken(SASTOKEN).buildClient())
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
     * Code snippet for {@link BlobBatch#deleteBlob(String, String, DeleteSnapshotsOptionType, BlobRequestConditions)}
     */
    public void addDeleteOperationWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-String-DeleteSnapshotsOptionType-BlobRequestConditions
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId("{lease ID}");

        Response<Void> deleteResponse = batch.deleteBlob("{container name}", "{blob name}",
            DeleteSnapshotsOptionType.INCLUDE, blobRequestConditions);
        // END: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-String-DeleteSnapshotsOptionType-BlobRequestConditions
    }

    /**
     * Code snippet for {@link BlobBatch#deleteBlob(String, DeleteSnapshotsOptionType, BlobRequestConditions)}
     */
    public void addDeleteOperationWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-DeleteSnapshotsOptionType-BlobRequestConditions
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId("{lease ID}");

        Response<Void> deleteResponse = batch.deleteBlob("{url of blob}", DeleteSnapshotsOptionType.INCLUDE,
            blobRequestConditions);
        // END: com.azure.storage.blob.batch.BlobBatch.deleteBlob#String-DeleteSnapshotsOptionType-BlobRequestConditions
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
     * Code snippet for {@link BlobBatch#setBlobAccessTier(String, String, AccessTier, String)}
     */
    public void addSetTierWithNames() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-String-AccessTier-String
        Response<Void> setTierResponse = batch.setBlobAccessTier("{container name}", "{blob name}", AccessTier.HOT,
            "{lease ID}");
        // END: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-String-AccessTier-String
    }

    /**
     * Code snippet for {@link BlobBatch#setBlobAccessTier(String, AccessTier, String)}
     */
    public void addSetTierWithUrl() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-AccessTier-String
        Response<Void> setTierResponse = batch.setBlobAccessTier("{url of blob}", AccessTier.HOT, "{lease ID}");
        // END: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#String-AccessTier-String
    }

    /**
     * Code snippet for {@link BlobBatch#setBlobAccessTier(BlobBatchSetBlobAccessTierOptions)}
     */
    public void addSetTierWithUrlOptions() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#BlobBatchSetBlobAccessTierOptions
        Response<Void> setTierResponse = batch.setBlobAccessTier(
            new BlobBatchSetBlobAccessTierOptions("{url of blob}", AccessTier.HOT).setLeaseId("{lease ID}"));
        // END: com.azure.storage.blob.batch.BlobBatch.setBlobAccessTier#BlobBatchSetBlobAccessTierOptions
    }
}
