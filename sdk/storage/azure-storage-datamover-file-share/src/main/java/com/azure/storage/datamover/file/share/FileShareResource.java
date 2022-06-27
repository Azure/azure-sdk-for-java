package com.azure.storage.datamover.file.share;

import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class FileShareResource extends StorageResource {

    private final ShareFileClient shareFileClient;

    FileShareResource(ShareFileClient shareFileClient) {
        this.shareFileClient = Objects.requireNonNull(shareFileClient);
    }

    @Override
    protected TransferCapabilities getIncomingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setWritePermission(true)));
            transferCapabilitiesBuilder.canUseSasUri(true);
        } catch (Exception e) {
            // ignore
        }

        return transferCapabilitiesBuilder.build();
    }

    @Override
    protected TransferCapabilities getOutgoingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            shareFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setReadPermission(true)));
            transferCapabilitiesBuilder.canUseSasUri(true);
        } catch (Exception e) {
            // ignore
        }

        return transferCapabilitiesBuilder.build();
    }

    @Override
    protected InputStream openInputStream() {
        return shareFileClient.openInputStream();
    }

    @Override
    protected long getLength() {
        return shareFileClient.getProperties().getContentLength();
    }

    @Override
    protected void consumeInputStream(InputStream inputStream, long length) {
        if (!shareFileClient.exists()) {
            shareFileClient.create(length);
        }
        shareFileClient.upload(inputStream, length, new ParallelTransferOptions());
    }

    @Override
    protected List<String> getPath() {
        String[] split = shareFileClient.getFilePath().split("/");
        return Arrays.asList(split);
    }
}
