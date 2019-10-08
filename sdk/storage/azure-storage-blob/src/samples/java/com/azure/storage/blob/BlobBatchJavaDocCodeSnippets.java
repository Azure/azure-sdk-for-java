// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;

/**
 * Code snippets for {@link BlobBatch}
 */
public class BlobBatchJavaDocCodeSnippets {
    private BlobBatch batch = new BlobBatch(JavaDocCodeSnippetsHelpers.getBlobServiceClient());

    /**
     * Code snippet for {@link BlobBatch#BlobBatch(BlobServiceClient)}
     */
    public void createFromClient() {
        BlobServiceClient blobServiceClient = JavaDocCodeSnippetsHelpers.getBlobServiceClient();

        // BEGIN: com.azure.storage.blob.BlobBatch.createWithServiceClient
        BlobBatch batch = new BlobBatch(blobServiceClient);
        // END: com.azure.storage.blob.BlobBatch.createWithServiceClient
    }

    /**
     * Code snippet for {@link BlobBatch#BlobBatch(BlobServiceAsyncClient)}
     */
    public void createWithAsyncServiceClient() {
        BlobServiceAsyncClient blobServiceAsyncClient = JavaDocCodeSnippetsHelpers.getBlobServiceAsyncClient();

        // BEGIN: com.azure.storage.blob.BlobBatch.createWithServiceAsyncClient
        BlobBatch batch = new BlobBatch(blobServiceAsyncClient);
        // END: com.azure.storage.blob.BlobBatch.createWithServiceAsyncClient
    }

    /**
     * Code snippet for {@link BlobBatch#delete(String, String)}
     */
    public void addSimpleDeleteOperationWithNames() {
        // BEGIN: com.azure.storage.blob.BlobBatch.delete#String-String
        Response<Void> deleteResponse = batch.delete("container name", "blob name");
        // END: com.azure.storage.blob.BlobBatch.delete#String-String
    }

    /**
     * Code snippet for {@link BlobBatch#delete(String)}
     */
    public void addSimpleDeleteOperationWithUrl() {
        // BEGIN: com.azure.storage.blob.BlobBatch.delete#String
        Response<Void> deleteResponse = batch.delete("url of blob");
        // END: com.azure.storage.blob.BlobBatch.delete#String
    }

    /**
     * Code snippet for {@link BlobBatch#delete(String, String, DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void addDeleteOperationWithNames() {
        // BEGIN: com.azure.storage.blob.BlobBatch.delete#String-String-DeleteSnapshotOptionsType-BlobAccessConditions

        // END: com.azure.storage.blob.BlobBatch.delete#String-String-DeleteSnapshotOptionsType-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlobBatch#delete(String, DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void addDeleteOperationWithUrl() {
        // BEGIN: com.azure.storage.blob.BlobBatch.delete#String-DeleteSnapshotOptionsType-BlobAccessConditions

        // END: com.azure.storage.blob.BlobBatch.delete#String-DeleteSnapshotOptionsType-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobBatch#setTier(String, AccessTier)}, {@link BlobBatch#setTier(String, String,
     * AccessTier)}, {@link BlobBatch#setTier(String, AccessTier, LeaseAccessConditions)}, {@link
     * BlobBatch#setTier(String, String, AccessTier, LeaseAccessConditions)}
     */
    public void addSetTierOperation() {

    }
}
