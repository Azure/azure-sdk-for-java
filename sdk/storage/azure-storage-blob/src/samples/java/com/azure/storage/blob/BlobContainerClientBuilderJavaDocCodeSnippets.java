// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

/**
 * Code snippets for {@link BlobContainerClientBuilder}
 */
@SuppressWarnings({"unused"})
public class BlobContainerClientBuilderJavaDocCodeSnippets {
    private String connectionString = "AccountName=name;AccountKey=key;DefaultEndpointProtocol=protocol;EndpointSuffix=suffix";
    private String endpoint = "endpointURL";
    private String containerName = "container Name";
    private StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential("accountName", "accountKey");
    private HttpPipeline httpPipeline = new HttpPipelineBuilder()
        .httpClient(HttpClient.createDefault())
        .policies(new AddDatePolicy())
        .policies(new RequestIdPolicy())
        .policies(new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential))
        .policies(new RequestRetryPolicy(new RequestRetryOptions()))
        .build();

    /**
     * Code snippet for {@link BlobContainerClientBuilder#buildClient()} using connection string
     */
    public void containerClientConnectionString() {
        // BEGIN: com.azure.storage.blob.BlobContainerClientBuilder.buildClient
        BlobContainerClient client = new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.blob.BlobContainerClientBuilder.buildClient
    }

    /**
     * Code snippet for {@link BlobContainerClientBuilder#buildAsyncClient()} using connection string
     */
    public void containerAsyncClientConnectionString() {
        // BEGIN: com.azure.storage.blob.BlobContainerClientBuilder.buildAsyncClient
        BlobContainerAsyncClient client = new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.blob.BlobContainerClientBuilder.buildAsyncClient
    }

    /**
     * Code snippet for {@link BlobContainerClientBuilder#buildClient()} using credential and endpoint
     */
    public void containerClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.blob.BlobContainerClientBuilder.endpoint#String
        BlobContainerClient client = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildClient();
        // END: com.azure.storage.blob.BlobContainerClientBuilder.endpoint#String
    }

    /**
     * Code snippet for {@link BlobContainerClientBuilder#buildClient()} using Container Name
     */
    public void containerClientContainerName() {
        // BEGIN: com.azure.storage.blob.BlobContainerClientBuilder.containerName#String
        BlobContainerClient client = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .containerName(containerName)
            .buildClient();
        // END: com.azure.storage.blob.BlobContainerClientBuilder.containerName#String
    }
}
