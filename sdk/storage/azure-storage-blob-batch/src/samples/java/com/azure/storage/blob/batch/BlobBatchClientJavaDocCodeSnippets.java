// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.StorageException;

import java.time.Duration;

/**
 * Code snippets for {@link BlobBatchClient}
 */
public class BlobBatchClientJavaDocCodeSnippets {
    private BlobBatchClient batchClient = new BlobBatchClientBuilder(new BlobServiceClientBuilder().buildClient())
        .buildClient();
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Code snippet for {@link BlobBatchClient#submitBatch(BlobBatch)}
     */
    public void submitBatch() {
        // BEGIN: com.azure.storage.blob.batch.BlobBatchClient.submitBatch#BlobBatch
        BlobBatch batch = batchClient.getBlobBatch();

        Response<Void> deleteResponse1 = batch.delete("container", "blob1");
        Response<Void> deleteResponse2 = batch.delete("container", "blob2", DeleteSnapshotsOptionType.INCLUDE,
            new BlobAccessConditions().setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("leaseId")));

        try {
            batchClient.submitBatch(batch);
            System.out.println("Batch submission completed successfully.");
            System.out.printf("Delete operation 1 completed with status code: %d%n", deleteResponse1.getStatusCode());
            System.out.printf("Delete operation 2 completed with status code: %d%n", deleteResponse2.getStatusCode());
        } catch (StorageException error) {
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

        Response<Void> deleteResponse1 = batch.delete("container", "blob1");
        Response<Void> deleteResponse2 = batch.delete("container", "blob2", DeleteSnapshotsOptionType.INCLUDE,
            new BlobAccessConditions().setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("leaseId")));

        try {
            System.out.printf("Batch submission completed with status code: %d%n",
                batchClient.submitBatchWithResponse(batch, true, timeout, Context.NONE).getStatusCode());
            System.out.printf("Delete operation 1 completed with status code: %d%n", deleteResponse1.getStatusCode());
            System.out.printf("Delete operation 2 completed with status code: %d%n", deleteResponse2.getStatusCode());
        } catch (StorageException error) {
            System.err.printf("Batch submission failed. Error message: %s%n", error.getMessage());
        }
        // END: com.azure.storage.blob.batch.BlobBatchClient.submitBatch#BlobBatch-boolean-Duration-Context
    }
}
