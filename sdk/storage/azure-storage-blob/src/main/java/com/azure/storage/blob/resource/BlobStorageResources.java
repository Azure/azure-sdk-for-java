package com.azure.storage.blob.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;

/**
 * A class with factories for blob storage resources.
 */
public final class BlobStorageResources {
    private BlobStorageResources() {
    }

    /**
     * Creates {@link StorageResource} representing a blob.
     * @param blobClient The blob client.
     * @return A {@link StorageResource} representing a blob.
     */
    public static StorageResource blob(BlobClient blobClient) {
        return new BlobStorageResource(blobClient);
    }

    /**
     * Creates {@link StorageResourceContainer} representing a blob container.
     * @param blobContainerClient The blob container client.
     * @return A {@link StorageResourceContainer} representing a blob container.
     */
    public static StorageResourceContainer blobContainer(BlobContainerClient blobContainerClient) {
        return new BlobStorageResourceContainer(blobContainerClient);
    }
}
