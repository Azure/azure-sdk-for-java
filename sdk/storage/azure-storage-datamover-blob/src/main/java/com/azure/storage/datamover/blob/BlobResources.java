package com.azure.storage.datamover.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;

public final class BlobResources {
    private BlobResources() {
    }

    public static StorageResource blob(BlobClient blobClient) {
        return new BlobResource(blobClient);
    }

    public static StorageResourceContainer blobContainer(BlobContainerClient blobContainerClient) {
        return new BlobResourceContainer(blobContainerClient);
    }
}
