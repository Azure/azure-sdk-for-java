// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

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
 * Code snippets for {@link PathClientBuilder}
 */
@SuppressWarnings({"unused"})
public class PathClientBuilderJavaDocCodeSnippets {
    private String connectionString = "AccountName=name;AccountKey=key;DefaultEndpointProtocol=protocol;EndpointSuffix=suffix";
    private String endpoint = "endpointURL";
    private String fileSystemName = "file system Name";
    private StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential("accountName", "accountKey");
    private HttpPipeline httpPipeline = new HttpPipelineBuilder()
        .httpClient(HttpClient.createDefault())
        .policies(new AddDatePolicy())
        .policies(new RequestIdPolicy())
        .policies(new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential))
        .policies(new RequestRetryPolicy(new RequestRetryOptions()))
        .build();

    /**
     * Code snippet for {@link PathClientBuilder#buildFileClient()} using credential and endpoint
     */
    public void fileClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.PathClientBuilder.buildFileClient
        DataLakeFileClient client = new PathClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildFileClient();
        // END: com.azure.storage.file.datalake.PathClientBuilder.buildFileClient
    }

    /**
     * Code snippet for {@link PathClientBuilder#buildFileAsyncClient()} using credential and endpoint
     */
    public void fileAsyncClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.PathClientBuilder.buildFileAsyncClient
        DataLakeFileAsyncClient client = new PathClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildFileAsyncClient();
        // END: com.azure.storage.file.datalake.PathClientBuilder.buildFileAsyncClient
    }

    /**
     * Code snippet for {@link PathClientBuilder#buildDirectoryClient()} using credential and endpoint
     */
    public void directoryClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.PathClientBuilder.buildDirectoryClient
        DataLakeDirectoryClient client = new PathClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildDirectoryClient();
        // END: com.azure.storage.file.datalake.PathClientBuilder.buildDirectoryClient
    }

    /**
     * Code snippet for {@link PathClientBuilder#buildDirectoryAsyncClient()} using credential and endpoint
     */
    public void directoryAsyncClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.PathClientBuilder.buildDirectoryAsyncClient
        DataLakeDirectoryAsyncClient client = new PathClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.datalake.PathClientBuilder.buildDirectoryAsyncClient
    }

}
