// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;

public class DataLakeServiceAsyncClient {

    private AzureBlobStorageImpl blobImpl;
    private final String accountName;
    private BlobServiceAsyncClient blobServiceAsyncClient;

    DataLakeServiceAsyncClient(BlobServiceAsyncClient blobServiceAsyncClient, DataLakeStorageClientImpl dataLakeImpl,
        String accountName) {
        this.accountName = accountName;
        this.blobServiceAsyncClient = blobServiceAsyncClient;
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
    public FileSystemAsyncClient getFileSystemAsyncClient(String fileSystemName) {
        if (ImplUtils.isNullOrEmpty(fileSystemName)) {
            fileSystemName = FileSystemAsyncClient.ROOT_FILESYSTEM_NAME;
        }
        return null;

    }

    // TODO (gapra) : Add getfilesystems


    /**
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     */
    public String getAccountUrl() {
        return blobImpl.getUrl();
    }

}
