package com.azure.storage.datamover.file.share;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.file.share.ShareDirectoryClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ShareDirectoryStorageResourceContainer implements StorageResourceContainer {

    private final ShareDirectoryClient shareDirectoryClient;

    ShareDirectoryStorageResourceContainer(ShareDirectoryClient shareDirectoryClient) {
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
                    return Stream.of(new ShareFileStorageResource(directoryClient.getFileClient(item.getName()), rootDir));
                }
            });
    }

    @Override
    public List<String> getPath() {
        String directoryPath = shareDirectoryClient.getDirectoryPath();
        return Arrays.asList(directoryPath.split("/"));
    }

    @Override
    public StorageResource getStorageResource(List<String> path) {
        return new ShareFileStorageResource(
            shareDirectoryClient.getFileClient(String.join("/", path)),
            shareDirectoryClient);
    }

    @Override
    public StorageResourceContainer getStorageResourceContainer(List<String> path) {
        return new ShareDirectoryStorageResourceContainer(
            shareDirectoryClient.getSubdirectoryClient(String.join("/", path)));
    }
}
