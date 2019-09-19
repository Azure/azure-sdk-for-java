// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;

/**
 * Code snippets for {@link ContainerClientBuilder}
 */
@SuppressWarnings({"unused"})
public class ContainerClientBuilderJavaDocCodeSnippets {
    private String connectionString = "AccountName=name;AccountKey=key;DefaultEndpointProtocol=protocol;EndpointSuffix=suffix";
    private String endpoint = "endpointURL";
    private String containerName = "container Name";
    private SharedKeyCredential sharedKeyCredential = new SharedKeyCredential("accountName", "accountKey");
    private HttpPipeline httpPipeline = new HttpPipelineBuilder()
        .httpClient(HttpClient.createDefault())
        .policies(new AddDatePolicy())
        .policies(new RequestIdPolicy())
        .policies(new SharedKeyCredentialPolicy(sharedKeyCredential))
        .policies(new RequestRetryPolicy(new RequestRetryOptions()))
        .build();

    /**
     * Code snippet for {@link ContainerClientBuilder#buildClient()} using connection string
     */
    public void containerClientConnectionString() {
        // BEGIN: com.azure.storage.blob.ContainerClientBuilder.buildClient
        ContainerClient client = new ContainerClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.blob.ContainerClientBuilder.buildClient
    }

    /**
     * Code snippet for {@link ContainerClientBuilder#buildAsyncClient()} using connection string
     */
    public void containerAsyncClientConnectionString() {
        // BEGIN: com.azure.storage.blob.ContainerClientBuilder.buildAsyncClient
        ContainerAsyncClient client = new ContainerClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.blob.ContainerClientBuilder.buildAsyncClient
    }

    /**
     * Code snippet for {@link ContainerClientBuilder#buildClient()} using credential and endpoint
     */
    public void containerClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.blob.ContainerClientBuilder.endpoint#String
        ContainerClient client = new ContainerClientBuilder()
            .endpoint(endpoint)
            .credential(sharedKeyCredential)
            .buildClient();
        // END: com.azure.storage.blob.ContainerClientBuilder.endpoint#String
    }

    /**
     * Code snippet for {@link ContainerClientBuilder#buildClient()} using Container Name
     */
    public void containerClientContainerName() {
        // BEGIN: com.azure.storage.blob.ContainerClientBuilder.containerName#String
        ContainerClient client = new ContainerClientBuilder()
            .endpoint(endpoint)
            .containerName(containerName)
            .buildClient();
        // END: com.azure.storage.blob.ContainerClientBuilder.containerName#String
    }
}
