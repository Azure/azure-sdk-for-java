// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.ContainerAsyncClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.blob.ContainerClientBuilder;

public class LeaseClientBuilderJavaDocCodeSnippets {
    private ContainerAsyncClient containerAsyncClient = new ContainerClientBuilder()
        .containerName("container")
        .buildAsyncClient();

    private ContainerClient containerClient = new ContainerClientBuilder()
        .containerName("container")
        .buildClient();

    private BlobAsyncClient blobAsyncClient = containerAsyncClient.getBlobAsyncClient("blob");
    private BlobClient blobClient = containerClient.getBlobClient("blob");

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
            .containerClient(containerClient)
            .buildClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainer
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithContainerAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainerAndLeaseId
        LeaseClient leaseClient = new LeaseClientBuilder()
            .containerClient(containerClient)
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
            .containerAsyncClient(containerAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainer
    }

    /**
     * Code snippets for {@link LeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithContainerAndLeaseId() {
        // BEGIN: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainerAndLeaseId
        LeaseAsyncClient leaseAsyncClient = new LeaseClientBuilder()
            .containerAsyncClient(containerAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainerAndLeaseId
    }
}
