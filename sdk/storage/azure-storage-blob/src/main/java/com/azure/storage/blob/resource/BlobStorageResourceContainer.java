package com.azure.storage.blob.resource;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

class BlobStorageResourceContainer implements StorageResourceContainer {

    private final BlobContainerClient blobContainerClient;

    BlobStorageResourceContainer(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = Objects.requireNonNull(blobContainerClient);
    }

    @Override
    public Iterable<StorageResource> listResources() {
        return blobContainerClient.listBlobs()
            .mapPage(
                blobItem -> new BlobStorageResource(blobContainerClient.getBlobClient(blobItem.getName()))
            );
    }

    @Override
    public List<String> getPath() {
        return Collections.emptyList();
    }

    @Override
    public StorageResource getStorageResource(List<String> path) {
        return new BlobStorageResource(blobContainerClient.getBlobClient(String.join("/", path)));
    }

    @Override
    public StorageResourceContainer getStorageResourceContainer(List<String> path) {
        throw new UnsupportedOperationException("Virtual directories not supported yet");
    }
}
