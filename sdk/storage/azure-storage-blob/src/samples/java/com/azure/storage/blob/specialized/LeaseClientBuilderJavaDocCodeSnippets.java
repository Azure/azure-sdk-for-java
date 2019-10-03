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
     * Code snippets for {@link LeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithBlob() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlob
        LeaseClient leaseClient = new LeaseClientBuilder()
            .blobClient(blobClient)
            .buildClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlob
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithBlobAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlobAndLeaseId
        LeaseClient leaseClient = new LeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlobAndLeaseId
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithContainer() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainer
        LeaseClient leaseClient = new LeaseClientBuilder()
            .containerClient(blobContainerClient)
            .buildClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainer
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithContainerAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainerAndLeaseId
        LeaseClient leaseClient = new LeaseClientBuilder()
            .containerClient(blobContainerClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainerAndLeaseId
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithBlob() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithBlob
        LeaseAsyncClient leaseAsyncClient = new LeaseClientBuilder()
            .blobAsyncClient(blobAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithBlob
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithBlobAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithBlobAndLeaseId
        LeaseAsyncClient leaseAsyncClient = new LeaseClientBuilder()
            .blobAsyncClient(blobAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithBlobAndLeaseId
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithContainer() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainer
        LeaseAsyncClient leaseAsyncClient = new LeaseClientBuilder()
            .containerAsyncClient(blobContainerAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainer
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithContainerAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainerAndLeaseId
        LeaseAsyncClient leaseAsyncClient = new LeaseClientBuilder()
            .containerAsyncClient(blobContainerAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainerAndLeaseId
    }
}
