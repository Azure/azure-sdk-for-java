package com.azure.storage.datamover.file.share;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.common.resource.TransferCapabilities;
import com.azure.storage.common.resource.TransferCapabilitiesBuilder;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FileShareDirectoryResourceContainer extends StorageResourceContainer {

    private final ShareDirectoryClient shareDirectoryClient;

    FileShareDirectoryResourceContainer(ShareDirectoryClient shareDirectoryClient) {
        this.shareDirectoryClient = Objects.requireNonNull(shareDirectoryClient);
    }

    @Override
    public Iterable<StorageResource> listResources() {
        return listResources(shareDirectoryClient, shareDirectoryClient)
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
                    return Stream.of(new FileShareResource(directoryClient.getFileClient(item.getName()), rootDir));
                }
            });
    }

    @Override
    public TransferCapabilities getIncomingTransferCapabilities() {
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

    @Override
    public List<String> getPath() {
        String directoryPath = shareDirectoryClient.getDirectoryPath();
        return Arrays.asList(directoryPath.split("/"));
    }

    @Override
    public StorageResource getStorageResource(List<String> path) {
        return new FileShareResource(
            shareDirectoryClient.getFileClient(String.join("/", path)),
            shareDirectoryClient);
    }
}
