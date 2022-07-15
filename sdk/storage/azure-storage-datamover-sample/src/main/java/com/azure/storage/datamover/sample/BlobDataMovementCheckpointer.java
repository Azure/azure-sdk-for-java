package com.azure.storage.datamover.sample;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.common.datamover.checkpoint.DataMovementCheckpointer;
import com.azure.storage.common.datamover.checkpoint.DataTransferState;

import java.util.stream.Stream;

public class BlobDataMovementCheckpointer implements DataMovementCheckpointer {
    private final BlobContainerClient blobContainerClient;


    public BlobDataMovementCheckpointer(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    @Override
    public Stream<DataTransferState> listTransfers() {
        return blobContainerClient.listBlobs()
            .stream()
            .map(blobItem -> {
                return blobContainerClient.getBlobClient(blobItem.getName())
                    .downloadContent()
                    .toObject(DataTransferState.class);
            });
    }

    @Override
    public void addTransfer(DataTransferState transfer) {
        String blobName = transfer.getIdentifier();
        BinaryData data = BinaryData.fromObject(transfer);
        blobContainerClient.getBlobClient(blobName).upload(data);
    }

    @Override
    public void removeTransfer(DataTransferState transferState) {
        String blobName = transferState.getIdentifier();
        blobContainerClient.getBlobClient(blobName).deleteIfExists();
    }
}
