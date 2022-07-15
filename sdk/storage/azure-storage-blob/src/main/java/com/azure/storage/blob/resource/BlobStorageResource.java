package com.azure.storage.blob.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.resource.StorageResource;

import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class BlobStorageResource implements StorageResource {

    private final BlobClient blobClient;

    BlobStorageResource(BlobClient blobClient) {
        this.blobClient = Objects.requireNonNull(blobClient);
    }

    @Override
    public ReadableByteChannel openReadableByteChannel() {
        return Channels.newChannel(blobClient.openInputStream());
    }

    @Override
    public long getLength() {
        return blobClient.getProperties().getBlobSize();
    }

    @Override
    public void consumeReadableByteChannel(ReadableByteChannel channel, long length) {
        blobClient.upload(Channels.newInputStream(channel), length);
    }

    @Override
    public String getUrl() {
        return blobClient.getBlobUrl() + "?" + blobClient.generateSas(
            new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobSasPermission().setReadPermission(true)));
    }

    @Override
    public void consumeUrl(String sasUri) {
        blobClient.getBlockBlobClient().uploadFromUrl(sasUri);
    }

    @Override
    public List<String> getPath() {
        String[] split = blobClient.getBlobName().split("/");
        return Arrays.asList(split);
    }

    @Override
    public boolean canConsumeReadableByteChannel() {
        return true;
    }

    @Override
    public boolean canProduceReadableByteChannel() {
        return true;
    }

    @Override
    public boolean canConsumeUrl() {
        return true;
    }

    @Override
    public boolean canProduceUrl() {
        try {
            // probe sas.
            blobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new BlobContainerSasPermission().setReadPermission(true)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
