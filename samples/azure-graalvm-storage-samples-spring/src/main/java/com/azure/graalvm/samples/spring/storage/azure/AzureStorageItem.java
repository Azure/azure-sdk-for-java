// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.spring.storage.azure;

import com.azure.graalvm.samples.spring.storage.StorageItem;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobItem;

import java.io.InputStream;

/**
 * Azure Blob Storage implementation of the StorageItem interface, allowing for access to details of a single Azure
 * Blob Storage blob item.
 */
public class AzureStorageItem implements StorageItem {
    private final String fileName;
    private double fileSize;
    private final String contentType;

    private BlobClient blobClient;

    AzureStorageItem(BlobItem blobItem) {
        this.fileName = blobItem.getName();
        setFileSize(blobItem.getProperties().getContentLength());
        this.contentType = blobItem.getProperties().getContentType();
    }

    AzureStorageItem(BlobClient blobClient) {
        this.fileName = blobClient.getBlobName();
        setFileSize(blobClient.getProperties().getBlobSize());
        this.blobClient = blobClient;
        this.contentType = blobClient.getProperties().getContentType();
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public double getFileSize() {
        return fileSize;
    }

    private void setFileSize(double sizeInBytes) {
        this.fileSize = sizeInBytes / 1024.0;
    }

    @Override
    public InputStream getContent() {
        return blobClient.openInputStream();
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
