// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;

import java.util.Arrays;
import java.util.List;

/**
 * Code samples for the READEME.md
 */
public class ReadmeCodeSamples {
    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().buildClient();
    private BlobBatchClient blobBatchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();
    private String blobUrl = "https://account.core.windows.net/containerName/blobName";
    private String blobUrl2 = "https://account.core.windows.net/containerName/blobName2";
    private String blobUrlWithSnapshot = "https://account.core.windows.net/containerName/blobName?snapshot=<DateTime>";
    private String blobUrlWithLease = "https://account.core.windows.net/containerName/blobNameWithLease";
    private List<String> blobUrls = Arrays.asList(blobUrl, blobUrl2, blobUrlWithSnapshot, blobUrlWithLease);

    public void creatingBlobBatchClient() {
        BlobBatchClient blobBatchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();
    }

    public void bulkDeletingBlobs() {
        blobBatchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE).forEach(response ->
            System.out.printf("Deleting blob with URL %s completed with status code %d%n",
                response.getRequest().getUrl(), response.getStatusCode()));
    }

    public void bulkSettingAccessTier() {
        blobBatchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).forEach(response ->
            System.out.printf("Setting blob access tier with URL %s completed with status code %d%n",
                response.getRequest().getUrl(), response.getStatusCode()));
    }

    public void advancedBatchingDelete() {
        BlobBatch blobBatch = blobBatchClient.getBlobBatch();

        // Delete a blob.
        Response<Void> deleteResponse = blobBatch.deleteBlob(blobUrl);

        // Delete a specific blob snapshot.
        Response<Void> deleteSnapshotResponse =
            blobBatch.deleteBlob(blobUrlWithSnapshot, DeleteSnapshotsOptionType.ONLY, null);

        // Delete a blob that has a lease.
        Response<Void> deleteWithLeaseResponse =
            blobBatch.deleteBlob(blobUrlWithLease, DeleteSnapshotsOptionType.INCLUDE, new BlobAccessConditions()
                .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId("leaseId")));

        blobBatchClient.submitBatch(blobBatch);
        System.out.printf("Deleting blob completed with status code %d%n", deleteResponse.getStatusCode());
        System.out.printf("Deleting blob snapshot completed with status code %d%n",
            deleteSnapshotResponse.getStatusCode());
        System.out.printf("Deleting blob with lease completed with status code %d%n",
            deleteWithLeaseResponse.getStatusCode());
    }

    public void advancedBatchingSetTier() {
        BlobBatch blobBatch = blobBatchClient.getBlobBatch();

        // Set AccessTier on a blob.
        Response<Void> setTierResponse = blobBatch.setBlobAccessTier(blobUrl, AccessTier.COOL);

        // Set AccessTier on another blob.
        Response<Void> setTierResponse2 = blobBatch.setBlobAccessTier(blobUrl2, AccessTier.ARCHIVE);

        // Set AccessTier on a blob that has a lease.
        Response<Void> setTierWithLeaseResponse = blobBatch.setBlobAccessTier(blobUrlWithLease, AccessTier.HOT,
            new LeaseAccessConditions().setLeaseId("leaseId"));

        blobBatchClient.submitBatch(blobBatch);
        System.out.printf("Set AccessTier on blob completed with status code %d%n", setTierResponse.getStatusCode());
        System.out.printf("Set AccessTier on blob completed with status code %d%n", setTierResponse2.getStatusCode());
        System.out.printf("Set AccessTier on  blob with lease completed with status code %d%n",
            setTierWithLeaseResponse.getStatusCode());
    }
}
