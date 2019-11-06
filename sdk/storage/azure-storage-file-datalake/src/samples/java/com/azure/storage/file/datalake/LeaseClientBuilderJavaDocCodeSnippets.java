// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

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
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithDirectory
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .pathClient(directoryClient)
            .buildClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithDirectory
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithDirectoryAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithDirectoryAndLeaseId
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .pathClient(directoryClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithDirectoryAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithFile() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFile
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .pathClient(fileClient)
            .buildClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFile
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithFileAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFileAndLeaseId
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .pathClient(fileClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFileAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystem
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .fileSystemClient(dataLakeFileSystemClient)
            .buildClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystem
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildClient()}.
     */
    public void syncInstantiationWithFileSystemAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystemAndLeaseId
        DataLakeLeaseClient dataLakeLeaseClient = new DataLakeLeaseClientBuilder()
            .fileSystemClient(dataLakeFileSystemClient)
            .leaseId(leaseId)
            .buildClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystemAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithFile() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFile
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .pathAsyncClient(fileAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFile
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithFileAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .pathAsyncClient(fileAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithDirectory() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectory
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .pathAsyncClient(directoryAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectory
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithDirectoryAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectoryAndLeaseId
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .pathAsyncClient(directoryAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectoryAndLeaseId
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystem
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .fileSystemAsyncClient(dataLakeFileSystemAsyncClient)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystem
    }

    /**
     * Code snippets for {@link DataLakeLeaseClientBuilder#buildAsyncClient()}.
     */
    public void asyncInstantiationWithFileSystemAndLeaseId() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystemAndLeaseId
        DataLakeLeaseAsyncClient dataLakeLeaseAsyncClient = new DataLakeLeaseClientBuilder()
            .fileSystemAsyncClient(dataLakeFileSystemAsyncClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        // END: com.azure.storage.file.datalake.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystemAndLeaseId
    }
}
