// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.BlobStorageException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Code snippets for {@link BlobBatchClient}
 */
public class BlobBatchClientJavaDocCodeSnippets {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_BLOB_ENDPOINT");
    private static final String SASTOKEN = Configuration.getGlobalConfiguration().get("SAS_TOKEN");

    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                                                    .endpoint(ENDPOINT)
                                                    .sasToken(SASTOKEN)
                                                    .buildClient();

    private BlobBatchClient batchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();

    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Code snippet for {@link BlobBatchClient#submitBatch(BlobBatch)}
     */
    public void submitBatch() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatchClient.submitBatch#BlobBatch
        BlobBatch batch = batchClient.getBlobBatch();

        Response<Void> deleteResponse1 = batch.deleteBlob("container", "blob1");
        Response<Void> deleteResponse2 = batch.deleteBlob("container", "blob2", DeleteSnapshotsOptionType.INCLUDE,
            new BlobRequestConditions().setLeaseId("leaseId"));

        try {
            batchClient.submitBatch(batch);
            System.out.println("Batch submission completed successfully.");
            System.out.printf("Delete operation 1 completed with status code: %d%n", deleteResponse1.getStatusCode());
            System.out.printf("Delete operation 2 completed with status code: %d%n", deleteResponse2.getStatusCode());
        } catch (BlobStorageException error) {
            System.err.printf("Batch submission failed. Error message: %s%n", error.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatchClient.submitBatch#BlobBatch
    }

    /**
     * Code snippet for {@link BlobBatchClient#submitBatchWithResponse(BlobBatch, boolean, Duration, Context)}
     */
    public void submitBatchWithResponse() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatchClient.submitBatch#BlobBatch-boolean-Duration-Context
        BlobBatch batch = batchClient.getBlobBatch();

        Response<Void> deleteResponse1 = batch.deleteBlob("container", "blob1");
        Response<Void> deleteResponse2 = batch.deleteBlob("container", "blob2", DeleteSnapshotsOptionType.INCLUDE,
            new BlobRequestConditions().setLeaseId("leaseId"));

        try {
            System.out.printf("Batch submission completed with status code: %d%n",
                batchClient.submitBatchWithResponse(batch, true, timeout, Context.NONE).getStatusCode());
            System.out.printf("Delete operation 1 completed with status code: %d%n", deleteResponse1.getStatusCode());
            System.out.printf("Delete operation 2 completed with status code: %d%n", deleteResponse2.getStatusCode());
        } catch (BlobStorageException error) {
            System.err.printf("Batch submission failed. Error message: %s%n", error.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatchClient.submitBatch#BlobBatch-boolean-Duration-Context
    }

    /**
     * Code snippet for {@link BlobBatchClient#deleteBlobs(List, DeleteSnapshotsOptionType)}
     */
    public void deleteBlobs() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("<CONTAINER_NAME>");
        BlobClient blobClient1 = containerClient.getBlobClient("<BLOB_NAME1>");
        BlobClient blobClient2 = containerClient.getBlobClient("<BLOB_NAME2>");
        BlobClient blobClient3 = containerClient.getBlobClient("<BLOB_NAME3>");

        // BEGIN: com.azure.storage.blob.batch.BlobBatchClient.deleteBlobs#List-DeleteSnapshotsOptionType
        List<String> blobUrls = new ArrayList<>();
        blobUrls.add(blobClient1.getBlobUrl());
        blobUrls.add(blobClient2.getBlobUrl());
        blobUrls.add(blobClient3.getBlobUrl());

        try {
            batchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE).forEach(response ->
                System.out.printf("Deleting blob with URL %s completed with status code %d%n",
                    response.getRequest().getUrl(), response.getStatusCode()));
        } catch (Throwable error) {
            System.err.printf("Deleting blob failed with exception: %s%n", error.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatchClient.deleteBlobs#List-DeleteSnapshotsOptionType
    }

    /**
     * Code snippet for {@link BlobBatchClient#deleteBlobs(List, DeleteSnapshotsOptionType, Duration, Context)}
     */
    public void deleteBlobsWithTimeoutAndContext() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("<CONTAINER_NAME>");
        BlobClient blobClient1 = containerClient.getBlobClient("<BLOB_NAME1>");
        BlobClient blobClient2 = containerClient.getBlobClient("<BLOB_NAME2>");
        BlobClient blobClient3 = containerClient.getBlobClient("<BLOB_NAME3>");

        // BEGIN: com.azure.storage.blob.batch.BlobBatchClient.deleteBlobs#List-DeleteSnapshotsOptionType-Duration-Context
        List<String> blobUrls = new ArrayList<>();
        blobUrls.add(blobClient1.getBlobUrl());
        blobUrls.add(blobClient2.getBlobUrl());
        blobUrls.add(blobClient3.getBlobUrl());

        try {
            batchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE, timeout, Context.NONE)
                .forEach(response -> System.out.printf("Deleting blob with URL %s completed with status code %d%n",
                    response.getRequest().getUrl(), response.getStatusCode()));
        } catch (Throwable error) {
            System.err.printf("Deleting blob failed with exception: %s%n", error.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatchClient.deleteBlobs#List-DeleteSnapshotsOptionType-Duration-Context
    }

    /**
     * Code snippet for {@link BlobBatchClient#setBlobsAccessTier(List, AccessTier)}
     */
    public void setBlobsAccessTier() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("<CONTAINER_NAME>");
        BlobClient blobClient1 = containerClient.getBlobClient("<BLOB_NAME1>");
        BlobClient blobClient2 = containerClient.getBlobClient("<BLOB_NAME2>");
        BlobClient blobClient3 = containerClient.getBlobClient("<BLOB_NAME3>");

        // BEGIN: com.azure.storage.blob.batch.BlobBatchClient.setBlobsAccessTier#List-AccessTier
        List<String> blobUrls = new ArrayList<>();
        blobUrls.add(blobClient1.getBlobUrl());
        blobUrls.add(blobClient2.getBlobUrl());
        blobUrls.add(blobClient3.getBlobUrl());

        try {
            batchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).forEach(response ->
                System.out.printf("Setting blob access tier with URL %s completed with status code %d%n",
                    response.getRequest().getUrl(), response.getStatusCode()));
        } catch (Throwable error) {
            System.err.printf("Setting blob access tier failed with exception: %s%n", error.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatchClient.setBlobsAccessTier#List-AccessTier
    }

    /**
     * Code snippet for {@link BlobBatchClient#setBlobsAccessTier(List, AccessTier, Duration, Context)}
     */
    public void setBlobsAccessTierWithTimeoutAndContext() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("<CONTAINER_NAME>");
        BlobClient blobClient1 = containerClient.getBlobClient("<BLOB_NAME1>");
        BlobClient blobClient2 = containerClient.getBlobClient("<BLOB_NAME2>");
        BlobClient blobClient3 = containerClient.getBlobClient("<BLOB_NAME3>");

        // BEGIN: com.azure.storage.blob.batch.BlobBatchClient.setBlobsAccessTier#List-AccessTier-Duration-Context
        List<String> blobUrls = new ArrayList<>();
        blobUrls.add(blobClient1.getBlobUrl());
        blobUrls.add(blobClient2.getBlobUrl());
        blobUrls.add(blobClient3.getBlobUrl());

        try {
            batchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT, timeout, Context.NONE).forEach(response ->
                System.out.printf("Setting blob access tier with URL %s completed with status code %d%n",
                    response.getRequest().getUrl(), response.getStatusCode()));
        } catch (Throwable error) {
            System.err.printf("Setting blob access tier failed with exception: %s%n", error.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatchClient.setBlobsAccessTier#List-AccessTier-Duration-Context
    }
}
