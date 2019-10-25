// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class LeaseClientBuilderJavaDocCodeSnippets {
    private BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
        .containerName("container")
        .buildAsyncClient();

    private BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
        .containerName("container")
        .buildClient();

    private BlobAsyncClient blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient("blob");
    private BlobClient blobClient = blobContainerClient.getBlobClient("blob");

    private String leaseId = "leaseId";

    /**
     * Code snippets for {@link BlobLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithBlob() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlob
        BlobLeaseClient blobLeaseClient = new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .buildClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlob
    }

    /**
     * Code snippets for {@link BlobLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithBlobAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.syncInstantiationWithBlobAndLeaseId
        BlobLeaseClient blobLeaseClient = new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.syncInstantiationWithBlobAndLeaseId
    }

    /**
     * Code snippets for {@link BlobLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithContainer() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainer
        BlobLeaseClient blobLeaseClient = new BlobLeaseClientBuilder()
            .containerClient(blobContainerClient)
            .buildClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainer
    }

    /**
     * Code snippets for {@link BlobLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithContainerAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.syncInstantiationWithContainerAndLeaseId
        BlobLeaseClient blobLeaseClient = new BlobLeaseClientBuilder()
            .containerClient(blobContainerClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.syncInstantiationWithContainerAndLeaseId
    }

    /**
     * Code snippets for {@link BlobLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithBlob() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithBlob
        BlobLeaseAsyncClient blobLeaseAsyncClient = new BlobLeaseClientBuilder()
            .blobAsyncClient(blobAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithBlob
    }

    /**
     * Code snippets for {@link BlobLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithBlobAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithBlobAndLeaseId
        BlobLeaseAsyncClient blobLeaseAsyncClient = new BlobLeaseClientBuilder()
            .blobAsyncClient(blobAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithBlobAndLeaseId
    }

    /**
     * Code snippets for {@link BlobLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithContainer() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithContainer
        BlobLeaseAsyncClient blobLeaseAsyncClient = new BlobLeaseClientBuilder()
            .containerAsyncClient(blobContainerAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithContainer
    }

    /**
     * Code snippets for {@link BlobLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithContainerAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithContainerAndLeaseId
        BlobLeaseAsyncClient blobLeaseAsyncClient = new BlobLeaseClientBuilder()
            .containerAsyncClient(blobContainerAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithContainerAndLeaseId
    }
}
