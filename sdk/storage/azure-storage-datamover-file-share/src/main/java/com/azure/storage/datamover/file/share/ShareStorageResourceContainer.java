package com.azure.storage.datamover.file.share;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.common.resource.TransferCapabilities;
import com.azure.storage.common.resource.TransferCapabilitiesBuilder;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ShareStorageResourceContainer extends StorageResourceContainer {

    private final ShareClient shareClient;

    ShareStorageResourceContainer(ShareClient shareClient) {
        this.shareClient = Objects.requireNonNull(shareClient);
    }

    @Override
    public Iterable<StorageResource> listResources() {
        return listResources(shareClient.getRootDirectoryClient(), shareClient.getRootDirectoryClient())
            .collect(Collectors.toList());
    }

    private Stream<StorageResource> listResources(ShareDirectoryClient directoryClient, ShareDirectoryClient rootDir) {
        return directoryClient
            .listFilesAndDirectories()
            .stream()
            .flatMap(item -> {
                if (item.isDirectory()) {
                    return listResources(directoryClient.getSubdirectoryClient(item.getName()), rootDir);
                } else {
                    return Stream.of(new ShareFileStorageResource(directoryClient.getFileClient(item.getName()), rootDir));
                }
            });
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
    public StorageResource getStorageResource(List<String> path) {
        return new ShareFileStorageResource(
            shareClient.getFileClient(String.join("/", path)), shareClient.getRootDirectoryClient());
    }
}
