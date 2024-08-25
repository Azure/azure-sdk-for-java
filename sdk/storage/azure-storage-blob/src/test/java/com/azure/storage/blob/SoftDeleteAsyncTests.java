// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SoftDeleteAsyncTests extends BlobTestBase {
    private BlobContainerAsyncClient containerClient;
    private BlobAsyncClient blobClient;

    @BeforeEach
    public void setup() {
        containerClient = softDeleteServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        containerClient.create().block();
        blobClient = containerClient.getBlobAsyncClient(generateBlobName());
        blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
    }

    @AfterEach
    public void cleanup() {
        containerClient.delete().block();
    }

    @Test
    public void undeleteMin() {
        blobClient.delete().block();
        assertAsyncResponseStatusCode(blobClient.undeleteWithResponse(), 200);
    }

    @Test
    public void undelete() {
        blobClient.delete().block();

        StepVerifier.create(blobClient.undeleteWithResponse())
            .then(() -> blobClient.getProperties())
            .assertNext(r -> {
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
            })
            .verifyComplete();
    }

    @Test
    public void listBlobsFlatOptionsDeleted() {
        blobClient.delete().block();

        ListBlobsOptions options = new ListBlobsOptions()
            .setDetails(new BlobListDetails().setRetrieveDeletedBlobs(true))
            .setPrefix(prefix);
        StepVerifier.create(containerClient.listBlobs(options))
            .assertNext(r -> assertEquals(blobClient.getBlobName(), r.getName()))
            .verifyComplete();
    }

    @Test
    public void listBlobsHierOptionsDeleted() {
        blobClient.delete().block();

        ListBlobsOptions options = new ListBlobsOptions().setDetails(
            new BlobListDetails().setRetrieveDeletedBlobs(true)).setPrefix(prefix);
        StepVerifier.create(containerClient.listBlobsByHierarchy("", options))
            .assertNext(r -> assertEquals(blobClient.getBlobName(), r.getName()))
            .verifyComplete();
    }

}
