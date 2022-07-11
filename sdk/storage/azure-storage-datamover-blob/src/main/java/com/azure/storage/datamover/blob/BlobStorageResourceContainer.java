package com.azure.storage.datamover.blob;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.common.resource.TransferCapabilities;
import com.azure.storage.common.resource.TransferCapabilitiesBuilder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class BlobStorageResourceContainer extends StorageResourceContainer {

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
    protected TransferCapabilities getIncomingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            blobContainerClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new BlobContainerSasPermission().setWritePermission(true)));
            transferCapabilitiesBuilder.canUseSasUri(true);
        } catch (Exception e) {
            // ignore
        }

        return transferCapabilitiesBuilder.build();
    }

    @Override
    protected List<String> getPath() {
        return Collections.emptyList();
    }

    @Override
    public StorageResource getStorageResource(List<String> path) {
        return new BlobStorageResource(blobContainerClient.getBlobClient(String.join("/", path)));
    }
}
