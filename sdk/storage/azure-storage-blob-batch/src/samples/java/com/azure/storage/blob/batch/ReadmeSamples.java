// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;

import java.util.Arrays;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private final BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
        .endpoint("<YOUR_END_POINT>")
        .sasToken("<YOUR_SAS_TOKEN>")
        .buildClient();
    private final BlobBatchClient blobBatchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();
    private final String blobUrl = String.format(
        "https://%s.blob.core.windows.net/containerName/blobName", "<YOUR_STORAGE_ACCOUNT>");
    private final String blobUrl2 = String.format(
        "https://%s.blob.core.windows.net/containerName/blobName2", "<YOUR_STORAGE_ACCOUNT>");
    private final String blobUrlWithSnapshot = String.format(
        "https://%s.blob.core.windows.net/containerName/blobName?snapshot=<DateTime>", "<YOUR_STORAGE_ACCOUNT>");
    private final String blobUrlWithLease = String.format(
        "https://%s.blob.core.windows.net/containerName/blobNameWithLease", "<YOUR_STORAGE_ACCOUNT>");
    private final List<String> blobUrls = Arrays.asList(blobUrl, blobUrl2, blobUrlWithSnapshot, blobUrlWithLease);

    public void createHttpClient() {
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .port(8080)
            .wiretap(true)
            .build();
    }

    public void creatingBlobBatchClient() {
        // BEGIN: readme-sample-creatingBlobBatchClient
        BlobBatchClient blobBatchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();
        // END: readme-sample-creatingBlobBatchClient
    }

    public void bulkDeletingBlobs() {
        // BEGIN: readme-sample-bulkDeletingBlobs
        blobBatchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE).forEach(response ->
            System.out.printf("Deleting blob with URL %s completed with status code %d%n",
                response.getRequest().getUrl(), response.getStatusCode()));
        // END: readme-sample-bulkDeletingBlobs
    }

    public void bulkSettingAccessTier() {
        // BEGIN: readme-sample-bulkSettingAccessTier
        blobBatchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).forEach(response ->
            System.out.printf("Setting blob access tier with URL %s completed with status code %d%n",
                response.getRequest().getUrl(), response.getStatusCode()));
        // END: readme-sample-bulkSettingAccessTier
    }

    public void advancedBatchingDelete() {
        // BEGIN: readme-sample-advancedBatchingDelete
        BlobBatch blobBatch = blobBatchClient.getBlobBatch();

        // Delete a blob.
        Response<Void> deleteResponse = blobBatch.deleteBlob(blobUrl);

        // Delete a specific blob snapshot.
        Response<Void> deleteSnapshotResponse =
            blobBatch.deleteBlob(blobUrlWithSnapshot, DeleteSnapshotsOptionType.ONLY, null);

        // Delete a blob that has a lease.
        Response<Void> deleteWithLeaseResponse =
            blobBatch.deleteBlob(blobUrlWithLease, DeleteSnapshotsOptionType.INCLUDE, new BlobRequestConditions()
                .setLeaseId("leaseId"));

        blobBatchClient.submitBatch(blobBatch);
        System.out.printf("Deleting blob completed with status code %d%n", deleteResponse.getStatusCode());
        System.out.printf("Deleting blob snapshot completed with status code %d%n",
            deleteSnapshotResponse.getStatusCode());
        System.out.printf("Deleting blob with lease completed with status code %d%n",
            deleteWithLeaseResponse.getStatusCode());
        // END: readme-sample-advancedBatchingDelete
    }

    public void advancedBatchingSetTier() {
        // BEGIN: readme-sample-advancedBatchingSetTier
        BlobBatch blobBatch = blobBatchClient.getBlobBatch();

        // Set AccessTier on a blob.
        Response<Void> setTierResponse = blobBatch.setBlobAccessTier(blobUrl, AccessTier.COOL);

        // Set AccessTier on another blob.
        Response<Void> setTierResponse2 = blobBatch.setBlobAccessTier(blobUrl2, AccessTier.ARCHIVE);

        // Set AccessTier on a blob that has a lease.
        Response<Void> setTierWithLeaseResponse = blobBatch.setBlobAccessTier(blobUrlWithLease, AccessTier.HOT,
            "leaseId");

        blobBatchClient.submitBatch(blobBatch);
        System.out.printf("Set AccessTier on blob completed with status code %d%n", setTierResponse.getStatusCode());
        System.out.printf("Set AccessTier on blob completed with status code %d%n", setTierResponse2.getStatusCode());
        System.out.printf("Set AccessTier on  blob with lease completed with status code %d%n",
            setTierWithLeaseResponse.getStatusCode());
        // END: readme-sample-advancedBatchingSetTier
    }

    public void deleteBlobWithLease() {
        // BEGIN: readme-sample-deleteBlobWithLease
        BlobBatch blobBatch = blobBatchClient.getBlobBatch();
        Response<Void> deleteWithLeaseResponse =
            blobBatch.deleteBlob(blobUrlWithLease, DeleteSnapshotsOptionType.INCLUDE, new BlobRequestConditions()
                .setLeaseId("leaseId"));
        // END: readme-sample-deleteBlobWithLease
    }
}
