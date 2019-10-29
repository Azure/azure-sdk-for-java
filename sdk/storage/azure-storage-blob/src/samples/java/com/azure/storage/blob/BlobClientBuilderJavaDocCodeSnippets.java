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
 * Code snippets for {@link BlobClientBuilder}
 */
@SuppressWarnings({"unused"})
public class BlobClientBuilderJavaDocCodeSnippets {
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
     * Code snippet for {@link BlobClientBuilder#buildClient()} using connection string
     */
    public void blobClientConnectionString() {
        // BEGIN: com.azure.storage.blob.BlobClientBuilder.buildClient
        BlobClient client = new BlobClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.blob.BlobClientBuilder.buildClient
    }

    /**
     * Code snippet for {@link BlobClientBuilder#buildAsyncClient()} using connection string
     */
    public void blobAsyncClientConnectionString() {
        // BEGIN: com.azure.storage.blob.BlobClientBuilder.buildAsyncClient
        BlobAsyncClient client = new BlobClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.blob.BlobClientBuilder.buildAsyncClient
    }

    /**
     * Code snippet for {@link BlobClientBuilder#endpoint(String)} using credential and endpoint
     */
    public void blobClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.Builder.endpoint#String
        BlobClient client = new BlobClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildClient();
        // END: com.azure.storage.blob.specialized.BlobClientBase.Builder.endpoint#String
    }

    /**
     * Code snippet for {@link BlobClientBuilder#containerName(String)} using HttpPipeline
     */
    public void blobClientHttpPipeline() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.Builder.containerName#String
        BlobClient client = new BlobClientBuilder()
            .endpoint(endpoint)
            .containerName(containerName)
            .buildClient();
        // END: com.azure.storage.blob.specialized.BlobClientBase.Builder.containerName#String
    }
}
