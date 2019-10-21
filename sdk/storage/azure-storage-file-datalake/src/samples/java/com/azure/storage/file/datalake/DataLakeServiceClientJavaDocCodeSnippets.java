// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.PublicAccessType;

import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link DataLakeServiceAsyncClient}
 */
@SuppressWarnings({"unused"})
public class DataLakeServiceClientJavaDocCodeSnippets {
    private DataLakeServiceAsyncClient client = JavaDocCodeSnippetsHelpers.getDataLakeServiceAsyncClient();

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#getFileSystemAsyncClient(String)}
     */
    public void getFileSystemClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getFileSystemAsyncClient#String
        FileSystemAsyncClient fileSystemAsyncClient = client.getFileSystemAsyncClient("fileSystemName");
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getFileSystemAsyncClient#String
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#createFileSystem(String)}
     */
    public void createFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystem#String
        FileSystemAsyncClient fileSystemAsyncClient =
            client.createFileSystem("fileSystemName").block();
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystem#String
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#createFileSystemWithResponse(String, Map, PublicAccessType)}
     */
    public void createContainerWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystemWithResponse#String-Map-PublicAccessType
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");

        FileSystemAsyncClient fileSystemAsyncClient = client
            .createFileSystemWithResponse("fileSystemName", metadata, PublicAccessType.CONTAINER).block().getValue();
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystemWithResponse#String-Map-PublicAccessType
    }

}
