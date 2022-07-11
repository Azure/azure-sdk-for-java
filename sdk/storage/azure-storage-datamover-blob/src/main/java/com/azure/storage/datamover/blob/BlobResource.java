package com.azure.storage.datamover.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.TransferCapabilities;
import com.azure.storage.common.resource.TransferCapabilitiesBuilder;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class BlobResource extends StorageResource {

    private final BlobClient blobClient;

    BlobResource(BlobClient blobClient) {
        this.blobClient = Objects.requireNonNull(blobClient);
    }

    @Override
    public TransferCapabilities getIncomingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            blobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new BlobContainerSasPermission().setWritePermission(true)));
            transferCapabilitiesBuilder.canUseSasUri(true);
        } catch (Exception e) {
            // ignore
        }

        return transferCapabilitiesBuilder.build();
    }

    @Override
    public TransferCapabilities getOutgoingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            blobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new BlobContainerSasPermission().setReadPermission(true)));
            transferCapabilitiesBuilder.canUseSasUri(true);
        } catch (Exception e) {
            // ignore
        }

        return transferCapabilitiesBuilder.build();
    }

    @Override
    public InputStream openInputStream() {
        return blobClient.openInputStream();
    }

    @Override
    public long getLength() {
        return blobClient.getProperties().getBlobSize();
    }

    @Override
    public void consumeInputStream(InputStream inputStream, long length) {
        blobClient.upload(inputStream, length);
    }

    @Override
    public String getSasUri() {
        return blobClient.getBlobUrl() + "?" + blobClient.generateSas(
            new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobSasPermission().setReadPermission(true)));
    }

    @Override
    public void consumeSasUri(String sasUri) {
        blobClient.getBlockBlobClient().uploadFromUrl(sasUri);
    }

    @Override
    public List<String> getPath() {
        String[] split = blobClient.getBlobName().split("/");
        return Arrays.asList(split);
    }
}
