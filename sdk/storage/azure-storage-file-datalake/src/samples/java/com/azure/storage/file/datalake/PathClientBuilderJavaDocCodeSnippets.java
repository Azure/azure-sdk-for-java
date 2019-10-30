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
 * Code snippets for {@link DataLakePathClientBuilder}
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
     * Code snippet for {@link DataLakePathClientBuilder#buildFileClient()} using credential and endpoint
     */
    public void fileClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClientBuilder.buildFileClient
        DataLakeFileClient client = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildFileClient();
        // END: com.azure.storage.file.datalake.DataLakePathClientBuilder.buildFileClient
    }

    /**
     * Code snippet for {@link DataLakePathClientBuilder#buildFileAsyncClient()} using credential and endpoint
     */
    public void fileAsyncClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClientBuilder.buildFileAsyncClient
        DataLakeFileAsyncClient client = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildFileAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakePathClientBuilder.buildFileAsyncClient
    }

    /**
     * Code snippet for {@link DataLakePathClientBuilder#buildDirectoryClient()} using credential and endpoint
     */
    public void directoryClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClientBuilder.buildDirectoryClient
        DataLakeDirectoryClient client = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildDirectoryClient();
        // END: com.azure.storage.file.datalake.DataLakePathClientBuilder.buildDirectoryClient
    }

    /**
     * Code snippet for {@link DataLakePathClientBuilder#buildDirectoryAsyncClient()} using credential and endpoint
     */
    public void directoryAsyncClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClientBuilder.buildDirectoryAsyncClient
        DataLakeDirectoryAsyncClient client = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakePathClientBuilder.buildDirectoryAsyncClient
    }

}
