package com.azure.storage.datamover.blob;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferMethod;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    protected Set<TransferMethod> getIncomingTransferMethods() {
        Set<TransferMethod> methods = new HashSet<>();
        methods.add(TransferMethod.STREAMING);

        try {
            // probe sas.
            blobContainerClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new BlobContainerSasPermission().setWritePermission(true)));
            methods.add(TransferMethod.URL_WITH_SAS);
        } catch (Exception e) {
            // ignore
        }

        return methods;
    }
}
