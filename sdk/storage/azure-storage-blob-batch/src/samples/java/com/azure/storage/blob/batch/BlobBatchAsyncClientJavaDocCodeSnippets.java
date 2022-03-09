// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Code snippets for {@link BlobBatchAsyncClient}
 */
public class BlobBatchAsyncClientJavaDocCodeSnippets {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_BLOB_ENDPOINT");
    private static final String SASTOKEN = Configuration.getGlobalConfiguration().get("SAS_TOKEN");

    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                                                    .endpoint(ENDPOINT)
                                                    .sasToken(SASTOKEN)
                                                    .buildClient();
    
    private BlobBatchAsyncClient batchAsyncClient = new BlobBatchClientBuilder(blobServiceClient).buildAsyncClient();

    /**
     * Code snippet for {@link BlobBatchAsyncClient#submitBatch(BlobBatch)}
     */
    public void submitBatch() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatchAsyncClient.submitBatch#BlobBatch
        BlobBatch batch = batchAsyncClient.getBlobBatch();

        Response<Void> deleteResponse1 = batch.deleteBlob("container", "blob1");
        Response<Void> deleteResponse2 = batch.deleteBlob("container", "blob2", DeleteSnapshotsOptionType.INCLUDE,
            new BlobRequestConditions().setLeaseId("leaseId"));

        batchAsyncClient.submitBatch(batch).subscribe(response -> {
            System.out.println("Batch submission completed successfully.");
            System.out.printf("Delete operation 1 completed with status code: %d%n", deleteResponse1.getStatusCode());
            System.out.printf("Delete operation 2 completed with status code: %d%n", deleteResponse2.getStatusCode());
        }, error -> System.err.printf("Batch submission failed. Error message: %s%n", error.getMessage()));
        // END: com.azure.storage.blob.batch.BlobBatchAsyncClient.submitBatch#BlobBatch
    }

    /**
     * Code snippet for {@link BlobBatchAsyncClient#submitBatchWithResponse(BlobBatch, boolean)}
     */
    public void submitBatchWithResponse() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatchAsyncClient.submitBatch#BlobBatch-boolean
        BlobBatch batch = batchAsyncClient.getBlobBatch();

        Response<Void> deleteResponse1 = batch.deleteBlob("container", "blob1");
        Response<Void> deleteResponse2 = batch.deleteBlob("container", "blob2", DeleteSnapshotsOptionType.INCLUDE,
            new BlobRequestConditions().setLeaseId("leaseId"));

        batchAsyncClient.submitBatchWithResponse(batch, true).subscribe(response -> {
            System.out.printf("Batch submission completed with status code: %d%n", response.getStatusCode());
            System.out.printf("Delete operation 1 completed with status code: %d%n", deleteResponse1.getStatusCode());
            System.out.printf("Delete operation 2 completed with status code: %d%n", deleteResponse2.getStatusCode());
        }, error -> System.err.printf("Batch submission failed. Error message: %s%n", error.getMessage()));
        // END: com.azure.storage.blob.batch.BlobBatchAsyncClient.submitBatch#BlobBatch-boolean
    }

    /**
     * Code snippet for {@link BlobBatchAsyncClient#deleteBlobs(List, DeleteSnapshotsOptionType)}
     */
    public void deleteBlobs() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("<CONTAINER_NAME>");
        BlobClient blobClient1 = containerClient.getBlobClient("<BLOB_NAME1>");
        BlobClient blobClient2 = containerClient.getBlobClient("<BLOB_NAME2>");
        BlobClient blobClient3 = containerClient.getBlobClient("<BLOB_NAME3>");

        // BEGIN: com.azure.storage.blob.batch.BlobBatchAsyncClient.deleteBlobs#List-DeleteSnapshotsOptionType
        List<String> blobUrls = new ArrayList<>();
        blobUrls.add(blobClient1.getBlobUrl());
        blobUrls.add(blobClient2.getBlobUrl());
        blobUrls.add(blobClient3.getBlobUrl());

        batchAsyncClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE).subscribe(response ->
                System.out.printf("Deleting blob with URL %s completed with status code %d%n",
                    response.getRequest().getUrl(), response.getStatusCode()),
            error -> System.err.printf("Deleting blob failed with exception: %s%n", error.getMessage()));
        // END: com.azure.storage.blob.batch.BlobBatchAsyncClient.deleteBlobs#List-DeleteSnapshotsOptionType
    }

    /**
     * Code snippet for {@link BlobBatchAsyncClient#setBlobsAccessTier(List, AccessTier)}
     */
    public void setBlobsAccessTier() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("<CONTAINER_NAME>");
        BlobClient blobClient1 = containerClient.getBlobClient("<BLOB_NAME1>");
        BlobClient blobClient2 = containerClient.getBlobClient("<BLOB_NAME2>");
        BlobClient blobClient3 = containerClient.getBlobClient("<BLOB_NAME3>");

        // BEGIN: com.azure.storage.blob.batch.BlobBatchAsyncClient.setBlobsAccessTier#List-AccessTier
        List<String> blobUrls = new ArrayList<>();
        blobUrls.add(blobClient1.getBlobUrl());
        blobUrls.add(blobClient2.getBlobUrl());
        blobUrls.add(blobClient3.getBlobUrl());

        batchAsyncClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).subscribe(response ->
                System.out.printf("Setting blob access tier with URL %s completed with status code %d%n",
                    response.getRequest().getUrl(), response.getStatusCode()),
            error -> System.err.printf("Setting blob access tier failed with exception: %s%n", error.getMessage()));
        // END: com.azure.storage.blob.batch.BlobBatchAsyncClient.setBlobsAccessTier#List-AccessTier
    }
}
