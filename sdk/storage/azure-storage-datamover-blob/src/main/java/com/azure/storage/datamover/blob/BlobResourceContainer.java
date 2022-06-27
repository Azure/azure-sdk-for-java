package com.azure.storage.datamover.blob;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;

import java.time.OffsetDateTime;
import java.util.Objects;

class BlobResourceContainer extends StorageResourceContainer {

    private final BlobContainerClient blobContainerClient;

    BlobResourceContainer(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = Objects.requireNonNull(blobContainerClient);
    }

    @Override
    protected Iterable<StorageResource> listResources() {
        return blobContainerClient.listBlobs()
            .mapPage(
                blobItem -> new BlobResource(blobContainerClient.getBlobClient(blobItem.getName()))
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
}
