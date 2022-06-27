package com.azure.storage.datamover.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.models.TransferMethod;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class BlobResource extends StorageResource {

    private final BlobClient blobClient;

    BlobResource(BlobClient blobClient) {
        this.blobClient = Objects.requireNonNull(blobClient);
    }

    @Override
    protected Set<TransferMethod> getIncomingTransferMethods() {
        Set<TransferMethod> methods = new HashSet<>();
        methods.add(TransferMethod.STREAMING);

        try {
            // probe sas.
            blobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new BlobContainerSasPermission().setWritePermission(true)));
            methods.add(TransferMethod.URL_WITH_SAS);
        } catch (Exception e) {
            // ignore
        }

        return methods;
    }

    @Override
    protected Set<TransferMethod> getOutgoingTransferMethods() {
        Set<TransferMethod> methods = new HashSet<>();
        methods.add(TransferMethod.STREAMING);

        try {
            // probe sas.
            blobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new BlobContainerSasPermission().setReadPermission(true)));
            methods.add(TransferMethod.URL_WITH_SAS);
        } catch (Exception e) {
            // ignore
        }

        return methods;
    }
}
