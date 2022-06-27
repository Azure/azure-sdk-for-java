package com.azure.storage.datamover.file.share;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

class FileShareDirectoryResourceContainer extends StorageResourceContainer {

    private final ShareDirectoryClient shareDirectoryClient;

    FileShareDirectoryResourceContainer(ShareDirectoryClient shareDirectoryClient) {
        this.shareDirectoryClient = Objects.requireNonNull(shareDirectoryClient);
    }

    @Override
    protected Iterable<StorageResource> listResources() {
        return shareDirectoryClient.listFilesAndDirectories()
            .stream().filter(
                item -> !item.isDirectory()
            ).map(item -> new FileShareResource(shareDirectoryClient.getFileClient(item.getName())))
            .collect(Collectors.toList());
    }

    @Override
    protected TransferCapabilities getIncomingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            shareDirectoryClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setWritePermission(true)));
            transferCapabilitiesBuilder.canUseSasUri(true);
        } catch (Exception e) {
            // ignore
        }

        return transferCapabilitiesBuilder.build();
    }
}
