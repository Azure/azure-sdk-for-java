package com.azure.storage.datamover.file.share;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class FileShareResourceContainer extends StorageResourceContainer {

    private final ShareClient shareClient;

    FileShareResourceContainer(ShareClient shareClient) {
        this.shareClient = Objects.requireNonNull(shareClient);
    }

    @Override
    protected Iterable<StorageResource> listResources() {
        return shareClient.getRootDirectoryClient()
            .listFilesAndDirectories()
            .stream().filter(
                item -> !item.isDirectory()
            ).map(item -> new FileShareResource(shareClient.getFileClient(item.getName())))
            .collect(Collectors.toList());
    }

    @Override
    protected TransferCapabilities getIncomingTransferCapabilities() {
        TransferCapabilitiesBuilder transferCapabilitiesBuilder = new TransferCapabilitiesBuilder()
            .canStream(true);

        try {
            // probe sas.
            shareClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
                new ShareSasPermission().setWritePermission(true)));
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
    protected StorageResource getStorageResource(List<String> path) {
        return new FileShareResource(shareClient.getFileClient(String.join("/", path)));
    }
}
