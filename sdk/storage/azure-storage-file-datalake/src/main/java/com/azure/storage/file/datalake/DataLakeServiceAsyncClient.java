// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.PagedResponse;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.models.FileSystemItem;

public class DataLakeServiceAsyncClient {

    private BlobServiceAsyncClient blobServiceAsyncClient;
    private FileSystemAsyncClient fileSystemAsyncClient;

    DataLakeServiceAsyncClient(AzureBlobStorageImpl blobImpl, DataLakeStorageClientImpl dataLakeImpl,
        String accountName) {
    }

    /**
     * Gets the associated {@link BlobServiceAsyncClient}
     * @return A {@link BlobServiceAsyncClient}
     */
    public BlobServiceAsyncClient getBlobServiceAsyncClient() {
        return this.blobServiceAsyncClient;
    }

    /**
     * Gets the associated {@link FileSystemAsyncClient}
     * @return A {@link FileSystemAsyncClient}
     */
    public FileSystemAsyncClient getFileSystemAsyncClient() {
        return this.fileSystemAsyncClient;
    }

    // TODO (gapra) : Add getfilesystems

}
