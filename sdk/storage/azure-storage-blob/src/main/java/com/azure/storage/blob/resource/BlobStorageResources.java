package com.azure.storage.blob.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;

public final class BlobStorageResources {
    private BlobStorageResources() {
    }

    public static StorageResource blob(BlobClient blobClient) {
        return new BlobStorageResource(blobClient);
    }

    public static StorageResourceContainer blobContainer(BlobContainerClient blobContainerClient) {
        return new BlobStorageResourceContainer(blobContainerClient);
    }
}
