// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClient;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder;

public class LeaseClientBuilderJavaDocCodeSnippets {
    private DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient = new DataLakeFileSystemClientBuilder()
        .fileSystemName("fileSystemName")
        .buildAsyncClient();

    private DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder()
        .fileSystemName("fileSystemName")
        .buildClient();

    private DataLakeFileAsyncClient fileAsyncClient = dataLakeFileSystemAsyncClient.getFileAsyncClient("file");
    private DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("file");

    private DataLakeDirectoryAsyncClient directoryAsyncClient = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient("dir");
    private DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("dir");

    private String leaseId = "leaseId";

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithDirectory() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithDirectory
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .directoryClient(directoryClient)
            .buildClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithDirectory
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithDirectoryAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithDirectoryAndLeaseId
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .directoryClient(directoryClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithDirectoryAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithFile() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFile
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .fileClient(fileClient)
            .buildClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFile
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithFileAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFileAndLeaseId
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .fileClient(fileClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFileAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystem
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .fileSystemClient(dataLakeFileSystemClient)
            .buildClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystem
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithFileSystemAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystemAndLeaseId
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .fileSystemClient(dataLakeFileSystemClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystemAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithFile() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFile
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .fileAsyncClient(fileAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFile
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithFileAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .fileAsyncClient(fileAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithDirectory() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectory
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .directoryAsyncClient(directoryAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectory
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithDirectoryAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectoryAndLeaseId
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .directoryAsyncClient(directoryAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectoryAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystem
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .fileSystemAsyncClient(dataLakeFileSystemAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystem
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithFileSystemAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystemAndLeaseId
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .fileSystemAsyncClient(dataLakeFileSystemAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystemAndLeaseId
    }
}
