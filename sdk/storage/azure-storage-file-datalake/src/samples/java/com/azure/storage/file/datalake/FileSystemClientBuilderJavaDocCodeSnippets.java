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
 * Code snippets for {@link DataLakeFileSystemClientBuilder}
 */
@SuppressWarnings({"unused"})
public class FileSystemClientBuilderJavaDocCodeSnippets {
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
     * Code snippet for {@link DataLakeFileSystemClientBuilder#buildClient()} using credential and endpoint
     */
    public void fileSystemClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildClient
        DataLakeFileSystemClient client = new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildClient();
        // END: com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildClient
    }

    /**
     * Code snippet for {@link DataLakeFileSystemClientBuilder#buildAsyncClient()} using credential and endpoint
     */
    public void fileSystemAsyncClientCredentialAndEndpoint() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildAsyncClient
        DataLakeFileSystemAsyncClient client = new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .credential(storageSharedKeyCredential)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder.buildAsyncClient
    }
}
